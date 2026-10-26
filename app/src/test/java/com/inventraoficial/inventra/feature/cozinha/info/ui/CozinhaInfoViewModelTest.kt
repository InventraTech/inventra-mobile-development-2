package com.inventraoficial.inventra.feature.cozinha.info.ui

import com.inventraoficial.inventra.data.local.FakeLocationProvider
import com.inventraoficial.inventra.data.local.SessionUser
import com.inventraoficial.inventra.data.remote.dto.auth.AccessType
import com.inventraoficial.inventra.data.remote.dto.kitchen.UpdateKitchenRequest
import com.inventraoficial.inventra.data.repository.FakeAuthRepository
import com.inventraoficial.inventra.data.repository.FakeKitchenRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CozinhaInfoViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private val kitchen =
        FakeKitchenRepository.fakeKitchen.copy(city = "São Paulo", state = "SP", latitude = -23.5, longitude = -46.6)
    private lateinit var kitchenRepository: FakeKitchenRepository
    private lateinit var authRepository: FakeAuthRepository
    private lateinit var locationProvider: FakeLocationProvider

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        kitchenRepository = FakeKitchenRepository(Result.success(kitchen))
        locationProvider = FakeLocationProvider()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(
        accessType: AccessType = AccessType.SUPERVISOR,
        kitchenId: Int? = 7,
    ): CozinhaInfoViewModel {
        val user = SessionUser("1", "Ana", "ana@inventra.com", accessType, kitchenId)
        authRepository = FakeAuthRepository(initialUser = user)
        return CozinhaInfoViewModel(kitchenRepository, authRepository, locationProvider)
    }

    private fun CozinhaInfoViewModel.startEditing() {
        onEditClick()
        assertTrue(uiState.value.isEditing)
    }

    @Test
    fun `carrega a cozinha do usuario com os dados reais`() =
        runTest(testDispatcher) {
            val viewModel = createViewModel()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertFalse(state.isLoading)
            assertEquals(listOf(7), kitchenRepository.kitchenCalls)
            assertEquals("Sabor Filial Sul", state.name)
            assertEquals("K7X2PQ", state.code)
            assertEquals("São Paulo", state.city)
            assertEquals("SP", state.state)
            assertTrue(state.hasLocation)
            assertTrue(state.canEdit)
        }

    @Test
    fun `quem nao e supervisor so consulta`() =
        runTest(testDispatcher) {
            val viewModel = createViewModel(accessType = AccessType.ESTOQUISTA)
            advanceUntilIdle()

            assertFalse(viewModel.uiState.value.canEdit)
            viewModel.onEditClick()
            assertFalse(viewModel.uiState.value.isEditing)
        }

    @Test
    fun `sem cozinha mostra o aviso e falha ao carregar deixa tentar de novo`() =
        runTest(testDispatcher) {
            val semCozinha = createViewModel(kitchenId = null)
            advanceUntilIdle()
            assertEquals("Você ainda não faz parte de uma cozinha.", semCozinha.uiState.value.errorMessage)

            kitchenRepository.result = Result.failure(Exception("Sem conexão"))
            val viewModel = createViewModel()
            advanceUntilIdle()
            assertEquals("Sem conexão", viewModel.uiState.value.errorMessage)

            kitchenRepository.result = Result.success(kitchen)
            viewModel.onRetryClick()
            advanceUntilIdle()
            assertNull(viewModel.uiState.value.errorMessage)
        }

    @Test
    fun `cancelar volta para o que estava salvo`() =
        runTest(testDispatcher) {
            val viewModel = createViewModel()
            advanceUntilIdle()
            viewModel.startEditing()

            viewModel.onNameChange("Outro nome")
            viewModel.onCancelClick()

            assertEquals("Sabor Filial Sul", viewModel.uiState.value.name)
            assertFalse(viewModel.uiState.value.isEditing)
        }

    @Test
    fun `salvar sem mudar o endereco nao manda o endereco nem coordenadas`() =
        runTest(testDispatcher) {
            val viewModel = createViewModel()
            advanceUntilIdle()
            viewModel.startEditing()

            viewModel.onNameChange(" Sabor Filial Norte ")
            viewModel.onAcceptingRequestsChange(false)
            viewModel.onSaveClick()
            advanceUntilIdle()

            val request = UpdateKitchenRequest("Sabor Filial Norte", null, "São Paulo", "SP", null, null, false)
            assertEquals(listOf(7 to request), kitchenRepository.updates)
            val state = viewModel.uiState.value
            assertFalse(state.isEditing)
            assertEquals("Sabor Filial Norte", state.name)
            assertFalse(state.acceptingRequests)
            assertEquals("Informações salvas.", state.savedMessage)
            // O nome mudou: atualiza a sessão, que a Home e o Perfil usam.
            assertEquals(1, authRepository.refreshCalls)
        }

    @Test
    fun `endereco novo sem pino avisa que a localizacao vai ser buscada`() =
        runTest(testDispatcher) {
            kitchenRepository.updateResult =
                Result.success(kitchen.copy(address = "Rua Irineu José Bordon, 335", latitude = null, longitude = null))
            val viewModel = createViewModel()
            advanceUntilIdle()
            viewModel.startEditing()

            viewModel.onAddressChange("Rua Irineu José Bordon, 335")
            viewModel.onSaveClick()
            advanceUntilIdle()

            assertEquals(
                "Rua Irineu José Bordon, 335",
                kitchenRepository.updates
                    .single()
                    .second.address,
            )
            assertFalse(viewModel.uiState.value.hasLocation)
            assertTrue(
                viewModel.uiState.value.savedMessage
                    .orEmpty()
                    .startsWith("Informações salvas. Estamos procurando"),
            )
            assertEquals(0, authRepository.refreshCalls)
        }

    @Test
    fun `usar minha localizacao manda as coordenadas no salvar`() =
        runTest(testDispatcher) {
            val viewModel = createViewModel()
            advanceUntilIdle()
            viewModel.startEditing()

            viewModel.onUseMyLocationClick()
            advanceUntilIdle()
            assertEquals(FakeLocationProvider.paulista, viewModel.uiState.value.newLocation)

            viewModel.onSaveClick()
            advanceUntilIdle()

            val request = kitchenRepository.updates.single().second
            assertEquals(FakeLocationProvider.paulista.latitude, request.latitude)
            assertEquals(FakeLocationProvider.paulista.longitude, request.longitude)
            assertNull(viewModel.uiState.value.newLocation)
        }

    @Test
    fun `sem permissao ou sem localizacao mostra o aviso`() =
        runTest(testDispatcher) {
            val viewModel = createViewModel()
            advanceUntilIdle()
            viewModel.startEditing()

            viewModel.onLocationPermissionResult(granted = false)
            assertEquals(
                "Permita a localização para marcar a cozinha no mapa.",
                viewModel.uiState.value.locationMessage,
            )
            assertEquals(0, locationProvider.calls)

            locationProvider.result = Result.failure(Exception("Localização desligada"))
            viewModel.onLocationPermissionResult(granted = true)
            advanceUntilIdle()
            assertEquals("Localização desligada", viewModel.uiState.value.locationMessage)
            assertNull(viewModel.uiState.value.newLocation)
        }

    @Test
    fun `UF aceita so letras, vira maiuscula e precisa de 2 letras`() =
        runTest(testDispatcher) {
            val viewModel = createViewModel()
            advanceUntilIdle()
            viewModel.startEditing()

            viewModel.onStateChange("m1")
            assertEquals("SP", viewModel.uiState.value.state)
            viewModel.onStateChange("m")
            assertEquals("M", viewModel.uiState.value.state)

            viewModel.onSaveClick()
            assertEquals("A UF tem 2 letras", viewModel.uiState.value.saveError)
            assertTrue(kitchenRepository.updates.isEmpty())
        }

    @Test
    fun `nome vazio nao salva e falha do backend fica na tela`() =
        runTest(testDispatcher) {
            val viewModel = createViewModel()
            advanceUntilIdle()
            viewModel.startEditing()

            viewModel.onNameChange("  ")
            viewModel.onSaveClick()
            assertEquals("Informe o nome da cozinha", viewModel.uiState.value.saveError)

            kitchenRepository.updateResult = Result.failure(Exception("Sem permissão"))
            viewModel.onNameChange("Sabor Filial Norte")
            viewModel.onSaveClick()
            advanceUntilIdle()

            assertEquals("Sem permissão", viewModel.uiState.value.saveError)
            assertTrue(viewModel.uiState.value.isEditing)
        }
}
