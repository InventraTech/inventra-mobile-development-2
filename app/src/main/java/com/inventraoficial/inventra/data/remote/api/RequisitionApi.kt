package com.inventraoficial.inventra.data.remote.api

import com.inventraoficial.inventra.data.remote.dto.requisition.AddRequisitionItemRequest
import com.inventraoficial.inventra.data.remote.dto.requisition.CreateRequisitionRequest
import com.inventraoficial.inventra.data.remote.dto.requisition.RequisitionItemResponse
import com.inventraoficial.inventra.data.remote.dto.requisition.RequisitionReasonRequest
import com.inventraoficial.inventra.data.remote.dto.requisition.RequisitionResponse
import com.inventraoficial.inventra.data.remote.dto.requisition.RequisitionStatus
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface RequisitionApi {
    @GET(PATH)
    suspend fun list(
        @Query("kitchenId") kitchenId: Int,
        @Query("status") status: RequisitionStatus? = null,
        @Query("requesterId") requesterId: String? = null,
    ): List<RequisitionResponse>

    @GET("$PATH/{id}")
    suspend fun find(
        @Path("id") id: Int,
    ): RequisitionResponse

    @GET("$PATH/{id}/items")
    suspend fun items(
        @Path("id") id: Int,
    ): List<RequisitionItemResponse>

    @POST(PATH)
    suspend fun create(
        @Body request: CreateRequisitionRequest,
    ): RequisitionResponse

    @POST("$PATH/{id}/items")
    suspend fun addItem(
        @Path("id") id: Int,
        @Body request: AddRequisitionItemRequest,
    ): RequisitionResponse

    @DELETE("$PATH/{id}/items/{itemId}")
    suspend fun removeItem(
        @Path("id") id: Int,
        @Path("itemId") itemId: Int,
    ): RequisitionResponse

    @PATCH("$PATH/{id}/submit")
    suspend fun submit(
        @Path("id") id: Int,
    ): RequisitionResponse

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
