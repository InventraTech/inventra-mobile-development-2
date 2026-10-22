package com.inventraoficial.inventra.feature.notifications.background

import com.inventraoficial.inventra.data.local.FakeAppPreferences
import com.inventraoficial.inventra.data.local.SessionUser
import com.inventraoficial.inventra.data.remote.dto.alert.AlertResponse
import com.inventraoficial.inventra.data.remote.dto.auth.AccessType
import com.inventraoficial.inventra.data.repository.FakeAlertRepository
import com.inventraoficial.inventra.data.repository.FakeAuthRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NewAlertsCheckerTest {
    private class FakeStore(
        var saved: MutableMap<String, Int> = mutableMapOf(),
    ) : LastNotifiedAlertStore {
        override suspend fun lastNotifiedId(userId: String): Int? = saved[userId]

        override suspend fun save(
            userId: String,
            alertId: Int,
        ) {
            saved[userId] = alertId
        }
    }

    private class RecordingNotifier : AlertNotifier {
        val shown = mutableListOf<List<AlertResponse>>()

        override fun show(alerts: List<AlertResponse>) {
            shown += alerts
        }

        override fun showAccessDecision(
            cozinhaName: String,
            approved: Boolean,
        ) = Unit
    }

    private val alertRepository = FakeAlertRepository()
    private val store = FakeStore()
    private val notifier = RecordingNotifier()

    private fun alert(
        id: Int,
        read: Boolean = false,
    ) = FakeAlertRepository.alert(id, "STOCK", "2026-10-17T10:00:00", "Arroz Camil", read)

    private fun checker(
        kitchenId: Int? = 7,
        enabled: Boolean = true,
        loggedIn: Boolean = true,
    ): NewAlertsChecker {
        val user = SessionUser("1", "Mauro", "mauro@inventra.com", AccessType.ESTOQUISTA, kitchenId)
        return NewAlertsChecker(
            authRepository = FakeAuthRepository(initialUser = user.takeIf { loggedIn }),
            alertRepository = alertRepository,
            appPreferences = FakeAppPreferences(alertNotifications = enabled),
            store = store,
            notifier = notifier,
        )
    }

    @Test
    fun `na primeira vez so marca os alertas que ja existiam, sem avisar`() =
        runTest {
            alertRepository.alerts = listOf(alert(3), alert(5))

            val result = checker().check()

            assertEquals(0, result.getOrNull())
            assertTrue(notifier.shown.isEmpty())
            assertEquals(5, store.saved["1"])
        }

    @Test
    fun `na primeira vez sem alertas comeca do zero e avisa o proximo`() =
        runTest {
            checker().check()
            assertEquals(0, store.saved["1"])

            alertRepository.alerts = listOf(alert(1))
            val result = checker().check()

            assertEquals(1, result.getOrNull())
            assertEquals(listOf(listOf(alert(1))), notifier.shown)
        }

    @Test
    fun `avisa so os nao lidos mais novos que o ultimo avisado, do mais antigo para o mais novo`() =
        runTest {
            store.saved["1"] = 5
            alertRepository.alerts = listOf(alert(9), alert(4), alert(7), alert(8, read = true))

            val result = checker().check()

            assertEquals(2, result.getOrNull())
            assertEquals(listOf(listOf(alert(7), alert(9))), notifier.shown)
            assertEquals(9, store.saved["1"])
            assertEquals(listOf(7), alertRepository.unreadAlertsCalls)
        }

    @Test
    fun `sem nada novo nao avisa de novo`() =
        runTest {
            store.saved["1"] = 9
            alertRepository.alerts = listOf(alert(9))

            checker().check()

            assertTrue(notifier.shown.isEmpty())
        }

    @Test
    fun `avisos desligados, sem cozinha ou sem sessao nao consultam a API`() =
        runTest {
            checker(enabled = false).check()
            checker(kitchenId = null).check()
            checker(loggedIn = false).check()

            assertTrue(alertRepository.unreadAlertsCalls.isEmpty())
            assertTrue(notifier.shown.isEmpty())
        }

    @Test
    fun `falha na API devolve falha e nao mexe no ultimo avisado`() =
        runTest {
            store.saved["1"] = 5
            alertRepository.error = Exception("Sem conexão")

            val result = checker().check()

            assertTrue(result.isFailure)
            assertEquals(5, store.saved["1"])
            assertTrue(notifier.shown.isEmpty())
        }
}
