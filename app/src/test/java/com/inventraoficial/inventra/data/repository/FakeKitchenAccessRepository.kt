package com.inventraoficial.inventra.data.repository

import com.inventraoficial.inventra.data.local.GeoPoint
import com.inventraoficial.inventra.data.remote.dto.kitchen.DiscoverKitchenResponse
import com.inventraoficial.inventra.data.remote.dto.kitchenaccess.KitchenAccessRequestResponse
import com.inventraoficial.inventra.data.remote.dto.kitchenaccess.KitchenAccessRequestStatus

class FakeKitchenAccessRepository(
    var discoverResult: Result<List<DiscoverKitchenResponse>> = Result.success(listOf(perto, longe, semLocalizacao)),
    var requestResult: Result<KitchenAccessRequestResponse> = Result.success(FakeMembersRepository.pendingRequest),
    var myRequestResult: Result<KitchenAccessRequestResponse?> = Result.success(FakeMembersRepository.pendingRequest),
) : KitchenAccessRepository {
    var discoverCalls = mutableListOf<Pair<String, GeoPoint?>>()
        private set
    var requestedKitchenIds = mutableListOf<Int>()
        private set
    var myRequestCalls = 0
        private set

    override suspend fun discover(
        query: String,
        near: GeoPoint?,
    ): Result<List<DiscoverKitchenResponse>> {
        discoverCalls += query to near
        return discoverResult
    }

    override suspend fun requestAccess(kitchenId: Int): Result<KitchenAccessRequestResponse> {
        requestedKitchenIds += kitchenId
        return requestResult
    }

    override suspend fun myRequest(): Result<KitchenAccessRequestResponse?> {
        myRequestCalls++
        return myRequestResult
    }

    companion object {
        val perto =
            DiscoverKitchenResponse(
                id = 7,
                name = "Sabor Filial Sul",
                city = "São Paulo",
                state = "SP",
                latitude = -23.56,
                longitude = -46.64,
                distanceKm = 0.85,
            )

        val longe =
            DiscoverKitchenResponse(
                id = 8,
                name = "Tung Tung Tung Sahur",
                city = "Rio de Janeiro",
                state = "RJ",
                latitude = -22.9,
                longitude = -43.2,
                distanceKm = 12.34,
                myRequestStatus = KitchenAccessRequestStatus.REJECTED,
            )

        val semLocalizacao =
            DiscoverKitchenResponse(
                id = 9,
                name = "Instituto J&F",
                myRequestStatus = KitchenAccessRequestStatus.PENDING,
            )
    }
}
