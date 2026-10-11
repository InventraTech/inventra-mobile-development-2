package com.inventraoficial.inventra.data.remote.api

import com.inventraoficial.inventra.data.remote.dto.kitchen.CreateKitchenRequest
import com.inventraoficial.inventra.data.remote.dto.kitchen.KitchenResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface KitchenApi {
    @POST(PATH)
    suspend fun create(
        @Body request: CreateKitchenRequest,
    ): KitchenResponse

    companion object {
        private const val PATH = "kitchens"
    }
}
