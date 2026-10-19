package com.inventraoficial.inventra.data.repository

import com.inventraoficial.inventra.data.remote.api.StockApi
import com.inventraoficial.inventra.data.remote.dto.stock.BatchConsumptionResponse
import com.inventraoficial.inventra.data.remote.dto.stock.ConsumeProductRequest
import com.inventraoficial.inventra.data.remote.dto.stock.ConsumedBatch
import com.inventraoficial.inventra.data.remote.dto.stock.KitchenStockItemResponse
import com.inventraoficial.inventra.data.remote.dto.stock.LowStockAlertResponse
import com.inventraoficial.inventra.data.remote.dto.stock.RegisterStockEntryRequest
import com.inventraoficial.inventra.data.remote.dto.stock.StockBatchResponse
import com.inventraoficial.inventra.data.remote.dto.stock.StockBatchStatus
import com.inventraoficial.inventra.data.remote.dto.stock.StockMovementReason
import com.inventraoficial.inventra.data.remote.dto.stock.StockMovementResponse
import com.inventraoficial.inventra.data.remote.dto.stock.StockMovementType
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

        var lastListProductId: Int? = null
            private set
        var lastMovements: Triple<Int, StockMovementType, Int>? = null
            private set

        override suspend fun movements(
            kitchenId: Int,
            productId: Int,
            type: StockMovementType,
            limit: Int,
        ): List<StockMovementResponse> {
            error?.let { throw it }
            lastMovements = Triple(productId, type, limit)
            return emptyList()
        }

        override suspend fun list(
            kitchenId: Int,
            productId: Int?,
        ): List<StockBatchResponse> {
            lastListProductId = productId
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

        var lastConsume: ConsumeProductRequest? = null
            private set

        override suspend fun consume(request: ConsumeProductRequest): List<BatchConsumptionResponse> {
            error?.let { throw it }
            lastConsume = request
            return listOf(
                BatchConsumptionResponse(
                    batch = ConsumedBatch(id = 1, batchNumber = "0001", expirationDate = "2026-10-11"),
                    consumedQuantity = request.quantity,
                    remainingQuantity = 0.0,
                    status = StockBatchStatus.WRITTEN_OFF,
                ),
            )
        }

        var lastEntry: RegisterStockEntryRequest? = null
            private set

        override suspend fun registerEntry(request: RegisterStockEntryRequest): StockBatchResponse {
            error?.let { throw it }
            lastEntry = request
            return FakeStockRepository.batch(1, "Leite Integral", request.expirationDate.orEmpty())
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

    @Test
    fun `batches envia o produto quando informado`() =
        runTest {
            val api = FakeStockApi(listOf(batch), listOf(alert))
            val repository = NetworkStockRepository(api, json)

            repository.batches(kitchenId = 7, productId = 5013)

            assertEquals(5013, api.lastListProductId)
        }

    @Test
    fun `movements busca o tipo pedido com limite`() =
        runTest {
            val api = FakeStockApi(listOf(batch), listOf(alert))
            val repository = NetworkStockRepository(api, json)

            val result = repository.movements(kitchenId = 7, productId = 5013, type = StockMovementType.WRITE_OFF)

            assertTrue(result.isSuccess)
            assertEquals(Triple(5013, StockMovementType.WRITE_OFF, 30), api.lastMovements)
        }

    @Test
    fun `consume envia cozinha, produto, quantidade e motivo`() =
        runTest {
            val api = FakeStockApi(listOf(batch), listOf(alert))
            val repository = NetworkStockRepository(api, json)

            val result = repository.consume(7, 5013, 2.5, StockMovementReason.LOSS)

            assertEquals(listOf("0001"), result.getOrNull()?.map { it.batch.batchNumber })
            assertEquals(ConsumeProductRequest(7, 5013, 2.5, StockMovementReason.LOSS), api.lastConsume)
        }

    @Test
    fun `registerEntry envia o lote novo e devolve o lote criado`() =
        runTest {
            val api = FakeStockApi(listOf(batch), listOf(alert))
            val repository = NetworkStockRepository(api, json)
            val request = RegisterStockEntryRequest(5013, 7, "L2026-010", 12.0, "2026-10-17", "2026-11-30")

            val result = repository.registerEntry(request)

            assertEquals("2026-11-30", result.getOrNull()?.expirationDate)
            assertEquals(request, api.lastEntry)
        }

    @Test
    fun `registerEntry com falha de rede devolve falha`() =
        runTest {
            val api = FakeStockApi(emptyList(), emptyList(), error = IOException("sem sinal"))
            val repository = NetworkStockRepository(api, json)
            val request = RegisterStockEntryRequest(5013, 7, "L2026-010", 12.0, "2026-10-17", "2026-11-30")

            assertTrue(repository.registerEntry(request).isFailure)
        }
}
