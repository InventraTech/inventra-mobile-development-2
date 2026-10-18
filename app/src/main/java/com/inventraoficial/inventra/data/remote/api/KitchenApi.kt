package com.inventraoficial.inventra.data.remote.api

import com.inventraoficial.inventra.data.remote.dto.kitchen.CreateKitchenRequest
import com.inventraoficial.inventra.data.remote.dto.kitchen.DiscoverKitchenPage
import com.inventraoficial.inventra.data.remote.dto.kitchen.KitchenResponse
import com.inventraoficial.inventra.data.remote.dto.kitchen.UpdateKitchenRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface KitchenApi {
    @POST(PATH)
    suspend fun create(
        @Body request: CreateKitchenRequest,
    ): KitchenResponse

    @GET("$PATH/discover")
    suspend fun discover(
        @Query("q") query: String?,
        @Query("lat") latitude: Double?,
        @Query("lng") longitude: Double?,
        @Query("size") size: Int,
    ): DiscoverKitchenPage

    @GET("$PATH/{id}")
    suspend fun find(
        @Path("id") id: Int,
    ): KitchenResponse

    @PUT("$PATH/{id}")
    suspend fun update(
        @Path("id") id: Int,
        @Body request: UpdateKitchenRequest,
    ): KitchenResponse

    companion object {
        private const val PATH = "kitchens"
    }
}
