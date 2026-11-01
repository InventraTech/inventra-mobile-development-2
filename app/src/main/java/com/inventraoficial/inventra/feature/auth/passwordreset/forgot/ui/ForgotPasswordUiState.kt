package com.inventraoficial.inventra.feature.auth.passwordreset.forgot.ui

data class ForgotPasswordUiState(
    val email: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val codeSentTo: String? = null,
)
