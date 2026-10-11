package com.inventraoficial.inventra.data.repository

import com.inventraoficial.inventra.data.remote.api.KitchenAccessApi
import com.inventraoficial.inventra.data.remote.api.UserApi
import com.inventraoficial.inventra.data.remote.apiCall
import com.inventraoficial.inventra.data.remote.dto.kitchenaccess.KitchenAccessRequestResponse
import com.inventraoficial.inventra.data.remote.dto.kitchenaccess.KitchenAccessRequestStatus
import com.inventraoficial.inventra.data.remote.dto.kitchenaccess.RejectKitchenAccessRequest
import com.inventraoficial.inventra.data.remote.dto.user.UserResponse
import kotlinx.serialization.json.Json

interface MembersRepository {
    suspend fun pendingRequests(): Result<List<KitchenAccessRequestResponse>>

    suspend fun members(): Result<List<UserResponse>>

    suspend fun approve(requestId: String): Result<KitchenAccessRequestResponse>

    suspend fun reject(
        requestId: String,
        reason: String?,
    ): Result<KitchenAccessRequestResponse>
}

class NetworkMembersRepository(
    private val kitchenAccessApi: KitchenAccessApi,
    private val userApi: UserApi,
    private val json: Json,
) : MembersRepository {
    override suspend fun pendingRequests(): Result<List<KitchenAccessRequestResponse>> =
        apiCall(json) { kitchenAccessApi.list(KitchenAccessRequestStatus.PENDING) }

    override suspend fun members(): Result<List<UserResponse>> = apiCall(json) { userApi.listMembers() }

    override suspend fun approve(requestId: String): Result<KitchenAccessRequestResponse> =
        apiCall(json) { kitchenAccessApi.approve(requestId) }

    override suspend fun reject(
        requestId: String,
        reason: String?,
    ): Result<KitchenAccessRequestResponse> {
        val request = RejectKitchenAccessRequest(reason)
        return apiCall(json) { kitchenAccessApi.reject(requestId, request) }
    }
}
