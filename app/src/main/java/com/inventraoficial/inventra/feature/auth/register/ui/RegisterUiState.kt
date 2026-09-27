package com.inventraoficial.inventra.feature.auth.register.ui

import com.inventraoficial.inventra.core.designsystem.molecules.InventraUserRole

data class RegisterUiState(
    val role: InventraUserRole? = null,
    val login: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val errorMessage: String? = null,
)
