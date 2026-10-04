package com.inventraoficial.inventra.feature.auth.login.ui

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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: FakeAuthRepository
    private lateinit var viewModel: LoginViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeAuthRepository()
        viewModel = LoginViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun preencherCampos(
        login: String = "teste@inventra.com",
        senha: String = "senha123",
    ) {
        viewModel.onLoginChange(login)
        viewModel.onPasswordChange(senha)
    }

    @Test
    fun `onLoginChange atualiza o login no estado`() {
        viewModel.onLoginChange("felipe")

        assertEquals("felipe", viewModel.uiState.value.login)
    }

    @Test
    fun `onPasswordChange atualiza a senha no estado`() {
        viewModel.onPasswordChange("123456")

        assertEquals("123456", viewModel.uiState.value.password)
    }

    @Test
    fun `onLoginClick com campos vazios preenche errorMessage e nao chama o repositorio`() {
        viewModel.onLoginClick()

        assertEquals("Preencha todos os campos", viewModel.uiState.value.errorMessage)
        assertEquals(0, repository.loginCalls)
    }

    @Test
    fun `onLoginClick com login vazio e senha preenchida ainda preenche errorMessage`() {
        viewModel.onPasswordChange("123456")

        viewModel.onLoginClick()

        assertEquals("Preencha todos os campos", viewModel.uiState.value.errorMessage)
        assertEquals(0, repository.loginCalls)
    }

    @Test
    fun `onLoginClick liga isLoading enquanto a chamada nao termina`() =
        runTest(testDispatcher) {
            preencherCampos()

            viewModel.onLoginClick()

            assertTrue(viewModel.uiState.value.isLoading)
            assertFalse(viewModel.uiState.value.isLoginEnabled)
        }

    @Test
    fun `onLoginClick com sucesso marca isLoggedIn e desliga isLoading`() =
        runTest(testDispatcher) {
            preencherCampos()

            viewModel.onLoginClick()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertTrue(state.isLoggedIn)
            assertFalse(state.isLoading)
            assertNull(state.errorMessage)
        }

    @Test
    fun `onLoginClick envia o email sem espacos nas pontas`() =
        runTest(testDispatcher) {
            preencherCampos(login = "  teste@inventra.com  ", senha = "senha123")

            viewModel.onLoginClick()
            advanceUntilIdle()

            assertEquals("teste@inventra.com", repository.lastEmail)
            assertEquals("senha123", repository.lastPassword)
        }

    @Test
    fun `onLoginClick com credenciais invalidas mostra a mensagem do repositorio`() =
        runTest(testDispatcher) {
            repository.result = Result.failure(Exception("E-mail ou senha inválidos."))
            preencherCampos()

            viewModel.onLoginClick()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertEquals("E-mail ou senha inválidos.", state.errorMessage)
            assertFalse(state.isLoggedIn)
            assertFalse(state.isLoading)
        }

    @Test
    fun `onLoginClick sem conexao mostra a mensagem de erro de rede`() =
        runTest(testDispatcher) {
            val mensagem = "Não foi possível conectar ao servidor. Verifique sua conexão."
            repository.result = Result.failure(Exception(mensagem))
            preencherCampos()

            viewModel.onLoginClick()
            advanceUntilIdle()

            assertEquals(mensagem, viewModel.uiState.value.errorMessage)
            assertFalse(viewModel.uiState.value.isLoggedIn)
        }

    @Test
    fun `nova tentativa apos erro limpa a mensagem anterior`() =
        runTest(testDispatcher) {
            repository.result = Result.failure(Exception("E-mail ou senha inválidos."))
            preencherCampos()
            viewModel.onLoginClick()
            advanceUntilIdle()

            repository.result = Result.success(FakeAuthRepository.fakeUser)
            viewModel.onLoginClick()

            assertNull(viewModel.uiState.value.errorMessage)
            advanceUntilIdle()
            assertTrue(viewModel.uiState.value.isLoggedIn)
        }
}
