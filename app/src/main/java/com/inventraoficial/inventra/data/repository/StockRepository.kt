package com.inventraoficial.inventra.data.repository

import com.inventraoficial.inventra.data.remote.api.StockApi
import com.inventraoficial.inventra.data.remote.apiCall
import com.inventraoficial.inventra.data.remote.dto.stock.BatchConsumptionResponse
import com.inventraoficial.inventra.data.remote.dto.stock.ConsumeProductRequest
import com.inventraoficial.inventra.data.remote.dto.stock.KitchenStockItemResponse
import com.inventraoficial.inventra.data.remote.dto.stock.LowStockAlertResponse
import com.inventraoficial.inventra.data.remote.dto.stock.StockBatchResponse
import com.inventraoficial.inventra.data.remote.dto.stock.StockMovementReason
import com.inventraoficial.inventra.data.remote.dto.stock.StockMovementResponse
import com.inventraoficial.inventra.data.remote.dto.stock.StockMovementType
import kotlinx.serialization.json.Json

interface StockRepository {
    suspend fun batches(
        kitchenId: Int,
        productId: Int? = null,
    ): Result<List<StockBatchResponse>>

    suspend fun expiringSoon(
        kitchenId: Int,
        days: Int,
    ): Result<List<StockBatchResponse>>

    suspend fun lowStock(): Result<List<LowStockAlertResponse>>

    suspend fun kitchenStock(kitchenId: Int): Result<List<KitchenStockItemResponse>>

    suspend fun movements(
        kitchenId: Int,
        productId: Int,
        type: StockMovementType,
    ): Result<List<StockMovementResponse>>

    // Baixa por produto: o backend tira primeiro dos lotes que vencem antes.
    suspend fun consume(
        kitchenId: Int,
        productId: Int,
        quantity: Double,
        reason: StockMovementReason,
    ): Result<List<BatchConsumptionResponse>>
}

class NetworkStockRepository(
    private val stockApi: StockApi,
    private val json: Json,
) : StockRepository {
    override suspend fun batches(
        kitchenId: Int,
        productId: Int?,
    ) = apiCall(json) { stockApi.list(kitchenId, productId) }

    override suspend fun expiringSoon(
        kitchenId: Int,
        days: Int,
    ): Result<List<StockBatchResponse>> = apiCall(json) { stockApi.expiringSoon(kitchenId, days) }

    override suspend fun lowStock(): Result<List<LowStockAlertResponse>> = apiCall(json) { stockApi.lowStock() }

    override suspend fun kitchenStock(kitchenId: Int): Result<List<KitchenStockItemResponse>> =
        apiCall(json) { stockApi.kitchenStock(kitchenId) }

    override suspend fun movements(
        kitchenId: Int,
        productId: Int,
        type: StockMovementType,
    ) = apiCall(json) { stockApi.movements(kitchenId, productId, type, MOVEMENTS_LIMIT) }

    override suspend fun consume(
        kitchenId: Int,
        productId: Int,
        quantity: Double,
        reason: StockMovementReason,
    ): Result<List<BatchConsumptionResponse>> =
        apiCall(json) { stockApi.consume(ConsumeProductRequest(kitchenId, productId, quantity, reason)) }

    private companion object {
        const val MOVEMENTS_LIMIT = 30
    }
}
