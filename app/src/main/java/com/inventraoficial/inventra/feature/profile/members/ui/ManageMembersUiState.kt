package com.inventraoficial.inventra.feature.profile.members.ui

data class PendingMemberRequest(
    val initials: String,
    val name: String,
    val roleLabel: String,
)

data class CozinhaMember(
    val initials: String,
    val name: String,
    val roleLabel: String,
    val isOwner: Boolean,
)

data class ManageMembersUiState(
    val pendingRequests: List<PendingMemberRequest> = emptyList(),
    val members: List<CozinhaMember> = emptyList(),
)
