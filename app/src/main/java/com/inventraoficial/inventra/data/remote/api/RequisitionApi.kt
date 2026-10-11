package com.inventraoficial.inventra.data.remote.api

import com.inventraoficial.inventra.data.remote.dto.requisition.RequisitionItemResponse
import com.inventraoficial.inventra.data.remote.dto.requisition.RequisitionReasonRequest
import com.inventraoficial.inventra.data.remote.dto.requisition.RequisitionResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path
import retrofit2.http.Query

interface RequisitionApi {
    @GET(PATH)
    suspend fun list(
        @Query("kitchenId") kitchenId: Int,
    ): List<RequisitionResponse>

    @GET("$PATH/{id}/items")
    suspend fun items(
        @Path("id") id: Int,
    ): List<RequisitionItemResponse>

    @PATCH("$PATH/{id}/approve")
    suspend fun approve(
        @Path("id") id: Int,
    ): RequisitionResponse

    @PATCH("$PATH/{id}/reject")
    suspend fun reject(
        @Path("id") id: Int,
        @Body request: RequisitionReasonRequest,
    ): RequisitionResponse

    @PATCH("$PATH/{id}/cancel")
    suspend fun cancel(
        @Path("id") id: Int,
        @Body request: RequisitionReasonRequest,
    ): RequisitionResponse

    companion object {
        private const val PATH = "requisitions"
    }
}
