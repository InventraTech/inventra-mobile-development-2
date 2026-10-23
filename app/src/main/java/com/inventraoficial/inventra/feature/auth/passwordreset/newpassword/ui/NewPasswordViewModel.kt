package com.inventraoficial.inventra.feature.auth.passwordreset.newpassword.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.inventraoficial.inventra.InventraApplication
import com.inventraoficial.inventra.data.repository.PasswordResetRepository
import com.inventraoficial.inventra.data.repository.ResetTokenExpiredException
import com.inventraoficial.inventra.feature.auth.passwordreset.MIN_PASSWORD_LENGTH
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class NewPasswordViewModel(
    private val passwordResetRepository: PasswordResetRepository,
    private val resetToken: String,
) : ViewModel() {
    private val _uiState = MutableStateFlow(NewPasswordUiState())
    val uiState: StateFlow<NewPasswordUiState> = _uiState.asStateFlow()

    fun onPasswordChange(password: String) {
        _uiState.update { it.copy(password = password, errorMessage = null) }
    }

    fun onConfirmPasswordChange(confirmPassword: String) {
        _uiState.update { it.copy(confirmPassword = confirmPassword, errorMessage = null) }
    }

    fun onSaveClick() {
        val state = _uiState.value
        if (state.isLoading || state.isTokenExpired) return
        val error = validate(state)
        if (error != null) {
            _uiState.update { it.copy(errorMessage = error) }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            passwordResetRepository
                .resetPassword(resetToken, state.password)
                .onSuccess { _uiState.update { it.copy(isLoading = false, isDone = true) } }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message,
                            isTokenExpired = error is ResetTokenExpiredException,
                        )
                    }
                }
        }
    }

    private fun validate(state: NewPasswordUiState): String? =
        when {
            state.password.isBlank() || state.confirmPassword.isBlank() -> "Preencha todos os campos"
            state.password.length < MIN_PASSWORD_LENGTH -> "A senha deve ter pelo menos $MIN_PASSWORD_LENGTH caracteres"
            state.password != state.confirmPassword -> "As senhas não coincidem"
            else -> null
        }

    companion object {
        fun factory(resetToken: String): ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    val application = this[APPLICATION_KEY] as InventraApplication
                    NewPasswordViewModel(application.container.passwordResetRepository, resetToken)
                }
            }
    }
}
