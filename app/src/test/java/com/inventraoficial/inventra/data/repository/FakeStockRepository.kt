package com.inventraoficial.inventra.data.repository

import com.inventraoficial.inventra.data.remote.dto.auth.KitchenSummary
import com.inventraoficial.inventra.data.remote.dto.stock.BatchConsumptionResponse
import com.inventraoficial.inventra.data.remote.dto.stock.ConsumeProductRequest
import com.inventraoficial.inventra.data.remote.dto.stock.KitchenStockItemResponse
import com.inventraoficial.inventra.data.remote.dto.stock.LowStockAlertResponse
import com.inventraoficial.inventra.data.remote.dto.stock.ProductSummary
import com.inventraoficial.inventra.data.remote.dto.stock.RegisterStockEntryRequest
import com.inventraoficial.inventra.data.remote.dto.stock.StockBatchResponse
import com.inventraoficial.inventra.data.remote.dto.stock.StockBatchStatus
import com.inventraoficial.inventra.data.remote.dto.stock.StockCategory
import com.inventraoficial.inventra.data.remote.dto.stock.StockMovementReason
import com.inventraoficial.inventra.data.remote.dto.stock.StockMovementResponse
import com.inventraoficial.inventra.data.remote.dto.stock.StockMovementType
import com.inventraoficial.inventra.data.remote.dto.stock.StockUnit

class FakeStockRepository(
    var expiring: List<StockBatchResponse> = emptyList(),
    var lowStock: List<LowStockAlertResponse> = emptyList(),
    var batches: List<StockBatchResponse> = emptyList(),
) : StockRepository {
    var expiringError: Exception? = null
    var lowStockError: Exception? = null
    var batchesError: Exception? = null
    var expiringCalls = mutableListOf<Pair<Int, Int>>()
        private set
    var batchesCalls = mutableListOf<Int>()
        private set

    var batchesProductIds = mutableListOf<Int?>()
        private set

    override suspend fun batches(
        kitchenId: Int,
        productId: Int?,
    ): Result<List<StockBatchResponse>> {
        batchesCalls += kitchenId
        batchesProductIds += productId
        batchesError?.let { return Result.failure(it) }
        return Result.success(if (productId == null) batches else batches.filter { it.product.id == productId })
    }

    var movements: Map<StockMovementType, List<StockMovementResponse>> = emptyMap()
    var movementsError: Exception? = null
    var movementsCalls = mutableListOf<StockMovementType>()
        private set

    override suspend fun movements(
        kitchenId: Int,
        productId: Int,
        type: StockMovementType,
    ): Result<List<StockMovementResponse>> {
        movementsCalls += type
        movementsError?.let { return Result.failure(it) }
        return Result.success(movements[type].orEmpty())
    }

    override suspend fun expiringSoon(
        kitchenId: Int,
        days: Int,
    ): Result<List<StockBatchResponse>> {
        expiringCalls += kitchenId to days
        return expiringError?.let { Result.failure(it) } ?: Result.success(expiring)
    }

    override suspend fun lowStock(): Result<List<LowStockAlertResponse>> =
        lowStockError?.let { Result.failure(it) } ?: Result.success(lowStock)

    var kitchenStock: List<KitchenStockItemResponse> = emptyList()
    var kitchenStockError: Exception? = null
    var kitchenStockCalls = mutableListOf<Int>()
        private set

    override suspend fun kitchenStock(kitchenId: Int): Result<List<KitchenStockItemResponse>> {
        kitchenStockCalls += kitchenId
        return kitchenStockError?.let { Result.failure(it) } ?: Result.success(kitchenStock)
    }

    var consumeResult: Result<List<BatchConsumptionResponse>> = Result.success(emptyList())
    var consumeCalls = mutableListOf<ConsumeProductRequest>()
        private set

    override suspend fun consume(
        kitchenId: Int,
        productId: Int,
        quantity: Double,
        reason: StockMovementReason,
    ): Result<List<BatchConsumptionResponse>> {
        consumeCalls += ConsumeProductRequest(kitchenId, productId, quantity, reason)
        return consumeResult
    }

    var registerEntryError: Exception? = null
    var registerEntryCalls = mutableListOf<RegisterStockEntryRequest>()
        private set

    override suspend fun registerEntry(request: RegisterStockEntryRequest): Result<StockBatchResponse> {
        registerEntryCalls += request
        registerEntryError?.let { return Result.failure(it) }
        val created = batch(id = 99, productName = "Produto ${request.productId}", request.expirationDate.orEmpty())
        return Result.success(
            created.copy(batchNumber = request.batchNumber, initialQuantity = request.initialQuantity),
        )
    }

    companion object {
        @Suppress("LongParameterList")
        fun batch(
            id: Int,
            productName: String,
            expirationDate: String,
            currentQuantity: Double = 1.0,
            unitPrice: Double? = null,
            productId: Int = id,
        ) = StockBatchResponse(
            id = id,
            product = ProductSummary(id = productId, name = productName),
            kitchen = KitchenSummary(id = 7, name = "Sabor Filial Sul"),
            batchNumber = "000$id",
            initialQuantity = currentQuantity,
            currentQuantity = currentQuantity,
            entryDate = "2026-10-01",
            expirationDate = expirationDate,
            unitPrice = unitPrice,
            status = StockBatchStatus.ACTIVE,
        )

        fun stockItem(
            id: Int,
            productName: String,
            categoryName: String?,
            totalQuantity: Double,
            activeBatches: Int,
        ) = KitchenStockItemResponse(
            product = ProductSummary(id = id, name = productName),
            category = categoryName?.let { StockCategory(id = it.length, name = it) },
            unit = StockUnit(id = 1, symbol = "kg"),
            totalQuantity = totalQuantity,
            activeBatches = activeBatches,
        )

        fun lowStockAlert(productName: String) =
            LowStockAlertResponse(
                kitchenId = 7,
                productId = 1,
                product = ProductSummary(id = 1, name = productName),
                currentQuantity = 1.0,
                minStock = 5.0,
            )
    }
}
