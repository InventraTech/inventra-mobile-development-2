package com.inventraoficial.inventra.data.repository

import com.inventraoficial.inventra.data.remote.api.RequisitionApi
import com.inventraoficial.inventra.data.remote.apiCall
import com.inventraoficial.inventra.data.remote.dto.requisition.AddRequisitionItemRequest
import com.inventraoficial.inventra.data.remote.dto.requisition.CreateRequisitionRequest
import com.inventraoficial.inventra.data.remote.dto.requisition.RequisitionItemResponse
import com.inventraoficial.inventra.data.remote.dto.requisition.RequisitionReasonRequest
import com.inventraoficial.inventra.data.remote.dto.requisition.RequisitionResponse
import com.inventraoficial.inventra.data.remote.dto.requisition.RequisitionStatus
import kotlinx.serialization.json.Json

interface RequisitionRepository {
    suspend fun requisitions(
        kitchenId: Int,
        status: RequisitionStatus? = null,
        requesterId: String? = null,
    ): Result<List<RequisitionResponse>>

    suspend fun requisition(id: Int): Result<RequisitionResponse>

    suspend fun items(id: Int): Result<List<RequisitionItemResponse>>

    suspend fun create(request: CreateRequisitionRequest): Result<RequisitionResponse>

    suspend fun addItem(
        id: Int,
        request: AddRequisitionItemRequest,
    ): Result<RequisitionResponse>

    suspend fun removeItem(
        id: Int,
        itemId: Int,
    ): Result<RequisitionResponse>

    suspend fun submit(id: Int): Result<RequisitionResponse>

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
    override suspend fun requisitions(
        kitchenId: Int,
        status: RequisitionStatus?,
        requesterId: String?,
    ) = apiCall(json) { requisitionApi.list(kitchenId, status, requesterId) }

    override suspend fun requisition(id: Int) = apiCall(json) { requisitionApi.find(id) }

    override suspend fun items(id: Int) = apiCall(json) { requisitionApi.items(id) }

    override suspend fun create(request: CreateRequisitionRequest) = apiCall(json) { requisitionApi.create(request) }

    override suspend fun addItem(
        id: Int,
        request: AddRequisitionItemRequest,
    ) = apiCall(json) { requisitionApi.addItem(id, request) }

    override suspend fun removeItem(
        id: Int,
        itemId: Int,
    ) = apiCall(json) { requisitionApi.removeItem(id, itemId) }

    override suspend fun submit(id: Int) = apiCall(json) { requisitionApi.submit(id) }

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
