package com.inventraoficial.inventra.feature.notifications.background

import com.inventraoficial.inventra.data.local.FakeAppPreferences
import com.inventraoficial.inventra.data.local.SessionUser
import com.inventraoficial.inventra.data.remote.dto.alert.AlertResponse
import com.inventraoficial.inventra.data.remote.dto.auth.AccessType
import com.inventraoficial.inventra.data.remote.dto.kitchenaccess.KitchenAccessRequestStatus
import com.inventraoficial.inventra.data.repository.FakeAuthRepository
import com.inventraoficial.inventra.data.repository.FakeKitchenAccessRepository
import com.inventraoficial.inventra.data.repository.FakeMembersRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AccessRequestCheckerTest {
    private class FakeStore : KnownAccessRequestStore {
        val saved = mutableMapOf<String, String>()

        override suspend fun known(userId: String): String? = saved[userId]

        override suspend fun save(
            userId: String,
            request: String,
        ) {
            saved[userId] = request
        }
    }

    private class RecordingNotifier : AlertNotifier {
        val decisions = mutableListOf<Pair<String, Boolean>>()

        override fun show(alerts: List<AlertResponse>) = Unit

        override fun showAccessDecision(
            cozinhaName: String,
            approved: Boolean,
        ) {
            decisions += cozinhaName to approved
        }
    }

    private val pending = FakeMembersRepository.pendingRequest
    private val kitchenAccessRepository = FakeKitchenAccessRepository()
    private val store = FakeStore()
    private val notifier = RecordingNotifier()
    private lateinit var authRepository: FakeAuthRepository

    private fun checker(
        accessType: AccessType = AccessType.ESTOQUISTA,
        kitchenId: Int? = null,
        enabled: Boolean = true,
    ): AccessRequestChecker {
        val user = SessionUser("1", "Mauro", "mauro@inventra.com", accessType, kitchenId)
        authRepository = FakeAuthRepository(initialUser = user)
        return AccessRequestChecker(
            authRepository = authRepository,
            kitchenAccessRepository = kitchenAccessRepository,
            appPreferences = FakeAppPreferences(alertNotifications = enabled),
            store = store,
            notifier = notifier,
        )
    }

    @Test
    fun `primeira vez so guarda o pedido, sem avisar`() =
        runTest {
            kitchenAccessRepository.myRequestResult =
                Result.success(pending.copy(status = KitchenAccessRequestStatus.APPROVED))

            checker().check()

            assertTrue(notifier.decisions.isEmpty())
            assertEquals("req-1:APPROVED", store.saved["1"])
        }

    @Test
    fun `pendente que foi aprovado avisa e atualiza a sessao`() =
        runTest {
            store.saved["1"] = "req-1:PENDING"
            kitchenAccessRepository.myRequestResult =
                Result.success(pending.copy(status = KitchenAccessRequestStatus.APPROVED))

            checker().check()

            assertEquals(listOf("Sabor Filial Sul" to true), notifier.decisions)
            assertEquals(1, authRepository.refreshCalls)
            assertEquals("req-1:APPROVED", store.saved["1"])
        }

    @Test
    fun `pendente que foi recusado avisa sem atualizar a sessao`() =
        runTest {
            store.saved["1"] = "req-1:PENDING"
            kitchenAccessRepository.myRequestResult =
                Result.success(pending.copy(status = KitchenAccessRequestStatus.REJECTED))

            checker().check()

            assertEquals(listOf("Sabor Filial Sul" to false), notifier.decisions)
            assertEquals(0, authRepository.refreshCalls)
        }

    @Test
    fun `sem mudanca nao avisa de novo`() =
        runTest {
            store.saved["1"] = "req-1:PENDING"

            checker().check()

            assertTrue(notifier.decisions.isEmpty())
        }

    @Test
    fun `pedido novo de outra cozinha nao conta como resposta`() =
        runTest {
            store.saved["1"] = "req-0:PENDING"
            kitchenAccessRepository.myRequestResult =
                Result.success(pending.copy(status = KitchenAccessRequestStatus.REJECTED))

            checker().check()

            assertTrue(notifier.decisions.isEmpty())
            assertEquals("req-1:REJECTED", store.saved["1"])
        }

    @Test
    fun `quem ja tem cozinha, supervisor ou avisos desligados nao consulta o pedido`() =
        runTest {
            checker(kitchenId = 7).check()
            checker(accessType = AccessType.SUPERVISOR).check()
            checker(enabled = false).check()

            assertEquals(0, kitchenAccessRepository.myRequestCalls)
        }

    @Test
    fun `falha na API devolve falha e nao mexe no pedido guardado`() =
        runTest {
            store.saved["1"] = "req-1:PENDING"
            kitchenAccessRepository.myRequestResult = Result.failure(Exception("Sem conexão"))

            val result = checker().check()

            assertTrue(result.isFailure)
            assertEquals("req-1:PENDING", store.saved["1"])
        }
}
