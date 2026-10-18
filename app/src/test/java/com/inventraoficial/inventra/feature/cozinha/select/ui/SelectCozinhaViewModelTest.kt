package com.inventraoficial.inventra.feature.cozinha.select.ui

import com.inventraoficial.inventra.core.designsystem.molecules.InventraCozinhaRequestStatus
import com.inventraoficial.inventra.data.local.FakeLocationProvider
import com.inventraoficial.inventra.data.remote.dto.kitchenaccess.KitchenAccessRequestStatus
import com.inventraoficial.inventra.data.repository.FakeAuthRepository
import com.inventraoficial.inventra.data.repository.FakeKitchenAccessRepository
import com.inventraoficial.inventra.data.repository.FakeMembersRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
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
class SelectCozinhaViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var kitchenAccessRepository: FakeKitchenAccessRepository
    private lateinit var authRepository: FakeAuthRepository
    private lateinit var locationProvider: FakeLocationProvider

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        // Sem pedido pendente: a seleção de cozinhas libera o pedido de entrada.
        kitchenAccessRepository = FakeKitchenAccessRepository(myRequestResult = Result.success(null))
        authRepository = FakeAuthRepository(hasSession = true)
        locationProvider = FakeLocationProvider()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = SelectCozinhaViewModel(kitchenAccessRepository, authRepository, locationProvider)

    @Test
    fun `ao abrir carrega as cozinhas sem localizacao`() =
        runTest(testDispatcher) {
            val viewModel = createViewModel()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertFalse(state.isLoading)
            assertEquals(listOf(7, 8, 9), state.cozinhas.map { it.id })
            assertEquals(listOf("" to null), kitchenAccessRepository.discoverCalls)
        }

    @Test
    fun `converte a resposta da API para os itens da lista`() =
        runTest(testDispatcher) {
            val viewModel = createViewModel()
            advanceUntilIdle()

            val (perto, longe, semLocalizacao) = viewModel.uiState.value.cozinhas
            assertEquals("São Paulo, SP", perto.location)
            assertEquals("850 m", perto.distance)
            assertEquals("São Paulo, SP · 850 m", perto.subtitle)
            assertEquals(InventraCozinhaRequestStatus.Disponivel, perto.status)
            assertEquals("12,3 km", longe.distance)
            assertEquals(InventraCozinhaRequestStatus.Recusada, longe.status)
            assertEquals("Localização não informada", semLocalizacao.subtitle)
            assertEquals(InventraCozinhaRequestStatus.Pendente, semLocalizacao.status)
        }

    @Test
    fun `falha ao carregar mostra a mensagem e tentar de novo busca outra vez`() =
        runTest(testDispatcher) {
            kitchenAccessRepository.discoverResult = Result.failure(Exception("Sem conexão"))
            val viewModel = createViewModel()
            advanceUntilIdle()

            assertEquals("Sem conexão", viewModel.uiState.value.errorMessage)

            kitchenAccessRepository.discoverResult = Result.success(emptyList())
            viewModel.onRetryClick()
            advanceUntilIdle()

            assertNull(viewModel.uiState.value.errorMessage)
            assertEquals(2, kitchenAccessRepository.discoverCalls.size)
        }

    @Test
    fun `a busca espera o usuario parar de digitar`() =
        runTest(testDispatcher) {
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.onSearchQueryChange("sa")
            viewModel.onSearchQueryChange("sabor")
            advanceTimeBy(300)
            assertEquals(1, kitchenAccessRepository.discoverCalls.size)

            advanceUntilIdle()
            assertEquals(listOf("" to null, "sabor" to null), kitchenAccessRepository.discoverCalls)
        }

    @Test
    fun `perto de mim busca com a localizacao do usuario`() =
        runTest(testDispatcher) {
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.onLocationPermissionResult(granted = true)
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertTrue(state.isNearMe)
            assertTrue(state.hasLocationPermission)
            assertEquals(FakeLocationProvider.paulista, state.userLocation)
            assertEquals("" to FakeLocationProvider.paulista, kitchenAccessRepository.discoverCalls.last())
        }

    @Test
    fun `negar a permissao mostra o aviso e nao busca a localizacao`() =
        runTest(testDispatcher) {
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.onLocationPermissionResult(granted = false)
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertFalse(state.isNearMe)
            assertEquals("Permita a localização para ver as cozinhas perto de você.", state.locationMessage)
            assertEquals(0, locationProvider.calls)
        }

    @Test
    fun `sem localizacao disponivel mostra o erro do provider`() =
        runTest(testDispatcher) {
            locationProvider.result = Result.failure(Exception("Localização desligada"))
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.onNearMeClick()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertFalse(state.isNearMe)
            assertFalse(state.isLocating)
            assertEquals("Localização desligada", state.locationMessage)
        }

    @Test
    fun `tocar de novo em perto de mim volta para a busca geral`() =
        runTest(testDispatcher) {
            val viewModel = createViewModel()
            advanceUntilIdle()
            viewModel.onNearMeClick()
            advanceUntilIdle()

            viewModel.onNearMeClick()
            advanceUntilIdle()

            assertFalse(viewModel.uiState.value.isNearMe)
            assertEquals("" to null, kitchenAccessRepository.discoverCalls.last())
        }

    @Test
    fun `solicitar entrada envia o pedido e marca isRequestSent`() =
        runTest(testDispatcher) {
            val viewModel = createViewModel()
            advanceUntilIdle()
            viewModel.onCozinhaClick(
                viewModel.uiState.value.cozinhas
                    .first(),
            )

            viewModel.onRequestAccessClick()
            assertTrue(viewModel.uiState.value.isRequesting)
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertTrue(state.isRequestSent)
            assertFalse(state.isRequesting)
            assertEquals(listOf(7), kitchenAccessRepository.requestedKitchenIds)
        }

    @Test
    fun `cozinha com pedido pendente nao manda outro`() =
        runTest(testDispatcher) {
            val viewModel = createViewModel()
            advanceUntilIdle()
            viewModel.onCozinhaClick(
                viewModel.uiState.value.cozinhas
                    .last(),
            )

            viewModel.onRequestAccessClick()
            advanceUntilIdle()

            assertTrue(kitchenAccessRepository.requestedKitchenIds.isEmpty())
        }

    @Test
    fun `com pedido pendente em outra cozinha so mostra as cozinhas e nao deixa pedir`() =
        runTest(testDispatcher) {
            kitchenAccessRepository.myRequestResult = Result.success(FakeMembersRepository.pendingRequest)
            val viewModel = createViewModel()
            advanceUntilIdle()
            assertEquals("Sabor Filial Sul", viewModel.uiState.value.pendingCozinhaName)

            viewModel.onCozinhaClick(viewModel.uiState.value.cozinhas[1])
            viewModel.onRequestAccessClick()
            advanceUntilIdle()

            assertTrue(kitchenAccessRepository.requestedKitchenIds.isEmpty())
            assertFalse(viewModel.uiState.value.isRequestSent)
        }

    @Test
    fun `pedido recusado nao bloqueia um novo pedido`() =
        runTest(testDispatcher) {
            val rejected = FakeMembersRepository.pendingRequest.copy(status = KitchenAccessRequestStatus.REJECTED)
            kitchenAccessRepository.myRequestResult = Result.success(rejected)
            val viewModel = createViewModel()
            advanceUntilIdle()

            assertNull(viewModel.uiState.value.pendingCozinhaName)
        }

    @Test
    fun `falha ao solicitar mostra o erro no sheet`() =
        runTest(testDispatcher) {
            kitchenAccessRepository.requestResult =
                Result.failure(Exception("Você já tem uma solicitação de entrada pendente."))
            val viewModel = createViewModel()
            advanceUntilIdle()
            viewModel.onCozinhaClick(
                viewModel.uiState.value.cozinhas
                    .first(),
            )

            viewModel.onRequestAccessClick()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertEquals("Você já tem uma solicitação de entrada pendente.", state.requestErrorMessage)
            assertFalse(state.isRequestSent)
            assertEquals(7, state.selectedCozinha?.id)
        }

    @Test
    fun `fechar o sheet limpa a cozinha e o erro`() =
        runTest(testDispatcher) {
            val viewModel = createViewModel()
            advanceUntilIdle()
            viewModel.onCozinhaClick(
                viewModel.uiState.value.cozinhas
                    .first(),
            )

            viewModel.onSheetDismiss()

            assertNull(viewModel.uiState.value.selectedCozinha)
            assertNull(viewModel.uiState.value.requestErrorMessage)
        }

    @Test
    fun `fechar a tela sai da conta e marca isLoggedOut`() =
        runTest(testDispatcher) {
            val viewModel = createViewModel()

            viewModel.onCloseClick()
            advanceUntilIdle()

            assertEquals(1, authRepository.logoutCalls)
            assertTrue(viewModel.uiState.value.isLoggedOut)
        }
}
