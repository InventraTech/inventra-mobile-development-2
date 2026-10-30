package com.inventraoficial.inventra.feature.auth.passwordreset.code.ui

data class ResetCodeUiState(
    val email: String = "",
    val code: String = "",
    val isLoading: Boolean = false,
    val isResending: Boolean = false,
    val resendInSeconds: Int = 0,
    val errorMessage: String? = null,
    val infoMessage: String? = null,
    val resetToken: String? = null,
) {
    val canResend: Boolean
        get() = resendInSeconds == 0 && !isResending
}
