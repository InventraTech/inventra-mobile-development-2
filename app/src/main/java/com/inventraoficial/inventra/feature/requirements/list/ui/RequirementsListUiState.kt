package com.inventraoficial.inventra.feature.requirements.list.ui

data class Requirement(
    val id: String,
    val imageRes: Int,
    val productName: String,
    val description: String,
    val status: RequirementStatus,
)

enum class RequirementStatus {
    ACCEPTED,
    REJECTED,
    PENDING,
}

data class RequirementsListUiState(
    val requirements: List<Requirement> = emptyList(),
    val processedRequirements: List<Requirement> = emptyList(),
)
