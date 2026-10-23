package com.inventraoficial.inventra.feature.auth.passwordreset.newpassword.ui

data class NewPasswordUiState(
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isDone: Boolean = false,
    val isTokenExpired: Boolean = false,
)
