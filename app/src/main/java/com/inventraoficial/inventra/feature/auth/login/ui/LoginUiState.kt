package com.inventraoficial.inventra.feature.auth.login.ui

data class LoginUiState(
    val login: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isBiometricAvailable: Boolean = false,
    val isLoggedIn: Boolean = false,
) {
    val isLoginEnabled: Boolean
        get() = login.isNotBlank() && password.isNotBlank() && !isLoading
}
