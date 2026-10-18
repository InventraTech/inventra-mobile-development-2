package com.inventraoficial.inventra.data.repository

import com.inventraoficial.inventra.data.remote.api.AlertApi
import com.inventraoficial.inventra.data.remote.apiCall
import com.inventraoficial.inventra.data.remote.dto.alert.AlertResponse
import kotlinx.serialization.json.Json

interface AlertRepository {
    suspend fun alerts(kitchenId: Int): Result<List<AlertResponse>>

    suspend fun unreadCount(kitchenId: Int): Result<Int>

    suspend fun markAsRead(id: Int): Result<AlertResponse>

    suspend fun markAllAsRead(kitchenId: Int): Result<Unit>
}

class NetworkAlertRepository(
    private val alertApi: AlertApi,
    private val json: Json,
) : AlertRepository {
    override suspend fun alerts(kitchenId: Int) = apiCall(json) { alertApi.list(kitchenId) }

    override suspend fun unreadCount(kitchenId: Int) = apiCall(json) { alertApi.list(kitchenId, unread = true).size }

    override suspend fun markAsRead(id: Int): Result<AlertResponse> = apiCall(json) { alertApi.markAsRead(id) }

    override suspend fun markAllAsRead(kitchenId: Int) = apiCall(json) { alertApi.markAllAsRead(kitchenId) }
}
