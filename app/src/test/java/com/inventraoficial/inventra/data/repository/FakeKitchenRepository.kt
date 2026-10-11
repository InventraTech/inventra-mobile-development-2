package com.inventraoficial.inventra.data.repository

import com.inventraoficial.inventra.data.remote.dto.kitchen.KitchenResponse

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
    }
}
