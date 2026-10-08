package com.inventraoficial.inventra.feature.auth.register.ui

import com.inventraoficial.inventra.core.designsystem.molecules.InventraUserRole
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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RegisterViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: FakeAuthRepository
    private lateinit var viewModel: RegisterViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeAuthRepository()
        viewModel = RegisterViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun preencherFormulario(
        role: InventraUserRole? = InventraUserRole.Estoquista,
        nome: String = "Felipe",
        email: String = "teste@inventra.com",
        senha: String = "senha1234",
        confirmarSenha: String = senha,
    ) {
        role?.let(viewModel::onRoleSelect)
        viewModel.onNameChange(nome)
        viewModel.onLoginChange(email)
        viewModel.onPasswordChange(senha)
        viewModel.onConfirmPasswordChange(confirmarSenha)
    }

    private fun assertErroSemChamarRepositorio(mensagem: String) {
        assertEquals(mensagem, viewModel.uiState.value.errorMessage)
        assertEquals(0, repository.registerCalls)
    }

    @Test
    fun `campos atualizam o estado`() {
        preencherFormulario(role = InventraUserRole.Supervisor)

        val state = viewModel.uiState.value
        assertEquals(InventraUserRole.Supervisor, state.role)
        assertEquals("Felipe", state.name)
        assertEquals("teste@inventra.com", state.login)
        assertEquals("senha1234", state.password)
        assertEquals("senha1234", state.confirmPassword)
    }

    @Test
    fun `sem cargo pede para escolher um cargo`() {
        preencherFormulario(role = null)

        viewModel.onRegisterClick()

        assertErroSemChamarRepositorio("Escolha um cargo")
    }

    @Test
    fun `sem nome pede para preencher todos os campos`() {
        preencherFormulario(nome = "  ")

        viewModel.onRegisterClick()

        assertErroSemChamarRepositorio("Preencha todos os campos")
    }

    @Test
    fun `senha com menos de 8 caracteres e recusada`() {
        preencherFormulario(senha = "1234567")

        viewModel.onRegisterClick()

        assertErroSemChamarRepositorio("A senha deve ter pelo menos 8 caracteres")
    }

    @Test
    fun `senhas diferentes sao recusadas`() {
        preencherFormulario(senha = "senha1234", confirmarSenha = "senha9999")

        viewModel.onRegisterClick()

        assertErroSemChamarRepositorio("As senhas não coincidem")
    }

    @Test
    fun `cadastro valido liga isLoading enquanto a chamada nao termina`() =
        runTest(testDispatcher) {
            preencherFormulario()

            viewModel.onRegisterClick()

            assertTrue(viewModel.uiState.value.isLoading)
        }

    @Test
    fun `cadastro com sucesso marca isRegistered e envia os dados limpos`() =
        runTest(testDispatcher) {
            preencherFormulario(nome = "  Felipe  ", email = "  teste@inventra.com  ")

            viewModel.onRegisterClick()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertTrue(state.isRegistered)
            assertFalse(state.isLoading)
            assertNull(state.errorMessage)
            assertEquals("Felipe", repository.lastName)
            assertEquals("teste@inventra.com", repository.lastEmail)
            assertEquals("senha1234", repository.lastPassword)
        }

    @Test
    fun `cargo Supervisor e enviado como SUPERVISOR`() =
        runTest(testDispatcher) {
            preencherFormulario(role = InventraUserRole.Supervisor)

            viewModel.onRegisterClick()
            advanceUntilIdle()

            assertEquals(AccessType.SUPERVISOR, repository.lastAccessType)
        }

    @Test
    fun `cargo Estoquista e enviado como ESTOQUISTA`() =
        runTest(testDispatcher) {
            preencherFormulario(role = InventraUserRole.Estoquista)

            viewModel.onRegisterClick()
            advanceUntilIdle()

            assertEquals(AccessType.ESTOQUISTA, repository.lastAccessType)
        }

    @Test
    fun `falha no cadastro mostra a mensagem do repositorio`() =
        runTest(testDispatcher) {
            repository.result = Result.failure(Exception("E-mail já cadastrado."))
            preencherFormulario()

            viewModel.onRegisterClick()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertEquals("E-mail já cadastrado.", state.errorMessage)
            assertFalse(state.isRegistered)
            assertFalse(state.isLoading)
        }
}
