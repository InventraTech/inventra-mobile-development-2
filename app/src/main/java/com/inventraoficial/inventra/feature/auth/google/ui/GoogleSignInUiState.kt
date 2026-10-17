package com.inventraoficial.inventra.feature.auth.google.ui

import com.inventraoficial.inventra.core.designsystem.molecules.InventraUserRole

data class GoogleSignInUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val pendingSignUp: GoogleSignUp? = null,
    val isLoggedIn: Boolean = false,
    val mustCreateKitchen: Boolean = false,
    val mustJoinKitchen: Boolean = false,
) {
    val screenErrorMessage: String?
        get() = errorMessage.takeIf { pendingSignUp == null }

    val isButtonLoading: Boolean
        get() = isLoading && pendingSignUp == null
}

data class GoogleSignUp(
    val idToken: String,
    val name: String?,
    val email: String,
    val role: InventraUserRole? = null,
) {
    val firstName: String?
        get() = name?.trim()?.substringBefore(" ")?.takeIf { it.isNotBlank() }
}
