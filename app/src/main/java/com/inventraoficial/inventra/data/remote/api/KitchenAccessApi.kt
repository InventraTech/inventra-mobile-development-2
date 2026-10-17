package com.inventraoficial.inventra.data.remote.api

import com.inventraoficial.inventra.data.remote.dto.kitchenaccess.CreateKitchenAccessRequest
import com.inventraoficial.inventra.data.remote.dto.kitchenaccess.KitchenAccessRequestResponse
import com.inventraoficial.inventra.data.remote.dto.kitchenaccess.KitchenAccessRequestStatus
import com.inventraoficial.inventra.data.remote.dto.kitchenaccess.RejectKitchenAccessRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface KitchenAccessApi {
    @GET(PATH)
    suspend fun list(
        @Query("status") status: KitchenAccessRequestStatus?,
    ): List<KitchenAccessRequestResponse>

    @POST("$PATH/{id}/approve")
    suspend fun approve(
        @Path("id") id: String,
    ): KitchenAccessRequestResponse

    @POST("$PATH/{id}/reject")
    suspend fun reject(
        @Path("id") id: String,
        @Body request: RejectKitchenAccessRequest,
    ): KitchenAccessRequestResponse

    @POST(PATH)
    suspend fun create(
        @Body request: CreateKitchenAccessRequest,
    ): KitchenAccessRequestResponse

    @GET("$PATH/mine")
    suspend fun mine(): KitchenAccessRequestResponse

    companion object {
        private const val PATH = "kitchen-access-requests"
    }
}
