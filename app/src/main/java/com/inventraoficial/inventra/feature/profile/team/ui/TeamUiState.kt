package com.inventraoficial.inventra.feature.profile.team.ui

data class TeamMember(
    val id: String,
    val initials: String,
    val name: String,
    val email: String,
    val roleLabel: String,
    val isCurrentUser: Boolean,
    val photoUrl: String? = null,
)

data class TeamUiState(
    val members: List<TeamMember> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)
