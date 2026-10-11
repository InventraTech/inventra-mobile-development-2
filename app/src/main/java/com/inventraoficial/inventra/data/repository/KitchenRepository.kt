package com.inventraoficial.inventra.data.repository

import com.inventraoficial.inventra.data.remote.api.KitchenApi
import com.inventraoficial.inventra.data.remote.apiCall
import com.inventraoficial.inventra.data.remote.dto.kitchen.CreateKitchenRequest
import com.inventraoficial.inventra.data.remote.dto.kitchen.KitchenResponse
import kotlinx.serialization.json.Json

interface KitchenRepository {
    suspend fun createKitchen(
        name: String,
        address: String?,
    ): Result<KitchenResponse>
}

class NetworkKitchenRepository(
    private val kitchenApi: KitchenApi,
    private val json: Json,
) : KitchenRepository {
    override suspend fun createKitchen(
        name: String,
        address: String?,
    ): Result<KitchenResponse> = apiCall(json) { kitchenApi.create(CreateKitchenRequest(name, address)) }
}
