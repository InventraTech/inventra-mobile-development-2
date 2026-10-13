package com.inventraoficial.inventra.feature.requirements.detail.ui

import com.inventraoficial.inventra.data.remote.dto.requisition.RequisitionStatus
import com.inventraoficial.inventra.data.remote.dto.requisition.RequisitionType

data class RequisitionItemUi(
    val name: String,
    val quantityText: String,
    val priceText: String?,
    val supplier: String?,
    val note: String?,
)

enum class PendingAction {
    Approve,
    Reject,
    Cancel,
}

data class RequisitionDetailUiState(
    val title: String = "",
    val status: RequisitionStatus? = null,
    val type: RequisitionType? = null,
    val requesterName: String = "",
    val dateText: String = "",
    val origin: String? = null,
    val reason: String? = null,
    val items: List<RequisitionItemUi> = emptyList(),
    val totalText: String? = null,
    val canReview: Boolean = false,
    val canCancel: Boolean = false,
    val pendingAction: PendingAction? = null,
    val reasonText: String = "",
    val isProcessing: Boolean = false,
    val actionErrorMessage: String? = null,
    val isFinished: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)
