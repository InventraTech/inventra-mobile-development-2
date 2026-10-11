package com.inventraoficial.inventra.feature.profile.members.ui

data class PendingMemberRequest(
    val id: String,
    val initials: String,
    val name: String,
    val email: String,
)

data class CozinhaMember(
    val id: String,
    val initials: String,
    val name: String,
    val roleLabel: String,
    val isCurrentUser: Boolean,
)

data class ManageMembersUiState(
    val pendingRequests: List<PendingMemberRequest> = emptyList(),
    val members: List<CozinhaMember> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val actionErrorMessage: String? = null,
    val processingRequestId: String? = null,
    val rejectTarget: PendingMemberRequest? = null,
    val rejectReason: String = "",
)
