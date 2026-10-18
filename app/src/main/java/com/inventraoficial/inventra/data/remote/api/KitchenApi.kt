package com.inventraoficial.inventra.data.remote.api

import com.inventraoficial.inventra.data.remote.dto.kitchen.CreateKitchenRequest
import com.inventraoficial.inventra.data.remote.dto.kitchen.DiscoverKitchenPage
import com.inventraoficial.inventra.data.remote.dto.kitchen.KitchenResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
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

    companion object {
        private const val PATH = "kitchens"
    }
}
