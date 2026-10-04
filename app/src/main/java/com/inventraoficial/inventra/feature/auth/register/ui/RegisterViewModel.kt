package com.inventraoficial.inventra.feature.auth.register.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.inventraoficial.inventra.InventraApplication
import com.inventraoficial.inventra.core.designsystem.molecules.InventraUserRole
import com.inventraoficial.inventra.data.remote.dto.auth.AccessType
import com.inventraoficial.inventra.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RegisterViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun onRoleSelect(role: InventraUserRole) {
        _uiState.update { it.copy(role = role) }
    }

    fun onNameChange(name: String) {
        _uiState.update { it.copy(name = name) }
    }

    fun onLoginChange(login: String) {
        _uiState.update { it.copy(login = login) }
    }

    fun onPasswordChange(password: String) {
        _uiState.update { it.copy(password = password) }
    }

    fun onConfirmPasswordChange(confirmPassword: String) {
        _uiState.update { it.copy(confirmPassword = confirmPassword) }
    }

    fun onRegisterClick() {
        val state = _uiState.value
        val erro = validate(state)
        if (erro != null) {
            _uiState.update { it.copy(errorMessage = erro) }
            return
        }

        // Nunca e nulo aqui (validate ja garantiu), mas o compilador nao sabe disso.
        val role = state.role ?: return
        val accessType =
            when (role) {
                InventraUserRole.Supervisor -> AccessType.SUPERVISOR
                InventraUserRole.Estoquista -> AccessType.ESTOQUISTA
            }

        _uiState.update { it.copy(errorMessage = null, isLoading = true) }
        viewModelScope.launch {
            authRepository
                .register(state.name.trim(), state.login.trim(), state.password, accessType)
                .onSuccess { _uiState.update { it.copy(isLoading = false, isRegistered = true) } }
                .onFailure { error -> _uiState.update { it.copy(isLoading = false, errorMessage = error.message) } }
        }
    }

    /** Devolve a mensagem do primeiro erro encontrado, ou null se o formulario estiver valido. */
    private fun validate(state: RegisterUiState): String? =
        when {
            state.role == null -> "Escolha um cargo"
            state.name.isBlank() ||
                state.login.isBlank() ||
                state.password.isBlank() ||
                state.confirmPassword.isBlank() -> "Preencha todos os campos"
            state.password.length < MIN_PASSWORD_LENGTH -> "A senha deve ter pelo menos $MIN_PASSWORD_LENGTH caracteres"
            state.password != state.confirmPassword -> "As senhas não coincidem"
            else -> null
        }

    companion object {
        private const val MIN_PASSWORD_LENGTH = 8

        val Factory: ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    val application = this[APPLICATION_KEY] as InventraApplication
                    RegisterViewModel(application.container.authRepository)
                }
            }
    }
}
