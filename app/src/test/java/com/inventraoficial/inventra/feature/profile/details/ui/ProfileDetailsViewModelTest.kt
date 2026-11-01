package com.inventraoficial.inventra.feature.profile.details.ui

import com.inventraoficial.inventra.data.local.SessionUser
import com.inventraoficial.inventra.data.remote.dto.auth.AccessType
import com.inventraoficial.inventra.data.repository.FakeAuthRepository
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileDetailsViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: FakeAuthRepository
    private lateinit var viewModel: ProfileDetailsViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeAuthRepository(hasSession = true)
        viewModel = ProfileDetailsViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `comeca logado e sem o modal de confirmacao`() {
        val state = viewModel.uiState.value
        assertFalse(state.isLoggedOut)
        assertFalse(state.isLogoutDialogVisible)
    }

    @Test
    fun `onLogoutClick so abre o modal e nao sai da conta`() =
        runTest(testDispatcher) {
            viewModel.onLogoutClick()
            advanceUntilIdle()

            assertTrue(viewModel.uiState.value.isLogoutDialogVisible)
            assertFalse(viewModel.uiState.value.isLoggedOut)
            assertEquals(0, repository.logoutCalls)
        }

    @Test
    fun `onLogoutDismiss fecha o modal e continua logado`() =
        runTest(testDispatcher) {
            viewModel.onLogoutClick()

            viewModel.onLogoutDismiss()
            advanceUntilIdle()

            assertFalse(viewModel.uiState.value.isLogoutDialogVisible)
            assertFalse(viewModel.uiState.value.isLoggedOut)
            assertEquals(0, repository.logoutCalls)
        }

    @Test
    fun `onLogoutConfirm fecha o modal, encerra a sessao e marca isLoggedOut`() =
        runTest(testDispatcher) {
            viewModel.onLogoutClick()

            viewModel.onLogoutConfirm()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertFalse(state.isLogoutDialogVisible)
            assertTrue(state.isLoggedOut)
            assertEquals(1, repository.logoutCalls)
            assertFalse(repository.hasSession())
        }

    @Test
    fun `isLoggedOut so vira true depois que o logout termina`() =
        runTest(testDispatcher) {
            viewModel.onLogoutConfirm()

            assertFalse(viewModel.uiState.value.isLoggedOut)
            assertEquals(0, repository.logoutCalls)
        }

    @Test
    fun `mostra os dados do usuario logado`() =
        runTest(testDispatcher) {
            repository.emitUser(supervisorComCozinha)
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertEquals("Felipe Kogake", state.fullName)
            assertEquals("felipe@inventra.com", state.email)
            assertEquals("Supervisor", state.roleLabel)
            assertEquals("Sabor Filial Sul", state.cozinhaName)
        }

    @Test
    fun `usuario sem cozinha mostra a mensagem no lugar do nome da cozinha`() =
        runTest(testDispatcher) {
            repository.emitUser(supervisorComCozinha.copy(kitchenId = null, kitchenName = null))
            advanceUntilIdle()

            assertEquals("Nenhuma cozinha vinculada", viewModel.uiState.value.cozinhaName)
        }

    @Test
    fun `atualiza sozinho quando o usuario salvo muda`() =
        runTest(testDispatcher) {
            repository.emitUser(supervisorComCozinha.copy(kitchenId = null, kitchenName = null))
            advanceUntilIdle()

            repository.emitUser(supervisorComCozinha)
            advanceUntilIdle()

            assertEquals("Sabor Filial Sul", viewModel.uiState.value.cozinhaName)
        }

    @Test
    fun `so mostra sair da cozinha para quem tem cozinha`() =
        runTest(testDispatcher) {
            repository.emitUser(supervisorComCozinha.copy(kitchenId = null, kitchenName = null))
            advanceUntilIdle()
            assertFalse(viewModel.uiState.value.leaveKitchen.canLeave)

            repository.emitUser(supervisorComCozinha)
            advanceUntilIdle()
            assertTrue(viewModel.uiState.value.leaveKitchen.canLeave)
        }

    @Test
    fun `sair da cozinha so acontece depois de confirmar`() =
        runTest(testDispatcher) {
            repository.emitUser(supervisorComCozinha)
            advanceUntilIdle()

            viewModel.onLeaveKitchenClick()
            assertTrue(viewModel.uiState.value.leaveKitchen.isDialogVisible)
            viewModel.onLeaveKitchenDismiss()
            advanceUntilIdle()

            assertFalse(viewModel.uiState.value.leaveKitchen.isDialogVisible)
            assertEquals(0, repository.leaveKitchenCalls)
        }

    @Test
    fun `confirmar sai da cozinha e marca para onde ir`() =
        runTest(testDispatcher) {
            repository.emitUser(supervisorComCozinha.copy(accessType = AccessType.ESTOQUISTA))
            advanceUntilIdle()

            viewModel.onLeaveKitchenClick()
            viewModel.onLeaveKitchenConfirm()
            assertTrue(viewModel.uiState.value.leaveKitchen.isLeaving)
            advanceUntilIdle()

            val leave = viewModel.uiState.value.leaveKitchen
            assertEquals(1, repository.leaveKitchenCalls)
            assertTrue(leave.hasLeft)
            assertFalse(leave.isSupervisor)
            assertEquals("Nenhuma cozinha vinculada", viewModel.uiState.value.cozinhaName)
        }

    @Test
    fun `ultimo supervisor nao sai e ve a mensagem do backend`() =
        runTest(testDispatcher) {
            val lastSupervisor = "Promova outro membro a supervisor antes de sair."
            repository.leaveKitchenResult = Result.failure(Exception(lastSupervisor))
            repository.emitUser(supervisorComCozinha)
            advanceUntilIdle()

            viewModel.onLeaveKitchenConfirm()
            advanceUntilIdle()

            val leave = viewModel.uiState.value.leaveKitchen
            assertFalse(leave.hasLeft)
            assertFalse(leave.isLeaving)
            assertEquals(lastSupervisor, leave.errorMessage)
            assertEquals("Sabor Filial Sul", viewModel.uiState.value.cozinhaName)
        }

    private companion object {
        val supervisorComCozinha =
            SessionUser(
                id = "00000000-0000-0000-0000-000000000001",
                name = "Felipe Kogake",
                email = "felipe@inventra.com",
                accessType = AccessType.SUPERVISOR,
                kitchenId = 7,
                kitchenName = "Sabor Filial Sul",
            )
    }
}
