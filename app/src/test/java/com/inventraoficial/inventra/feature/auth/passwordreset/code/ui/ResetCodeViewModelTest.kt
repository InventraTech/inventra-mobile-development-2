package com.inventraoficial.inventra.feature.auth.passwordreset.code.ui

import com.inventraoficial.inventra.data.repository.FakePasswordResetRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
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
class ResetCodeViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private val email = "maria@inventra.com"
    private lateinit var repository: FakePasswordResetRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = FakePasswordResetRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = ResetCodeViewModel(repository, email)

    @Test
    fun `comeca contando 60 segundos para reenviar`() =
        runTest(testDispatcher) {
            val viewModel = createViewModel()
            runCurrent()

            val state = viewModel.uiState.value
            assertEquals(ResetCodeViewModel.RESEND_SECONDS, state.resendInSeconds)
            assertFalse(state.canResend)
        }

    @Test
    fun `depois dos 60 segundos libera o reenvio`() =
        runTest(testDispatcher) {
            val viewModel = createViewModel()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertEquals(0, state.resendInSeconds)
            assertTrue(state.canResend)
        }

    @Test
    fun `codigo aceita so 6 numeros`() {
        val viewModel = createViewModel()

        viewModel.onCodeChange("48a29 1573")

        assertEquals("482915", viewModel.uiState.value.code)
    }

    @Test
    fun `codigo incompleto mostra erro e nao confere`() =
        runTest(testDispatcher) {
            val viewModel = createViewModel()
            viewModel.onCodeChange("4829")

            viewModel.onConfirmClick()
            runCurrent()

            assertEquals("Digite os 6 números do código", viewModel.uiState.value.errorMessage)
            assertNull(repository.lastCode)
        }

    @Test
    fun `codigo certo guarda o token para abrir a nova senha`() =
        runTest(testDispatcher) {
            val viewModel = createViewModel()
            viewModel.onCodeChange("482915")

            viewModel.onConfirmClick()
            runCurrent()

            val state = viewModel.uiState.value
            assertEquals("reset-token", state.resetToken)
            assertEquals(email, repository.lastEmail)
            assertEquals("482915", repository.lastCode)
            assertFalse(state.isLoading)
        }

    @Test
    fun `codigo errado mostra a mensagem e limpa o campo`() =
        runTest(testDispatcher) {
            repository.verifyResult = Result.failure(Exception("Código inválido ou expirado."))
            val viewModel = createViewModel()
            viewModel.onCodeChange("000000")

            viewModel.onConfirmClick()
            runCurrent()

            val state = viewModel.uiState.value
            assertEquals("Código inválido ou expirado.", state.errorMessage)
            assertEquals("", state.code)
            assertNull(state.resetToken)
        }

    @Test
    fun `reenviar antes do tempo nao faz nada`() =
        runTest(testDispatcher) {
            val viewModel = createViewModel()
            runCurrent()

            viewModel.onResendClick()
            runCurrent()

            assertTrue(repository.codeRequests.isEmpty())
        }

    @Test
    fun `reenviar depois do tempo pede outro codigo e volta a contar`() =
        runTest(testDispatcher) {
            val viewModel = createViewModel()
            advanceUntilIdle()
            viewModel.onCodeChange("1234")

            viewModel.onResendClick()
            runCurrent()

            val state = viewModel.uiState.value
            assertEquals(listOf(email), repository.codeRequests)
            assertEquals("Enviamos um novo código para $email.", state.infoMessage)
            assertEquals("", state.code)
            assertEquals(ResetCodeViewModel.RESEND_SECONDS, state.resendInSeconds)
        }

    @Test
    fun `falha ao reenviar mostra a mensagem e deixa tentar de novo`() =
        runTest(testDispatcher) {
            repository.requestCodeResult = Result.failure(Exception("Sem conexão"))
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.onResendClick()
            runCurrent()

            val state = viewModel.uiState.value
            assertEquals("Sem conexão", state.errorMessage)
            assertTrue(state.canResend)
        }

    @Test
    fun `abrir a nova senha limpa o token da tela`() =
        runTest(testDispatcher) {
            val viewModel = createViewModel()
            viewModel.onCodeChange("482915")
            viewModel.onConfirmClick()
            runCurrent()

            viewModel.onNewPasswordOpened()

            assertNull(viewModel.uiState.value.resetToken)
        }
}
