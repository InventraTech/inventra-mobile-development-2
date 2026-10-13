package com.inventraoficial.inventra.data.repository

import com.inventraoficial.inventra.data.remote.api.StockApi
import com.inventraoficial.inventra.data.remote.dto.stock.KitchenStockItemResponse
import com.inventraoficial.inventra.data.remote.dto.stock.LowStockAlertResponse
import com.inventraoficial.inventra.data.remote.dto.stock.StockBatchResponse
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class NetworkStockRepositoryTest {
    private val json = Json { ignoreUnknownKeys = true }
    private val batch = FakeStockRepository.batch(1, "Leite Integral", "2026-10-11")
    private val alert = FakeStockRepository.lowStockAlert("Arroz Camil")

    private class FakeStockApi(
        private val batches: List<StockBatchResponse>,
        private val alerts: List<LowStockAlertResponse>,
        private val error: Exception? = null,
    ) : StockApi {
        var lastQuery: Pair<Int, Int>? = null
            private set
        var lastKitchenId: Int? = null
            private set

        override suspend fun list(kitchenId: Int): List<StockBatchResponse> {
            error?.let { throw it }
            lastKitchenId = kitchenId
            return batches
        }

        override suspend fun expiringSoon(
            kitchenId: Int,
            days: Int,
        ): List<StockBatchResponse> {
            error?.let { throw it }
            lastQuery = kitchenId to days
            return batches
        }

        override suspend fun lowStock(): List<LowStockAlertResponse> {
            error?.let { throw it }
            return alerts
        }

        var lastKitchenStock: Int? = null
            private set

        override suspend fun kitchenStock(kitchenId: Int): List<KitchenStockItemResponse> {
            error?.let { throw it }
            lastKitchenStock = kitchenId
            return listOf(FakeStockRepository.stockItem(1, "Arroz Camil", "Grãos", 24.0, 2))
        }
    }

    @Test
    fun `expiringSoon envia a cozinha e os dias`() =
        runTest {
            val api = FakeStockApi(listOf(batch), listOf(alert))
            val repository = NetworkStockRepository(api, json)

            val result = repository.expiringSoon(kitchenId = 7, days = 7)

            assertEquals(listOf(batch), result.getOrNull())
            assertEquals(7 to 7, api.lastQuery)
        }

    @Test
    fun `batches lista os lotes da cozinha`() =
        runTest {
            val api = FakeStockApi(listOf(batch), listOf(alert))
            val repository = NetworkStockRepository(api, json)

            val result = repository.batches(kitchenId = 7)

            assertEquals(listOf(batch), result.getOrNull())
            assertEquals(7, api.lastKitchenId)
        }

    @Test
    fun `lowStock lista os produtos abaixo do minimo`() =
        runTest {
            val repository = NetworkStockRepository(FakeStockApi(listOf(batch), listOf(alert)), json)

            val result = repository.lowStock()

            assertEquals(listOf(alert), result.getOrNull())
        }

    @Test
    fun `falha de rede vira Result de falha`() =
        runTest {
            val api = FakeStockApi(emptyList(), emptyList(), error = IOException("sem sinal"))
            val repository = NetworkStockRepository(api, json)

            val result = repository.expiringSoon(kitchenId = 7, days = 7)

            assertTrue(result.isFailure)
        }

    @Test
    fun `kitchenStock busca o resumo da cozinha`() =
        runTest {
            val api = FakeStockApi(listOf(batch), listOf(alert))
            val repository = NetworkStockRepository(api, json)

            val result = repository.kitchenStock(kitchenId = 7)

            assertEquals(listOf("Arroz Camil"), result.getOrNull()?.map { it.product.name })
            assertEquals(7, api.lastKitchenStock)
        }
}
