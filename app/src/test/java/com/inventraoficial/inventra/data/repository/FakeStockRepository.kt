package com.inventraoficial.inventra.data.repository

import com.inventraoficial.inventra.data.remote.dto.auth.KitchenSummary
import com.inventraoficial.inventra.data.remote.dto.stock.KitchenStockItemResponse
import com.inventraoficial.inventra.data.remote.dto.stock.LowStockAlertResponse
import com.inventraoficial.inventra.data.remote.dto.stock.ProductSummary
import com.inventraoficial.inventra.data.remote.dto.stock.StockBatchResponse
import com.inventraoficial.inventra.data.remote.dto.stock.StockBatchStatus
import com.inventraoficial.inventra.data.remote.dto.stock.StockCategory
import com.inventraoficial.inventra.data.remote.dto.stock.StockUnit

class FakeStockRepository(
    var expiring: List<StockBatchResponse> = emptyList(),
    var lowStock: List<LowStockAlertResponse> = emptyList(),
) : StockRepository {
    var expiringError: Exception? = null
    var lowStockError: Exception? = null
    var expiringCalls = mutableListOf<Pair<Int, Int>>()
        private set

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

    companion object {
        fun batch(
            id: Int,
            productName: String,
            expirationDate: String,
            currentQuantity: Double = 1.0,
            unitPrice: Double? = null,
        ) = StockBatchResponse(
            id = id,
            product = ProductSummary(id = id, name = productName),
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
