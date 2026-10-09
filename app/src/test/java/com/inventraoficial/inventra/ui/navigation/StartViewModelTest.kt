package com.inventraoficial.inventra.ui.navigation

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
}
