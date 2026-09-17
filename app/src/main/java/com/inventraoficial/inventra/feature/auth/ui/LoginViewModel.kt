package com.inventraoficial.inventra.feature.auth.ui

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

    fun onLoginClick() {
        val estadoAtual = _uiState.value
        if (estadoAtual.login.isBlank() || estadoAtual.password.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Preencha todos os campos")
        } else {
            // por enquanto, sem chamada real — pode só limpar o erro, ou deixar vazio mesmo
        }
    }

    fun onForgotPasswordClick() {
    }
}
