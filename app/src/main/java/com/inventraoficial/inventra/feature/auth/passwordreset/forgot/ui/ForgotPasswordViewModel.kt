package com.inventraoficial.inventra.feature.auth.passwordreset.forgot.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.inventraoficial.inventra.InventraApplication
import com.inventraoficial.inventra.data.repository.PasswordResetRepository
import com.inventraoficial.inventra.feature.auth.passwordreset.looksLikeEmail
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ForgotPasswordViewModel(
    private val passwordResetRepository: PasswordResetRepository,
    email: String,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ForgotPasswordUiState(email = email))
    val uiState: StateFlow<ForgotPasswordUiState> = _uiState.asStateFlow()

    fun onEmailChange(email: String) {
        _uiState.update { it.copy(email = email, errorMessage = null) }
    }

    fun onSendClick() {
        val state = _uiState.value
        if (state.isLoading) return
        val email = state.email.trim()
        if (!email.looksLikeEmail()) {
            _uiState.update { it.copy(errorMessage = "Informe um e-mail válido") }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            passwordResetRepository
                .requestCode(email)
                .onSuccess { _uiState.update { it.copy(isLoading = false, codeSentTo = email) } }
                .onFailure { error -> _uiState.update { it.copy(isLoading = false, errorMessage = error.message) } }
        }
    }

    fun onCodeScreenOpened() {
        _uiState.update { it.copy(codeSentTo = null) }
    }

    companion object {
        fun factory(email: String): ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    val application = this[APPLICATION_KEY] as InventraApplication
                    ForgotPasswordViewModel(application.container.passwordResetRepository, email)
                }
            }
    }
}
