package com.inventraoficial.inventra.feature.requirements.history.ui

import com.inventraoficial.inventra.data.remote.dto.requisition.RequisitionStatus
import com.inventraoficial.inventra.feature.requirements.RequisitionSummary

enum class HistoryFilter(
    val label: String,
    val status: RequisitionStatus?,
) {
    All("Todas", null),
    Approved("Aprovadas", RequisitionStatus.APPROVED),
    Rejected("Recusadas", RequisitionStatus.REJECTED),
    Cancelled("Canceladas", RequisitionStatus.CANCELLED),
}

data class RequirementsHistoryUiState(
    val requisitions: List<RequisitionSummary> = emptyList(),
    val selectedFilter: HistoryFilter = HistoryFilter.All,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)
