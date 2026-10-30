package com.inventraoficial.inventra.feature.auth.passwordreset.forgot.ui

import com.inventraoficial.inventra.data.repository.FakePasswordResetRepository
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
class ForgotPasswordViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
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

    private fun createViewModel(email: String = "") = ForgotPasswordViewModel(repository, email)

    @Test
    fun `comeca com o e-mail digitado no login`() {
        val viewModel = createViewModel("maria@inventra.com")

        assertEquals("maria@inventra.com", viewModel.uiState.value.email)
    }

    @Test
    fun `e-mail invalido mostra erro e nao pede o codigo`() =
        runTest(testDispatcher) {
            val viewModel = createViewModel("maria")

            viewModel.onSendClick()
            advanceUntilIdle()

            assertEquals("Informe um e-mail válido", viewModel.uiState.value.errorMessage)
            assertTrue(repository.codeRequests.isEmpty())
        }

    @Test
    fun `e-mail valido pede o codigo sem espacos e abre a tela do codigo`() =
        runTest(testDispatcher) {
            val viewModel = createViewModel()
            viewModel.onEmailChange("  maria@inventra.com ")

            viewModel.onSendClick()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertEquals(listOf("maria@inventra.com"), repository.codeRequests)
            assertEquals("maria@inventra.com", state.codeSentTo)
            assertFalse(state.isLoading)
        }

    @Test
    fun `falha ao pedir o codigo mostra a mensagem e fica na tela`() =
        runTest(testDispatcher) {
            repository.requestCodeResult = Result.failure(Exception("Sem conexão"))
            val viewModel = createViewModel("maria@inventra.com")

            viewModel.onSendClick()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertEquals("Sem conexão", state.errorMessage)
            assertNull(state.codeSentTo)
            assertFalse(state.isLoading)
        }

    @Test
    fun `abrir a tela do codigo limpa o aviso de navegacao`() =
        runTest(testDispatcher) {
            val viewModel = createViewModel("maria@inventra.com")
            viewModel.onSendClick()
            advanceUntilIdle()

            viewModel.onCodeScreenOpened()

            assertNull(viewModel.uiState.value.codeSentTo)
        }

    @Test
    fun `digitar de novo limpa o erro`() {
        val viewModel = createViewModel("maria")
        viewModel.onSendClick()

        viewModel.onEmailChange("maria@")

        assertNull(viewModel.uiState.value.errorMessage)
    }
}
