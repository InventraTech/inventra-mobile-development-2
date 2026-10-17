package com.inventraoficial.inventra.data.remote.api

import com.inventraoficial.inventra.data.remote.dto.stock.BatchConsumptionResponse
import com.inventraoficial.inventra.data.remote.dto.stock.ConsumeProductRequest
import com.inventraoficial.inventra.data.remote.dto.stock.KitchenStockItemResponse
import com.inventraoficial.inventra.data.remote.dto.stock.LowStockAlertResponse
import com.inventraoficial.inventra.data.remote.dto.stock.RegisterStockEntryRequest
import com.inventraoficial.inventra.data.remote.dto.stock.StockBatchResponse
import com.inventraoficial.inventra.data.remote.dto.stock.StockMovementResponse
import com.inventraoficial.inventra.data.remote.dto.stock.StockMovementType
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface StockApi {
    @POST(PATH)
    suspend fun registerEntry(
        @Body request: RegisterStockEntryRequest,
    ): StockBatchResponse

    @GET(PATH)
    suspend fun list(
        @Query("kitchenId") kitchenId: Int,
        @Query("productId") productId: Int? = null,
    ): List<StockBatchResponse>

    @GET("$PATH/expiring-soon")
    suspend fun expiringSoon(
        @Query("kitchenId") kitchenId: Int,
        @Query("days") days: Int,
    ): List<StockBatchResponse>

    @GET("$PATH/low-stock")
    suspend fun lowStock(): List<LowStockAlertResponse>

    @GET("kitchens/{kitchenId}/stock")
    suspend fun kitchenStock(
        @Path("kitchenId") kitchenId: Int,
    ): List<KitchenStockItemResponse>

    @GET("stock-movements")
    suspend fun movements(
        @Query("kitchenId") kitchenId: Int,
        @Query("productId") productId: Int,
        @Query("type") type: StockMovementType,
        @Query("limit") limit: Int,
    ): List<StockMovementResponse>

    @POST("$PATH/consume")
    suspend fun consume(
        @Body request: ConsumeProductRequest,
    ): List<BatchConsumptionResponse>

    companion object {
        private const val PATH = "stock-batches"
    }
}
