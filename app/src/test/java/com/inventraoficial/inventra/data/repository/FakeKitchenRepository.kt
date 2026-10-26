package com.inventraoficial.inventra.data.repository

import com.inventraoficial.inventra.data.remote.dto.kitchen.KitchenResponse
import com.inventraoficial.inventra.data.remote.dto.kitchen.UpdateKitchenRequest

class FakeKitchenRepository(
    var result: Result<KitchenResponse> = Result.success(fakeKitchen),
) : KitchenRepository {
    var createCalls = 0
        private set
    var lastName: String? = null
        private set
    var lastAddress: String? = null
        private set

    override suspend fun createKitchen(
        name: String,
        address: String?,
    ): Result<KitchenResponse> {
        createCalls++
        lastName = name
        lastAddress = address
        return result
    }

    var kitchenCalls = mutableListOf<Int>()
        private set

    override suspend fun kitchen(id: Int): Result<KitchenResponse> {
        kitchenCalls += id
        return result
    }

    var updateResult: Result<KitchenResponse>? = null
    var updates = mutableListOf<Pair<Int, UpdateKitchenRequest>>()
        private set

    // Sem updateResult, devolve a cozinha com as mudanças aplicadas, como o backend.
    override suspend fun updateKitchen(
        id: Int,
        request: UpdateKitchenRequest,
    ): Result<KitchenResponse> {
        updates += id to request
        return updateResult ?: result.map { it.applying(request) }
    }

    companion object {
        val fakeKitchen =
            KitchenResponse(
                id = 7,
                name = "Sabor Filial Sul",
                code = "K7X2PQ",
                address = "Av. das Indústrias, 450",
                active = true,
                createdAt = "2026-10-10T00:00:00Z",
            )

        private fun KitchenResponse.applying(request: UpdateKitchenRequest) =
            copy(
                name = request.name ?: name,
                address = request.address ?: address,
                city = request.city?.ifBlank { null } ?: city,
                state = request.state ?: state,
                latitude = request.latitude ?: latitude,
                longitude = request.longitude ?: longitude,
                acceptingRequests = request.acceptingRequests ?: acceptingRequests,
            )
    }
}
