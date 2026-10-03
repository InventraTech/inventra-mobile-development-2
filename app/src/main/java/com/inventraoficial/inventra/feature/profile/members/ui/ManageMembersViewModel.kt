package com.inventraoficial.inventra.feature.profile.members.ui

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ManageMembersViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(buildInitialState())
    val uiState: StateFlow<ManageMembersUiState> = _uiState.asStateFlow()

    private fun buildInitialState(): ManageMembersUiState =
        // simula o formato que a API deve devolver no futuro
        ManageMembersUiState(
            pendingRequests =
                listOf(
                    PendingMemberRequest(initials = "MR", name = "Mauro Rosa", roleLabel = "Estoquista"),
                    PendingMemberRequest(initials = "EP", name = "Eduardo Passos", roleLabel = "Estoquista"),
                ),
            members =
                listOf(
                    CozinhaMember(
                        initials = "JS",
                        name = "Jones Silva (você)",
                        roleLabel = "Gerente",
                        isOwner = true,
                    ),
                    CozinhaMember(
                        initials = "PL",
                        name = "Paula Lima",
                        roleLabel = "Estoquista",
                        isOwner = false,
                    ),
                ),
        )

    fun onAcceptClick(request: PendingMemberRequest) {
        val estadoAtual = _uiState.value
        _uiState.value =
            estadoAtual.copy(
                pendingRequests = estadoAtual.pendingRequests - request,
                members =
                    estadoAtual.members +
                        CozinhaMember(
                            initials = request.initials,
                            name = request.name,
                            roleLabel = request.roleLabel,
                            isOwner = false,
                        ),
            )
    }

    fun onRejectClick(request: PendingMemberRequest) {
        _uiState.value = _uiState.value.copy(pendingRequests = _uiState.value.pendingRequests - request)
    }

    fun onRemoveClick(member: CozinhaMember) {
        _uiState.value = _uiState.value.copy(members = _uiState.value.members - member)
    }
}
