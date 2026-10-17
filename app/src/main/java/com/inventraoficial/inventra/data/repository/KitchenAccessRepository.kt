package com.inventraoficial.inventra.data.repository

import com.inventraoficial.inventra.data.local.GeoPoint
import com.inventraoficial.inventra.data.remote.api.KitchenAccessApi
import com.inventraoficial.inventra.data.remote.api.KitchenApi
import com.inventraoficial.inventra.data.remote.apiCall
import com.inventraoficial.inventra.data.remote.dto.kitchen.DiscoverKitchenResponse
import com.inventraoficial.inventra.data.remote.dto.kitchenaccess.CreateKitchenAccessRequest
import com.inventraoficial.inventra.data.remote.dto.kitchenaccess.KitchenAccessRequestResponse
import kotlinx.serialization.json.Json
import retrofit2.HttpException

interface KitchenAccessRepository {
    suspend fun discover(
        query: String,
        near: GeoPoint? = null,
    ): Result<List<DiscoverKitchenResponse>>

    suspend fun requestAccess(kitchenId: Int): Result<KitchenAccessRequestResponse>

    // null quando o usuário ainda não pediu para entrar em nenhuma cozinha.
    suspend fun myRequest(): Result<KitchenAccessRequestResponse?>
}

class NetworkKitchenAccessRepository(
    private val kitchenApi: KitchenApi,
    private val kitchenAccessApi: KitchenAccessApi,
    private val json: Json,
) : KitchenAccessRepository {
    override suspend fun discover(
        query: String,
        near: GeoPoint?,
    ): Result<List<DiscoverKitchenResponse>> =
        apiCall(json) {
            kitchenApi
                .discover(
                    query = query.trim().ifBlank { null },
                    latitude = near?.latitude,
                    longitude = near?.longitude,
                    size = DISCOVER_PAGE_SIZE,
                ).content
        }

    override suspend fun requestAccess(kitchenId: Int): Result<KitchenAccessRequestResponse> =
        apiCall(json) { kitchenAccessApi.create(CreateKitchenAccessRequest(kitchenId)) }

    override suspend fun myRequest(): Result<KitchenAccessRequestResponse?> =
        apiCall(json) {
            try {
                kitchenAccessApi.mine()
            } catch (e: HttpException) {
                if (e.code() == HTTP_NOT_FOUND) null else throw e
            }
        }

    private companion object {
        const val DISCOVER_PAGE_SIZE = 50
        const val HTTP_NOT_FOUND = 404
    }
}
