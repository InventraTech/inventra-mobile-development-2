package com.inventraoficial.inventra.feature.auth.register.ui

import androidx.lifecycle.ViewModel
import com.inventraoficial.inventra.core.designsystem.molecules.InventraUserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class RegisterViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun onRoleSelect(role: InventraUserRole) {
        _uiState.value = _uiState.value.copy(role = role)
    }

    fun onLoginChange(login: String) {
        _uiState.value = _uiState.value.copy(login = login)
    }

    fun onPasswordChange(password: String) {
        _uiState.value = _uiState.value.copy(password = password)
    }

    fun onConfirmPasswordChange(confirmPassword: String) {
        _uiState.value = _uiState.value.copy(confirmPassword = confirmPassword)
    }

    fun onRegisterClick(): Boolean {
        val estadoAtual = _uiState.value

        if (estadoAtual.role == null) {
            _uiState.value = _uiState.value.copy(errorMessage = "Escolha um cargo")
            return false
        }

        if (estadoAtual.login.isBlank() || estadoAtual.password.isBlank() || estadoAtual.confirmPassword.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Preencha todos os campos")
            return false
        }

        if (estadoAtual.password != estadoAtual.confirmPassword) {
            _uiState.value = _uiState.value.copy(errorMessage = "As senhas nao coincidem")
            return false
        }

        _uiState.value = _uiState.value.copy(errorMessage = null)
        return true
    }
}
