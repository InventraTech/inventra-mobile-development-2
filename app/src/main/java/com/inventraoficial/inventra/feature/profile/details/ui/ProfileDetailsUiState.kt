package com.inventraoficial.inventra.feature.profile.details.ui

data class ProfileDetailsUiState(
    val fullName: String = "",
    val roleLabel: String = "",
    val cozinhaName: String = "",
    val email: String = "",
    val isLogoutDialogVisible: Boolean = false,
    val isLoggedOut: Boolean = false,
    val leaveKitchen: LeaveKitchenState = LeaveKitchenState(),
)

data class LeaveKitchenState(
    val canLeave: Boolean = false,
    val isSupervisor: Boolean = false,
    val isDialogVisible: Boolean = false,
    val isLeaving: Boolean = false,
    val errorMessage: String? = null,
    val hasLeft: Boolean = false,
)
