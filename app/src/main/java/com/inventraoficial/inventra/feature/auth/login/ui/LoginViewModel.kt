package com.inventraoficial.inventra.feature.auth.login.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.inventraoficial.inventra.InventraApplication
import com.inventraoficial.inventra.data.local.mustCreateKitchen
import com.inventraoficial.inventra.data.local.mustJoinKitchen
import com.inventraoficial.inventra.data.local.toSessionUser
import com.inventraoficial.inventra.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onLoginChange(login: String) {
        _uiState.update { it.copy(login = login) }
    }

    fun onPasswordChange(password: String) {
        _uiState.update { it.copy(password = password) }
    }

    fun onLoginClick() {
        val currentState = _uiState.value
        if (currentState.login.isBlank() || currentState.password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Preencha todos os campos") }
            return
        }

        _uiState.update { it.copy(errorMessage = null, isLoading = true) }
        viewModelScope.launch {
            authRepository
                .login(currentState.login.trim(), currentState.password)
                .onSuccess { user ->
                    val sessionUser = user.toSessionUser()
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = null,
                            isLoggedIn = true,
                            mustCreateKitchen = sessionUser.mustCreateKitchen,
                            mustJoinKitchen = sessionUser.mustJoinKitchen,
                        )
                    }
                }.onFailure { error -> _uiState.update { it.copy(errorMessage = error.message, isLoading = false) } }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    val application = this[APPLICATION_KEY] as InventraApplication
                    LoginViewModel(application.container.authRepository)
                }
            }
    }
}
