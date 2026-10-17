package com.inventraoficial.inventra.feature.auth.google.ui

import com.inventraoficial.inventra.core.designsystem.molecules.InventraUserRole
import com.inventraoficial.inventra.data.remote.auth.google.GoogleSignInCancelledException
import com.inventraoficial.inventra.data.remote.dto.auth.AccessType
import com.inventraoficial.inventra.data.remote.dto.auth.KitchenSummary
import com.inventraoficial.inventra.data.remote.dto.auth.ProfileSummary
import com.inventraoficial.inventra.data.repository.FakeAuthRepository
import com.inventraoficial.inventra.data.repository.GoogleRegistrationRequiredException
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
class GoogleSignInViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: FakeAuthRepository
    private lateinit var viewModel: GoogleSignInViewModel

    private val registrationRequired =
        Result.failure<Nothing>(GoogleRegistrationRequiredException(name = "Felipe Kogake", email = "felipe@gmail.com"))

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeAuthRepository()
        viewModel = GoogleSignInViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun clicarNoGoogle(token: Result<String> = Result.success("firebase-id-token")) {
        viewModel.onGoogleClick { token }
    }

    @Test
    fun `conta existente entra e envia o token sem cargo`() =
        runTest {
            clicarNoGoogle()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertTrue(state.isLoggedIn)
            assertFalse(state.isLoading)
            assertEquals("firebase-id-token", repository.lastIdToken)
            assertNull(repository.lastAccessType)
        }

    @Test
    fun `supervisor sem cozinha precisa criar a cozinha depois de entrar`() =
        runTest {
            clicarNoGoogle()
            advanceUntilIdle()

            assertTrue(viewModel.uiState.value.mustCreateKitchen)
        }

    @Test
    fun `usuario com cozinha nao precisa criar a cozinha`() =
        runTest {
            repository.result =
                Result.success(FakeAuthRepository.fakeUser.copy(kitchen = KitchenSummary(id = 7, name = "Sabor Sul")))

            clicarNoGoogle()
            advanceUntilIdle()

            assertFalse(viewModel.uiState.value.mustCreateKitchen)
            assertFalse(viewModel.uiState.value.mustJoinKitchen)
        }

    @Test
    fun `estoquista sem cozinha precisa pedir entrada numa cozinha depois de entrar`() =
        runTest {
            val estoquista = ProfileSummary(id = 2, accessType = AccessType.ESTOQUISTA)
            repository.result = Result.success(FakeAuthRepository.fakeUser.copy(profile = estoquista))

            clicarNoGoogle()
            advanceUntilIdle()

            assertTrue(viewModel.uiState.value.mustJoinKitchen)
            assertFalse(viewModel.uiState.value.mustCreateKitchen)
        }

    @Test
    fun `mostra o carregando enquanto busca o token`() =
        runTest {
            clicarNoGoogle()

            assertTrue(viewModel.uiState.value.isLoading)
            assertTrue(viewModel.uiState.value.isButtonLoading)
        }

    @Test
    fun `cancelar a escolha da conta nao mostra erro nem chama o backend`() =
        runTest {
            clicarNoGoogle(Result.failure(GoogleSignInCancelledException(RuntimeException())))
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertNull(state.errorMessage)
            assertFalse(state.isLoading)
            assertEquals(0, repository.googleCalls)
        }

    @Test
    fun `falha ao buscar o token mostra a mensagem`() =
        runTest {
            clicarNoGoogle(Result.failure(Exception("Nenhuma conta Google encontrada neste aparelho.")))
            advanceUntilIdle()

            assertEquals("Nenhuma conta Google encontrada neste aparelho.", viewModel.uiState.value.errorMessage)
            assertEquals(0, repository.googleCalls)
        }

    @Test
    fun `erro do backend aparece na tela`() =
        runTest {
            repository.result = Result.failure(Exception("Conta desativada."))

            clicarNoGoogle()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertEquals("Conta desativada.", state.screenErrorMessage)
            assertFalse(state.isLoggedIn)
        }

    @Test
    fun `primeiro acesso abre a escolha de cargo com o nome e o email`() =
        runTest {
            repository.result = registrationRequired

            clicarNoGoogle()
            advanceUntilIdle()

            val signUp = viewModel.uiState.value.pendingSignUp
            assertEquals("firebase-id-token", signUp?.idToken)
            assertEquals("Felipe", signUp?.firstName)
            assertEquals("felipe@gmail.com", signUp?.email)
            assertFalse(viewModel.uiState.value.isLoggedIn)
            assertFalse(viewModel.uiState.value.isButtonLoading)
        }

    @Test
    fun `confirmar sem cargo pede para escolher`() =
        runTest {
            repository.result = registrationRequired
            clicarNoGoogle()
            advanceUntilIdle()

            viewModel.onConfirmSignUp()

            assertEquals("Escolha um cargo", viewModel.uiState.value.errorMessage)
            assertEquals(1, repository.googleCalls)
        }

    @Test
    fun `erro dentro da escolha de cargo nao aparece na tela de tras`() =
        runTest {
            repository.result = registrationRequired
            clicarNoGoogle()
            advanceUntilIdle()

            viewModel.onConfirmSignUp()

            assertNull(viewModel.uiState.value.screenErrorMessage)
        }

    @Test
    fun `confirmar o cargo reenvia o mesmo token com o cargo e entra`() =
        runTest {
            repository.result = registrationRequired
            clicarNoGoogle()
            advanceUntilIdle()
            repository.result = Result.success(FakeAuthRepository.fakeUser)

            viewModel.onRoleSelect(InventraUserRole.Estoquista)
            viewModel.onConfirmSignUp()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertEquals(AccessType.ESTOQUISTA, repository.lastAccessType)
            assertEquals("firebase-id-token", repository.lastIdToken)
            assertTrue(state.isLoggedIn)
            assertNull(state.pendingSignUp)
        }

    @Test
    fun `fechar a escolha de cargo limpa o cadastro pendente`() =
        runTest {
            repository.result = registrationRequired
            clicarNoGoogle()
            advanceUntilIdle()

            viewModel.onDismissSignUp()

            assertNull(viewModel.uiState.value.pendingSignUp)
            assertNull(viewModel.uiState.value.errorMessage)
        }

    @Test
    fun `segundo clique enquanto carrega e ignorado`() =
        runTest {
            clicarNoGoogle()
            clicarNoGoogle()
            advanceUntilIdle()

            assertEquals(1, repository.googleCalls)
        }
}
