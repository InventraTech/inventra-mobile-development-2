package com.inventraoficial.inventra.feature.requirements.list.ui

import com.inventraoficial.inventra.feature.requirements.RequisitionSummary

data class RequirementsListUiState(
    val requisitions: List<RequisitionSummary> = emptyList(),
    val isSupervisor: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)
