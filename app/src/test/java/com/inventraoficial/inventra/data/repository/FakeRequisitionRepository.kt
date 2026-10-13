package com.inventraoficial.inventra.data.repository

import com.inventraoficial.inventra.data.remote.dto.auth.KitchenSummary
import com.inventraoficial.inventra.data.remote.dto.requisition.AddRequisitionItemRequest
import com.inventraoficial.inventra.data.remote.dto.requisition.CreateRequisitionRequest
import com.inventraoficial.inventra.data.remote.dto.requisition.RequisitionItemResponse
import com.inventraoficial.inventra.data.remote.dto.requisition.RequisitionResponse
import com.inventraoficial.inventra.data.remote.dto.requisition.RequisitionStatus
import com.inventraoficial.inventra.data.remote.dto.requisition.RequisitionType
import com.inventraoficial.inventra.data.remote.dto.requisition.UserSummary
import com.inventraoficial.inventra.data.remote.dto.stock.ProductSummary
import com.inventraoficial.inventra.data.remote.dto.stock.SupplierSummary

class FakeRequisitionRepository(
    var requisitions: List<RequisitionResponse> = emptyList(),
    var itemsById: Map<Int, List<RequisitionItemResponse>> = emptyMap(),
) : RequisitionRepository {
    var error: Exception? = null
    var actionError: Exception? = null
    var actions = mutableListOf<Pair<String, Int>>()
        private set
    var reasons = mutableListOf<String>()
        private set
    var listCalls = mutableListOf<Triple<Int, RequisitionStatus?, String?>>()
        private set
    var created = mutableListOf<CreateRequisitionRequest>()
        private set
    var addedItems = mutableListOf<Pair<Int, AddRequisitionItemRequest>>()
        private set
    var nextId = 100

    override suspend fun requisitions(
        kitchenId: Int,
        status: RequisitionStatus?,
        requesterId: String?,
    ): Result<List<RequisitionResponse>> {
        listCalls += Triple(kitchenId, status, requesterId)
        error?.let { return Result.failure(it) }
        return Result.success(
            requisitions
                .filter { status == null || it.status == status }
                .filter { requesterId == null || it.requester?.id == requesterId },
        )
    }

    override suspend fun requisition(id: Int): Result<RequisitionResponse> {
        error?.let { return Result.failure(it) }
        return requisitions.firstOrNull { it.id == id }?.let { Result.success(it) }
            ?: Result.failure(NoSuchElementException("Requisição não encontrada."))
    }

    override suspend fun items(id: Int): Result<List<RequisitionItemResponse>> {
        error?.let { return Result.failure(it) }
        return Result.success(itemsById[id].orEmpty())
    }

    override suspend fun create(request: CreateRequisitionRequest): Result<RequisitionResponse> {
        created += request
        actionError?.let { return Result.failure(it) }
        val requisition =
            requisition(nextId++, status = RequisitionStatus.DRAFT, type = request.type).copy(origin = request.origin)
        requisitions = requisitions + requisition
        return Result.success(requisition)
    }

    override suspend fun addItem(
        id: Int,
        request: AddRequisitionItemRequest,
    ): Result<RequisitionResponse> {
        addedItems += id to request
        actionError?.let { return Result.failure(it) }
        val current = itemsById[id].orEmpty()
        val newItem =
            RequisitionItemResponse(
                id = id * ITEM_ID_FACTOR + current.size + 1,
                product = ProductSummary(id = request.productId, name = "Produto ${request.productId}"),
                quantity = request.quantity,
                estimatedPrice = request.estimatedPrice,
                suggestedSupplier = request.suggestedSupplierId?.let { SupplierSummary(it, "Fornecedor $it") },
                note = request.note,
            )
        itemsById = itemsById + (id to current + newItem)
        return Result.success(requisitions.first { it.id == id })
    }

    override suspend fun removeItem(
        id: Int,
        itemId: Int,
    ): Result<RequisitionResponse> {
        actions += "removeItem" to itemId
        actionError?.let { return Result.failure(it) }
        itemsById = itemsById + (id to itemsById[id].orEmpty().filterNot { it.id == itemId })
        return Result.success(requisitions.first { it.id == id })
    }

    override suspend fun submit(id: Int) = act("submit", id, RequisitionStatus.UNDER_REVIEW)

    override suspend fun approve(id: Int): Result<RequisitionResponse> = act("approve", id, RequisitionStatus.APPROVED)

    override suspend fun reject(
        id: Int,
        reason: String,
    ): Result<RequisitionResponse> {
        reasons += reason
        return act("reject", id, RequisitionStatus.REJECTED)
    }

    override suspend fun cancel(
        id: Int,
        reason: String,
    ): Result<RequisitionResponse> {
        reasons += reason
        return act("cancel", id, RequisitionStatus.CANCELLED)
    }

    private fun act(
        name: String,
        id: Int,
        status: RequisitionStatus,
    ): Result<RequisitionResponse> {
        actions += name to id
        actionError?.let { return Result.failure(it) }
        return Result.success(requisitions.first { it.id == id }.copy(status = status))
    }

    companion object {
        private const val ITEM_ID_FACTOR = 1000

        val supervisor = UserSummary(id = "sup-1", name = "Jones Silva")
        val buyer = UserSummary(id = "buy-1", name = "Maria Souza")
        val otherBuyer = UserSummary(id = "buy-2", name = "Paulo Lima")

        @Suppress("LongParameterList")
        fun requisition(
            id: Int,
            status: RequisitionStatus = RequisitionStatus.UNDER_REVIEW,
            type: RequisitionType = RequisitionType.PURCHASE,
            requester: UserSummary = buyer,
            createdAt: String = "2026-10-11T13:00:00",
            reason: String? = null,
        ) = RequisitionResponse(
            id = id,
            type = type,
            origin = "Reposição semanal",
            status = status,
            reason = reason,
            kitchen = KitchenSummary(id = 7, name = "Sabor Filial Sul"),
            requester = requester,
            createdAt = createdAt,
        )

        fun item(
            id: Int,
            productName: String,
            quantity: Double,
            estimatedPrice: Double? = null,
        ) = RequisitionItemResponse(
            id = id,
            product = ProductSummary(id = id, name = productName),
            quantity = quantity,
            estimatedPrice = estimatedPrice,
            suggestedSupplier = SupplierSummary(id = 1, legalName = "Italac"),
        )
    }
}
