package com.inventraoficial.inventra.feature.auth.passwordreset.code.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.inventraoficial.inventra.InventraApplication
import com.inventraoficial.inventra.data.repository.PasswordResetRepository
import com.inventraoficial.inventra.feature.auth.passwordreset.RESET_CODE_LENGTH
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

class ResetCodeViewModel(
    private val passwordResetRepository: PasswordResetRepository,
    private val email: String,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ResetCodeUiState(email = email))
    val uiState: StateFlow<ResetCodeUiState> = _uiState.asStateFlow()
    private var countdown: Job? = null

    init {
        startCountdown()
    }

    fun onCodeChange(code: String) {
        _uiState.update { it.copy(code = code.filter(Char::isDigit).take(RESET_CODE_LENGTH), errorMessage = null) }
    }

    fun onConfirmClick() {
        val state = _uiState.value
        if (state.isLoading) return
        if (state.code.length != RESET_CODE_LENGTH) {
            _uiState.update { it.copy(errorMessage = "Digite os $RESET_CODE_LENGTH números do código") }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null, infoMessage = null) }
        viewModelScope.launch {
            passwordResetRepository
                .verifyCode(email, state.code)
                .onSuccess { token -> _uiState.update { it.copy(isLoading = false, resetToken = token) } }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, code = "", errorMessage = error.message) }
                }
        }
    }

    fun onResendClick() {
        if (!_uiState.value.canResend) return

        _uiState.update { it.copy(isResending = true, errorMessage = null, infoMessage = null) }
        viewModelScope.launch {
            passwordResetRepository
                .requestCode(email)
                .onSuccess {
                    _uiState.update {
                        it.copy(isResending = false, code = "", infoMessage = "Enviamos um novo código para $email.")
                    }
                    startCountdown()
                }.onFailure { error -> _uiState.update { it.copy(isResending = false, errorMessage = error.message) } }
        }
    }

    fun onNewPasswordOpened() {
        _uiState.update { it.copy(resetToken = null) }
    }

    private fun startCountdown() {
        countdown?.cancel()
        countdown =
            viewModelScope.launch {
                for (seconds in RESEND_SECONDS downTo 1) {
                    _uiState.update { it.copy(resendInSeconds = seconds) }
                    delay(1.seconds)
                }
                _uiState.update { it.copy(resendInSeconds = 0) }
            }
    }

    companion object {
        const val RESEND_SECONDS = 60

        fun factory(email: String): ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    val application = this[APPLICATION_KEY] as InventraApplication
                    ResetCodeViewModel(application.container.passwordResetRepository, email)
                }
            }
    }
}
