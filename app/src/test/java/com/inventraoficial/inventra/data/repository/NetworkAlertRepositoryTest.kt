package com.inventraoficial.inventra.data.repository

import com.inventraoficial.inventra.data.remote.api.AlertApi
import com.inventraoficial.inventra.data.remote.dto.alert.AlertResponse
import com.inventraoficial.inventra.data.remote.dto.alert.ReportUnregisteredProductRequest
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class NetworkAlertRepositoryTest {
    private val json = Json { ignoreUnknownKeys = true }
    private val unread = FakeAlertRepository.alert(1, "EXPIRATION", "2026-10-11T13:00:00", "Leite Integral")
    private val read = FakeAlertRepository.alert(2, "STOCK", "2026-10-10T09:00:00", "Óleo de Soja", read = true)

    private class FakeAlertApi(
        private val alerts: List<AlertResponse>,
        private val error: Exception? = null,
    ) : AlertApi {
        var lastList: Pair<Int, Boolean?>? = null
            private set
        var lastReadId: Int? = null
            private set

        override suspend fun list(
            kitchenId: Int,
            unread: Boolean?,
        ): List<AlertResponse> {
            error?.let { throw it }
            lastList = kitchenId to unread
            return if (unread == true) alerts.filter { !it.read } else alerts
        }

        var lastReadAll: Int? = null
            private set

        override suspend fun markAllAsRead(kitchenId: Int) {
            error?.let { throw it }
            lastReadAll = kitchenId
        }

        override suspend fun markAsRead(id: Int): AlertResponse {
            lastReadId = id
            return alerts.first { it.id == id }.copy(read = true)
        }

        var lastReport: ReportUnregisteredProductRequest? = null
            private set

        override suspend fun reportUnregisteredProduct(request: ReportUnregisteredProductRequest): AlertResponse {
            error?.let { throw it }
            lastReport = request
            return FakeAlertRepository.alert(99, "UNREGISTERED_PRODUCT", "2026-10-17T10:00:00")
        }
    }

    @Test
    fun `alerts lista os alertas da cozinha`() =
        runTest {
            val api = FakeAlertApi(listOf(unread, read))
            val repository = NetworkAlertRepository(api, json)

            val result = repository.alerts(kitchenId = 7)

            assertEquals(listOf(unread, read), result.getOrNull())
            assertEquals(7 to null, api.lastList)
        }

    @Test
    fun `unreadCount conta so os nao lidos`() =
        runTest {
            val api = FakeAlertApi(listOf(unread, read))
            val repository = NetworkAlertRepository(api, json)

            val result = repository.unreadCount(kitchenId = 7)

            assertEquals(1, result.getOrNull())
            assertEquals(7 to true, api.lastList)
        }

    @Test
    fun `markAsRead envia o id do alerta`() =
        runTest {
            val api = FakeAlertApi(listOf(unread))
            val repository = NetworkAlertRepository(api, json)

            val result = repository.markAsRead(1)

            assertEquals(1, api.lastReadId)
            assertEquals(true, result.getOrNull()?.read)
        }

    @Test
    fun `falha de rede vira Result de falha`() =
        runTest {
            val api = FakeAlertApi(emptyList(), error = IOException("sem sinal"))
            val repository = NetworkAlertRepository(api, json)

            val result = repository.alerts(kitchenId = 7)

            assertTrue(result.isFailure)
        }

    @Test
    fun `markAllAsRead envia a cozinha`() =
        runTest {
            val api = FakeAlertApi(listOf(unread, read))
            val repository = NetworkAlertRepository(api, json)

            val result = repository.markAllAsRead(kitchenId = 7)

            assertTrue(result.isSuccess)
            assertEquals(7, api.lastReadAll)
        }

    @Test
    fun `reportUnregisteredProduct envia a cozinha e o codigo`() =
        runTest {
            val api = FakeAlertApi(emptyList())
            val repository = NetworkAlertRepository(api, json)

            val result = repository.reportUnregisteredProduct(kitchenId = 7, barcode = "7894900011517")

            assertEquals("UNREGISTERED_PRODUCT", result.getOrNull()?.type)
            assertEquals(ReportUnregisteredProductRequest(7, "7894900011517"), api.lastReport)
        }
}
