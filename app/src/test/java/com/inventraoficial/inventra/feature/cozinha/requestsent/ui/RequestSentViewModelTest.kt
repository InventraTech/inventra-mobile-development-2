package com.inventraoficial.inventra.feature.cozinha.requestsent.ui

import com.inventraoficial.inventra.core.designsystem.molecules.InventraCozinhaRequestStatus
import com.inventraoficial.inventra.data.remote.dto.kitchenaccess.KitchenAccessRequestStatus
import com.inventraoficial.inventra.data.repository.FakeAuthRepository
import com.inventraoficial.inventra.data.repository.FakeKitchenAccessRepository
import com.inventraoficial.inventra.data.repository.FakeMembersRepository
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
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneOffset

@OptIn(ExperimentalCoroutinesApi::class)
class RequestSentViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private val today = LocalDate.of(2026, 10, 17)
    private val clock = Clock.fixed(today.atTime(12, 0).toInstant(ZoneOffset.UTC), ZoneOffset.UTC)
    private val pending = FakeMembersRepository.pendingRequest
    private lateinit var kitchenAccessRepository: FakeKitchenAccessRepository
    private lateinit var authRepository: FakeAuthRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        kitchenAccessRepository = FakeKitchenAccessRepository()
        authRepository = FakeAuthRepository(hasSession = true)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = RequestSentViewModel(kitchenAccessRepository, authRepository, clock)

    @Test
    fun `comeca carregando`() {
        val viewModel = createViewModel()

        assertTrue(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `pedido pendente mostra a cozinha, o envio e o prazo`() =
        runTest(testDispatcher) {
            kitchenAccessRepository.myRequestResult = Result.success(pending.copy(expiresAt = "2026-10-22T12:00:00"))
            val viewModel = createViewModel()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertFalse(state.isLoading)
            assertEquals(RequestSentStatus.Pending, state.status)
            assertEquals("Sabor Filial Sul", state.cozinhaName)
            assertEquals("10/10/2026", state.sentAt)
            assertEquals("22/10/2026", state.expiresAt)
            assertEquals("vence em 5 dias", state.expiresIn)
            assertFalse(state.canRequestAnother)
            assertNull(state.destination)
        }

    @Test
    fun `prazo de amanha e de hoje`() =
        runTest(testDispatcher) {
            kitchenAccessRepository.myRequestResult = Result.success(pending.copy(expiresAt = "2026-10-18T12:00:00"))
            val viewModel = createViewModel()
            advanceUntilIdle()
            assertEquals("vence amanhã", viewModel.uiState.value.expiresIn)

            kitchenAccessRepository.myRequestResult = Result.success(pending.copy(expiresAt = "2026-10-17T23:00:00"))
            viewModel.onRefreshClick()
            advanceUntilIdle()
            assertEquals("vence hoje", viewModel.uiState.value.expiresIn)
        }

    @Test
    fun `pedido recusado mostra o motivo sem espacos e deixa pedir em outra`() =
        runTest(testDispatcher) {
            val reason = "  Não faz parte da equipe  "
            val rejected = pending.copy(status = KitchenAccessRequestStatus.REJECTED, reason = reason)
            kitchenAccessRepository.myRequestResult = Result.success(rejected)
            val viewModel = createViewModel()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertEquals(RequestSentStatus.Rejected, state.status)
            assertEquals("Não faz parte da equipe", state.rejectionReason)
            assertNull(state.expiresIn)
            assertTrue(state.canRequestAnother)
        }

    @Test
    fun `motivo em branco vira nulo`() =
        runTest(testDispatcher) {
            kitchenAccessRepository.myRequestResult =
                Result.success(pending.copy(status = KitchenAccessRequestStatus.REJECTED, reason = "   "))
            val viewModel = createViewModel()
            advanceUntilIdle()

            assertNull(viewModel.uiState.value.rejectionReason)
        }

    @Test
    fun `lista as outras cozinhas pedidas, sem a do pedido atual`() =
        runTest(testDispatcher) {
            val viewModel = createViewModel()
            advanceUntilIdle()

            val others = viewModel.uiState.value.otherRequests
            assertEquals(listOf(8, 9), others.map { it.id })
            assertEquals(
                listOf(InventraCozinhaRequestStatus.Recusada, InventraCozinhaRequestStatus.Pendente),
                others.map { it.status },
            )
            assertEquals(listOf("" to null), kitchenAccessRepository.discoverCalls)
        }

    @Test
    fun `falha na busca de cozinhas nao atrapalha o pedido atual`() =
        runTest(testDispatcher) {
            kitchenAccessRepository.discoverResult = Result.failure(Exception("Muitas buscas"))
            val viewModel = createViewModel()
            advanceUntilIdle()

            assertNull(viewModel.uiState.value.errorMessage)
            assertEquals("Sabor Filial Sul", viewModel.uiState.value.cozinhaName)
            assertTrue(
                viewModel.uiState.value.otherRequests
                    .isEmpty(),
            )
        }

    @Test
    fun `pedido aprovado atualiza o usuario e vai para a Home`() =
        runTest(testDispatcher) {
            kitchenAccessRepository.myRequestResult =
                Result.success(pending.copy(status = KitchenAccessRequestStatus.APPROVED))
            val viewModel = createViewModel()
            advanceUntilIdle()

            assertEquals(1, authRepository.refreshCalls)
            assertEquals(RequestSentDestination.Home, viewModel.uiState.value.destination)
            assertTrue(kitchenAccessRepository.discoverCalls.isEmpty())
        }

    @Test
    fun `aprovado mas sem conseguir atualizar o usuario mostra o erro`() =
        runTest(testDispatcher) {
            kitchenAccessRepository.myRequestResult =
                Result.success(pending.copy(status = KitchenAccessRequestStatus.APPROVED))
            authRepository.refreshResult = Result.failure(Exception("Sem conexão"))
            val viewModel = createViewModel()
            advanceUntilIdle()

            assertEquals("Sem conexão", viewModel.uiState.value.errorMessage)
            assertNull(viewModel.uiState.value.destination)
        }

    @Test
    fun `sem nenhum pedido manda escolher uma cozinha`() =
        runTest(testDispatcher) {
            kitchenAccessRepository.myRequestResult = Result.success(null)
            val viewModel = createViewModel()
            advanceUntilIdle()

            assertEquals(RequestSentDestination.SelectCozinha, viewModel.uiState.value.destination)
        }

    @Test
    fun `falha ao carregar mostra o erro e tentar de novo consulta outra vez`() =
        runTest(testDispatcher) {
            kitchenAccessRepository.myRequestResult = Result.failure(Exception("Sem conexão"))
            val viewModel = createViewModel()
            advanceUntilIdle()

            assertEquals("Sem conexão", viewModel.uiState.value.errorMessage)

            kitchenAccessRepository.myRequestResult = Result.success(pending)
            viewModel.onRetryClick()
            advanceUntilIdle()

            assertNull(viewModel.uiState.value.errorMessage)
            assertEquals(2, kitchenAccessRepository.myRequestCalls)
        }

    @Test
    fun `atualizar status consulta o pedido e as cozinhas de novo e desliga o refreshing`() =
        runTest(testDispatcher) {
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.onRefreshClick()
            assertTrue(viewModel.uiState.value.isRefreshing)
            advanceUntilIdle()

            assertFalse(viewModel.uiState.value.isRefreshing)
            assertEquals(2, kitchenAccessRepository.myRequestCalls)
            assertEquals(2, kitchenAccessRepository.discoverCalls.size)
        }

    @Test
    fun `atualizacao automatica so confere o pedido e nao roda enquanto carrega`() =
        runTest(testDispatcher) {
            val viewModel = createViewModel()
            viewModel.onAutoRefresh()
            advanceUntilIdle()
            assertEquals(1, kitchenAccessRepository.myRequestCalls)

            viewModel.onAutoRefresh()
            advanceUntilIdle()

            assertEquals(2, kitchenAccessRepository.myRequestCalls)
            assertEquals(1, kitchenAccessRepository.discoverCalls.size)
        }

    @Test
    fun `atualizacao automatica que falha mantem o pedido na tela, sem erro`() =
        runTest(testDispatcher) {
            val viewModel = createViewModel()
            advanceUntilIdle()

            kitchenAccessRepository.myRequestResult = Result.failure(Exception("Sem conexão"))
            viewModel.onAutoRefresh()
            advanceUntilIdle()

            assertNull(viewModel.uiState.value.errorMessage)
            assertEquals("Sabor Filial Sul", viewModel.uiState.value.cozinhaName)
        }

    @Test
    fun `atualizacao automatica percebe a aprovacao e vai para a Home`() =
        runTest(testDispatcher) {
            val viewModel = createViewModel()
            advanceUntilIdle()

            kitchenAccessRepository.myRequestResult =
                Result.success(pending.copy(status = KitchenAccessRequestStatus.APPROVED))
            viewModel.onAutoRefresh()
            advanceUntilIdle()

            assertEquals(RequestSentDestination.Home, viewModel.uiState.value.destination)
        }

    @Test
    fun `sair da conta faz logout e vai para o Login`() =
        runTest(testDispatcher) {
            val viewModel = createViewModel()

            viewModel.onLogoutClick()
            advanceUntilIdle()

            assertEquals(1, authRepository.logoutCalls)
            assertEquals(RequestSentDestination.Login, viewModel.uiState.value.destination)
        }
}
