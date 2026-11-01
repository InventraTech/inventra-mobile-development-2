package com.inventraoficial.inventra.feature.auth.passwordreset.newpassword.ui

import com.inventraoficial.inventra.data.repository.FakePasswordResetRepository
import com.inventraoficial.inventra.data.repository.ResetTokenExpiredException
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
class NewPasswordViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: FakePasswordResetRepository
    private lateinit var viewModel: NewPasswordViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = FakePasswordResetRepository()
        viewModel = NewPasswordViewModel(repository, "reset-token")
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun preencher(
        senha: String = "NovaSenha@456",
        confirmar: String = senha,
    ) {
        viewModel.onPasswordChange(senha)
        viewModel.onConfirmPasswordChange(confirmar)
    }

    @Test
    fun `campos vazios mostram erro e nao salvam`() =
        runTest(testDispatcher) {
            viewModel.onSaveClick()
            advanceUntilIdle()

            assertEquals("Preencha todos os campos", viewModel.uiState.value.errorMessage)
            assertNull(repository.lastResetToken)
        }

    @Test
    fun `senha curta mostra o minimo de caracteres`() =
        runTest(testDispatcher) {
            preencher(senha = "curta")

            viewModel.onSaveClick()
            advanceUntilIdle()

            assertEquals("A senha deve ter pelo menos 8 caracteres", viewModel.uiState.value.errorMessage)
            assertNull(repository.lastResetToken)
        }

    @Test
    fun `senhas diferentes mostram erro`() =
        runTest(testDispatcher) {
            preencher(senha = "NovaSenha@456", confirmar = "NovaSenha@457")

            viewModel.onSaveClick()
            advanceUntilIdle()

            assertEquals("As senhas não coincidem", viewModel.uiState.value.errorMessage)
        }

    @Test
    fun `salvar manda o token e a senha e mostra a confirmacao`() =
        runTest(testDispatcher) {
            preencher()

            viewModel.onSaveClick()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertEquals("reset-token", repository.lastResetToken)
            assertEquals("NovaSenha@456", repository.lastNewPassword)
            assertTrue(state.isDone)
            assertFalse(state.isLoading)
        }

    @Test
    fun `token vencido pede um novo codigo`() =
        runTest(testDispatcher) {
            repository.resetPasswordResult = Result.failure(ResetTokenExpiredException("Link ou token inválido."))
            preencher()

            viewModel.onSaveClick()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertTrue(state.isTokenExpired)
            assertEquals("Link ou token inválido.", state.errorMessage)
            assertFalse(state.isDone)
        }

    @Test
    fun `outra falha mostra a mensagem e deixa tentar de novo`() =
        runTest(testDispatcher) {
            repository.resetPasswordResult = Result.failure(Exception("Sem conexão"))
            preencher()

            viewModel.onSaveClick()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertEquals("Sem conexão", state.errorMessage)
            assertFalse(state.isTokenExpired)
            assertFalse(state.isLoading)
        }
}
