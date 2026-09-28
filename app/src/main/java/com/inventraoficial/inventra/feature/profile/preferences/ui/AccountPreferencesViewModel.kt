package com.inventraoficial.inventra.feature.profile.preferences.ui

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AccountPreferencesViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(AccountPreferencesUiState())
    val uiState: StateFlow<AccountPreferencesUiState> = _uiState.asStateFlow()

    fun onNotifyExpiringChange(value: Boolean) {
        _uiState.value = _uiState.value.copy(notifyExpiring = value)
    }

    fun onNotifyLowStockChange(value: Boolean) {
        _uiState.value = _uiState.value.copy(notifyLowStock = value)
    }

    fun onNotifyPendingApprovalChange(value: Boolean) {
        _uiState.value = _uiState.value.copy(notifyPendingApproval = value)
    }

    fun onThemeSelect(theme: AppTheme) {
        _uiState.value = _uiState.value.copy(theme = theme)
    }

    fun onLanguageClick() {
        // sem tela de selecao de idioma ainda
    }
}
