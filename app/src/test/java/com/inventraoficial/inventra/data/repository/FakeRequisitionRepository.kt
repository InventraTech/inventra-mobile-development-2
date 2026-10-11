package com.inventraoficial.inventra.data.repository

import com.inventraoficial.inventra.data.remote.dto.auth.KitchenSummary
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

    override suspend fun requisitions(kitchenId: Int): Result<List<RequisitionResponse>> {
        error?.let { return Result.failure(it) }
        return Result.success(requisitions)
    }

    override suspend fun items(id: Int): Result<List<RequisitionItemResponse>> = Result.success(itemsById[id].orEmpty())

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
