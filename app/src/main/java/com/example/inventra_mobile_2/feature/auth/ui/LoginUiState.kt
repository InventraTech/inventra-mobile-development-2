package com.example.inventra_mobile_2.feature.auth.ui

data class LoginUiState(
    val login: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isBiometricAvailable: Boolean = false
) {
    val isLoginEnabled: Boolean
        get() = login.isNotBlank() && password.isNotBlank() && !isLoading
}