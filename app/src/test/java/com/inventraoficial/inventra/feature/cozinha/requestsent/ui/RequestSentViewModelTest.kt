package com.inventraoficial.inventra.feature.cozinha.requestsent.ui

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

@OptIn(ExperimentalCoroutinesApi::class)
class RequestSentViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
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

    private fun createViewModel() = RequestSentViewModel(kitchenAccessRepository, authRepository)

    @Test
    fun `comeca carregando`() {
        val viewModel = createViewModel()

        assertTrue(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `pedido pendente mostra a cozinha e a data de vencimento`() =
        runTest(testDispatcher) {
            kitchenAccessRepository.myRequestResult = Result.success(pending.copy(expiresAt = "2026-10-18T12:00:00"))
            val viewModel = createViewModel()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertFalse(state.isLoading)
            assertEquals(RequestSentStatus.Pending, state.status)
            assertEquals("Sabor Filial Sul", state.cozinhaName)
            assertEquals("18/10/2026", state.expiresAt)
            assertNull(state.destination)
        }

    @Test
    fun `pedido recusado mostra o motivo sem espacos`() =
        runTest(testDispatcher) {
            val reason = "  Não faz parte da equipe  "
            val rejected = pending.copy(status = KitchenAccessRequestStatus.REJECTED, reason = reason)
            kitchenAccessRepository.myRequestResult = Result.success(rejected)
            val viewModel = createViewModel()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertEquals(RequestSentStatus.Rejected, state.status)
            assertEquals("Não faz parte da equipe", state.rejectionReason)
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
    fun `pedido aprovado atualiza o usuario e vai para a Home`() =
        runTest(testDispatcher) {
            kitchenAccessRepository.myRequestResult =
                Result.success(pending.copy(status = KitchenAccessRequestStatus.APPROVED))
            val viewModel = createViewModel()
            advanceUntilIdle()

            assertEquals(1, authRepository.refreshCalls)
            assertEquals(RequestSentDestination.Home, viewModel.uiState.value.destination)
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
    fun `atualizar status consulta de novo e desliga o refreshing`() =
        runTest(testDispatcher) {
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.onRefreshClick()
            assertTrue(viewModel.uiState.value.isRefreshing)
            advanceUntilIdle()

            assertFalse(viewModel.uiState.value.isRefreshing)
            assertEquals(2, kitchenAccessRepository.myRequestCalls)
        }

    @Test
    fun `escolher outra cozinha volta para a selecao`() {
        val viewModel = createViewModel()

        viewModel.onChooseAnotherClick()

        assertEquals(RequestSentDestination.SelectCozinha, viewModel.uiState.value.destination)
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
