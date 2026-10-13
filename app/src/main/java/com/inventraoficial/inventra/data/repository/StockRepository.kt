package com.inventraoficial.inventra.data.repository

import com.inventraoficial.inventra.data.remote.api.StockApi
import com.inventraoficial.inventra.data.remote.apiCall
import com.inventraoficial.inventra.data.remote.dto.stock.KitchenStockItemResponse
import com.inventraoficial.inventra.data.remote.dto.stock.LowStockAlertResponse
import com.inventraoficial.inventra.data.remote.dto.stock.StockBatchResponse
import kotlinx.serialization.json.Json

interface StockRepository {
    suspend fun expiringSoon(
        kitchenId: Int,
        days: Int,
    ): Result<List<StockBatchResponse>>

    suspend fun lowStock(): Result<List<LowStockAlertResponse>>

    suspend fun kitchenStock(kitchenId: Int): Result<List<KitchenStockItemResponse>>
}

class NetworkStockRepository(
    private val stockApi: StockApi,
    private val json: Json,
) : StockRepository {
    override suspend fun expiringSoon(
        kitchenId: Int,
        days: Int,
    ): Result<List<StockBatchResponse>> = apiCall(json) { stockApi.expiringSoon(kitchenId, days) }

    override suspend fun lowStock(): Result<List<LowStockAlertResponse>> = apiCall(json) { stockApi.lowStock() }

    override suspend fun kitchenStock(kitchenId: Int): Result<List<KitchenStockItemResponse>> =
        apiCall(json) { stockApi.kitchenStock(kitchenId) }
}
