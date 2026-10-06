package com.inventraoficial.inventra.feature.requirements.history.ui

import com.inventraoficial.inventra.feature.requirements.list.ui.RequirementStatus

data class HistoryItem(
    val id: String,
    val imageRes: Int,
    val productName: String,
    val description: String,
    val status: RequirementStatus,
)

data class RequirementsHistoryUiState(
    val historyItems: List<HistoryItem> = emptyList(),
)
