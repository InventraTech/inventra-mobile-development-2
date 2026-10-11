package com.inventraoficial.inventra.data.repository

import com.inventraoficial.inventra.data.remote.api.RequisitionApi
import com.inventraoficial.inventra.data.remote.apiCall
import com.inventraoficial.inventra.data.remote.dto.requisition.RequisitionItemResponse
import com.inventraoficial.inventra.data.remote.dto.requisition.RequisitionReasonRequest
import com.inventraoficial.inventra.data.remote.dto.requisition.RequisitionResponse
import kotlinx.serialization.json.Json

interface RequisitionRepository {
    suspend fun requisitions(kitchenId: Int): Result<List<RequisitionResponse>>

    suspend fun items(id: Int): Result<List<RequisitionItemResponse>>

    suspend fun approve(id: Int): Result<RequisitionResponse>

    suspend fun reject(
        id: Int,
        reason: String,
    ): Result<RequisitionResponse>

    suspend fun cancel(
        id: Int,
        reason: String,
    ): Result<RequisitionResponse>
}

class NetworkRequisitionRepository(
    private val requisitionApi: RequisitionApi,
    private val json: Json,
) : RequisitionRepository {
    override suspend fun requisitions(kitchenId: Int) = apiCall(json) { requisitionApi.list(kitchenId) }

    override suspend fun items(id: Int) = apiCall(json) { requisitionApi.items(id) }

    override suspend fun approve(id: Int) = apiCall(json) { requisitionApi.approve(id) }

    override suspend fun reject(
        id: Int,
        reason: String,
    ): Result<RequisitionResponse> = apiCall(json) { requisitionApi.reject(id, RequisitionReasonRequest(reason)) }

    override suspend fun cancel(
        id: Int,
        reason: String,
    ): Result<RequisitionResponse> = apiCall(json) { requisitionApi.cancel(id, RequisitionReasonRequest(reason)) }
}
