package com.inventraoficial.inventra.feature.auth.register.ui

import com.inventraoficial.inventra.core.designsystem.molecules.InventraUserRole

data class RegisterUiState(
    val role: InventraUserRole? = null,
    val name: String = "",
    val login: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isRegistered: Boolean = false,
)
