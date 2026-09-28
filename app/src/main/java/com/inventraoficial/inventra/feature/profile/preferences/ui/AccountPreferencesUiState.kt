package com.inventraoficial.inventra.feature.profile.preferences.ui

enum class AppTheme(val label: String) {
    Light("Claro"),
    Dark("Escuro"),
    System("Sistema"),
}

data class AccountPreferencesUiState(
    val notifyExpiring: Boolean = true,
    val notifyLowStock: Boolean = false,
    val notifyPendingApproval: Boolean = false,
    val theme: AppTheme = AppTheme.Light,
    val language: String = "Português (Brasil)",
)
