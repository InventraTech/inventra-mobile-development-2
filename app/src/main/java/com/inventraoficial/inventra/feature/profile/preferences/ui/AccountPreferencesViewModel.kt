package com.inventraoficial.inventra.feature.profile.preferences.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.inventraoficial.inventra.InventraApplication
import com.inventraoficial.inventra.data.local.AppPreferences
import com.inventraoficial.inventra.data.local.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AccountPreferencesViewModel(
    private val appPreferences: AppPreferences,
) : ViewModel() {
    private val _uiState = MutableStateFlow(AccountPreferencesUiState())
    val uiState: StateFlow<AccountPreferencesUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            appPreferences.themeMode.collect { mode ->
                _uiState.update { it.copy(theme = mode) }
            }
        }
    }

    fun onThemeSelect(theme: ThemeMode) {
        viewModelScope.launch {
            appPreferences.setThemeMode(theme)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    val application = this[APPLICATION_KEY] as InventraApplication
                    AccountPreferencesViewModel(appPreferences = application.container.appPreferences)
                }
            }
    }
}
