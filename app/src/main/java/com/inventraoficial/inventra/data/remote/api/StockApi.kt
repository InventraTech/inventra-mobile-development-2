package com.inventraoficial.inventra.data.remote.api

import com.inventraoficial.inventra.data.remote.dto.stock.LowStockAlertResponse
import com.inventraoficial.inventra.data.remote.dto.stock.StockBatchResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface StockApi {
    @GET("$PATH/expiring-soon")
    suspend fun expiringSoon(
        @Query("kitchenId") kitchenId: Int,
        @Query("days") days: Int,
    ): List<StockBatchResponse>

    @GET("$PATH/low-stock")
    suspend fun lowStock(): List<LowStockAlertResponse>

    companion object {
        private const val PATH = "stock-batches"
    }
}
