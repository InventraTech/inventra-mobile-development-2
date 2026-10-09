package com.inventraoficial.inventra.data.session

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SessionManagerTest {
    @Test
    fun `quem esta ouvindo recebe o aviso de sessao expirada`() =
        runTest {
            val sessionManager = SessionManager()
            val avisos = mutableListOf<Unit>()
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                sessionManager.sessionExpired.toList(avisos)
            }

            sessionManager.notifySessionExpired()
            advanceUntilIdle()

            assertEquals(1, avisos.size)
        }

    @Test
    fun `cada 401 gera um aviso novo`() =
        runTest {
            val sessionManager = SessionManager()
            val avisos = mutableListOf<Unit>()
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                sessionManager.sessionExpired.toList(avisos)
            }

            sessionManager.notifySessionExpired()
            sessionManager.notifySessionExpired()
            advanceUntilIdle()

            assertEquals(2, avisos.size)
        }

    @Test
    fun `aviso sem ninguem ouvindo e descartado e nao chega para quem ouvir depois`() =
        runTest {
            val sessionManager = SessionManager()

            // Ninguem ouvindo: o aviso se perde (por isso o token e apagado antes, no interceptor).
            sessionManager.notifySessionExpired()

            val avisos = mutableListOf<Unit>()
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                sessionManager.sessionExpired.toList(avisos)
            }
            advanceUntilIdle()

            // SharedFlow nao guarda "valor atual": quem chega depois nao recebe aviso antigo.
            assertTrue(avisos.isEmpty())
        }
}
