package com.inventraoficial.inventra.feature.profile.details.ui

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

            // a coroutine ainda nao rodou: nada de navegar antes de apagar o token
            assertFalse(viewModel.uiState.value.isLoggedOut)
            assertEquals(0, repository.logoutCalls)
        }
}
