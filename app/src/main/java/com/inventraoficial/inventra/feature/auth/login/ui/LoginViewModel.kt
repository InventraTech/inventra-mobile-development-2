package com.inventraoficial.inventra.feature.auth.login.ui

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class LoginViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onLoginChange(login: String) {
        _uiState.value = _uiState.value.copy(login = login)
    }

    fun onPasswordChange(password: String) {
        _uiState.value = _uiState.value.copy(password = password)
    }

    @Suppress("ReturnCount")
    fun onLoginClick(): Boolean {
        val estadoAtual = _uiState.value
        if (estadoAtual.login.isBlank() || estadoAtual.password.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Preencha todos os campos")
            return false
        }

        if (estadoAtual.login != TEST_LOGIN || estadoAtual.password != TEST_PASSWORD) {
            _uiState.value = _uiState.value.copy(errorMessage = "Credenciais inválidas")
            return false
        }

        _uiState.value = _uiState.value.copy(errorMessage = null)
        return true
    }

    fun onForgotPasswordClick() {
    }

    private companion object {
        const val TEST_LOGIN = "felipe.kogake@gmail.com"
        const val TEST_PASSWORD = "kogake77"
    }
}
