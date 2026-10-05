package com.inventraoficial.inventra.feature.requirements.list.ui

import androidx.lifecycle.ViewModel
import com.inventraoficial.inventra.R
import com.inventraoficial.inventra.feature.requirements.RequirementsSharedData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class RequirementsListViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(RequirementsListUiState())
    val uiState: StateFlow<RequirementsListUiState> = _uiState.asStateFlow()

    private val allRequirements: List<Requirement> = buildFakeRequirements()

    init {
        loadRequirements()
    }

    private fun buildFakeRequirements(): List<Requirement> =
        listOf(
            Requirement(
                id = "1",
                imageRes = R.drawable.ic_inventra_logo,
                productName = "Baião - Arroz Camil",
                description = "Solicitação entrada como Estoquista",
                status = RequirementStatus.PENDING,
            ),
            Requirement(
                id = "2",
                imageRes = R.drawable.ic_inventra_logo,
                productName = "Eduardo Passos",
                description = "Solicitação entrada como Estoquista",
                status = RequirementStatus.PENDING,
            ),
            Requirement(
                id = "3",
                imageRes = R.drawable.ic_inventra_logo,
                productName = "Compra - Leite Integral",
                description = "Solicitação de compra para reposição",
                status = RequirementStatus.PENDING,
            ),
            Requirement(
                id = "4",
                imageRes = R.drawable.ic_inventra_logo,
                productName = "Maria Souza",
                description = "Entrada como Estoquista",
                status = RequirementStatus.PENDING,
            ),
            Requirement(
                id = "5",
                imageRes = R.drawable.ic_inventra_logo,
                productName = "João Silva",
                description = "Entrada como Gerente",
                status = RequirementStatus.PENDING,
            ),
        )

    private fun loadRequirements() {
        _uiState.value = _uiState.value.copy(requirements = allRequirements)
    }

    fun onAcceptRequirement(requirement: Requirement) {
        val acceptedRequirement = requirement.copy(status = RequirementStatus.ACCEPTED)
        RequirementsSharedData.addProcessedItem(acceptedRequirement)

        val updatedRequirements = _uiState.value.requirements.filter { it.id != requirement.id }
        _uiState.value = _uiState.value.copy(requirements = updatedRequirements)
    }

    fun onRejectRequirement(requirement: Requirement) {
        val rejectedRequirement = requirement.copy(status = RequirementStatus.REJECTED)
        RequirementsSharedData.addProcessedItem(rejectedRequirement)

        val updatedRequirements = _uiState.value.requirements.filter { it.id != requirement.id }
        _uiState.value = _uiState.value.copy(requirements = updatedRequirements)
    }
}
