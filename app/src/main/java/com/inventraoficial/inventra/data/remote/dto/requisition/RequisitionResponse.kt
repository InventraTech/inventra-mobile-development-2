package com.inventraoficial.inventra.data.remote.dto.requisition

import com.inventraoficial.inventra.data.remote.dto.auth.KitchenSummary
import com.inventraoficial.inventra.data.remote.dto.stock.ProductSummary
import com.inventraoficial.inventra.data.remote.dto.stock.SupplierSummary
import kotlinx.serialization.Serializable

@Serializable
data class RequisitionResponse(
    val id: Int,
    val type: RequisitionType,
    val origin: String? = null,
    val status: RequisitionStatus,
    val reason: String? = null,
    val kitchen: KitchenSummary? = null,
    val requester: UserSummary? = null,
    val approver: UserSummary? = null,
    val createdAt: String,
    val approvedAt: String? = null,
)

@Serializable
data class RequisitionItemResponse(
    val id: Int,
    val product: ProductSummary,
    val quantity: Double,
    val estimatedPrice: Double? = null,
    val suggestedSupplier: SupplierSummary? = null,
    val note: String? = null,
)

@Serializable
data class UserSummary(
    val id: String,
    val name: String,
)

@Serializable
data class RequisitionReasonRequest(
    val reason: String,
)

@Serializable
enum class RequisitionType {
    PURCHASE,
    TRANSFER,
    CONSUMPTION,
}

@Serializable
enum class RequisitionStatus {
    UNDER_REVIEW,
    APPROVED,
    REJECTED,
    CANCELLED,
}
