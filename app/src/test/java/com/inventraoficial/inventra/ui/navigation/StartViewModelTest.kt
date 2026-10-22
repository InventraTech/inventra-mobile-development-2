package com.inventraoficial.inventra.ui.navigation

import com.inventraoficial.inventra.data.local.SessionUser
import com.inventraoficial.inventra.data.remote.dto.auth.AccessType
import com.inventraoficial.inventra.data.repository.FakeAuthRepository
import com.inventraoficial.inventra.data.session.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class StartViewModelTest {
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `comeca sem destino enquanto a sessao nao foi verificada`() =
        runTest(testDispatcher) {
            val viewModel = StartViewModel(FakeAuthRepository(hasSession = true), SessionManager())

            assertNull(viewModel.startScreen.value)
        }

    @Test
    fun `com sessao salva o destino inicial e a Home`() =
        runTest(testDispatcher) {
            val viewModel = StartViewModel(FakeAuthRepository(hasSession = true), SessionManager())

            advanceUntilIdle()

            assertEquals(Screen.Home, viewModel.startScreen.value)
        }

    @Test
    fun `sem sessao o destino inicial e o Login`() =
        runTest(testDispatcher) {
            val viewModel = StartViewModel(FakeAuthRepository(hasSession = false), SessionManager())

            advanceUntilIdle()

            assertEquals(Screen.Login, viewModel.startScreen.value)
        }

    @Test
    fun `repassa para a tela os avisos de sessao expirada`() =
        runTest(testDispatcher) {
            val sessionManager = SessionManager()
            val viewModel = StartViewModel(FakeAuthRepository(hasSession = true), sessionManager)
            val avisos = mutableListOf<Unit>()
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                viewModel.sessionExpired.toList(avisos)
            }

            sessionManager.notifySessionExpired()
            advanceUntilIdle()

            assertEquals(1, avisos.size)
        }

    @Test
    fun `com sessao busca o usuario atualizado no servidor`() =
        runTest(testDispatcher) {
            val repository = FakeAuthRepository(hasSession = true)
            StartViewModel(repository, SessionManager())

            advanceUntilIdle()

            assertEquals(1, repository.refreshCalls)
        }

    @Test
    fun `sem sessao nao busca usuario no servidor`() =
        runTest(testDispatcher) {
            val repository = FakeAuthRepository(hasSession = false)
            StartViewModel(repository, SessionManager())

            advanceUntilIdle()

            assertEquals(0, repository.refreshCalls)
        }

    @Test
    fun `decide o destino antes de esperar a resposta do servidor`() =
        runTest(testDispatcher) {
            val repository = FakeAuthRepository(hasSession = true)
            val viewModel = StartViewModel(repository, SessionManager())

            advanceUntilIdle()

            assertEquals(Screen.Home, viewModel.startScreen.value)
        }

    @Test
    fun `supervisor sem cozinha abre direto em Criar cozinha`() =
        runTest(testDispatcher) {
            val supervisorSemCozinha = sessionUser(AccessType.SUPERVISOR, kitchenId = null)
            val repository = FakeAuthRepository(hasSession = true, initialUser = supervisorSemCozinha)
            val viewModel = StartViewModel(repository, SessionManager())

            advanceUntilIdle()

            assertEquals(Screen.CreateCozinha, viewModel.startScreen.value)
        }

    @Test
    fun `supervisor com cozinha abre na Home`() =
        runTest(testDispatcher) {
            val user = sessionUser(AccessType.SUPERVISOR, kitchenId = 7)
            val repository = FakeAuthRepository(hasSession = true, initialUser = user)
            val viewModel = StartViewModel(repository, SessionManager())

            advanceUntilIdle()

            assertEquals(Screen.Home, viewModel.startScreen.value)
        }

    @Test
    fun `estoquista sem cozinha abre no acompanhamento da solicitacao`() =
        runTest(testDispatcher) {
            val user = sessionUser(AccessType.ESTOQUISTA, kitchenId = null)
            val repository = FakeAuthRepository(hasSession = true, initialUser = user)
            val viewModel = StartViewModel(repository, SessionManager())

            advanceUntilIdle()

            assertEquals(Screen.RequestSent, viewModel.startScreen.value)
        }

    @Test
    fun `comprador sem cozinha abre no acompanhamento da solicitacao`() =
        runTest(testDispatcher) {
            val user = sessionUser(AccessType.COMPRADOR, kitchenId = null)
            val repository = FakeAuthRepository(hasSession = true, initialUser = user)
            val viewModel = StartViewModel(repository, SessionManager())

            advanceUntilIdle()

            assertEquals(Screen.RequestSent, viewModel.startScreen.value)
        }

    @Test
    fun `estoquista com cozinha abre na Home`() =
        runTest(testDispatcher) {
            val user = sessionUser(AccessType.ESTOQUISTA, kitchenId = 7)
            val repository = FakeAuthRepository(hasSession = true, initialUser = user)
            val viewModel = StartViewModel(repository, SessionManager())

            advanceUntilIdle()

            assertEquals(Screen.Home, viewModel.startScreen.value)
        }

    private fun sessionUser(
        accessType: AccessType,
        kitchenId: Int?,
    ) = SessionUser(
        id = "00000000-0000-0000-0000-000000000001",
        name = "Felipe",
        email = "felipe@inventra.com",
        accessType = accessType,
        kitchenId = kitchenId,
        kitchenName = kitchenId?.let { "Cozinha $it" },
    )
}
