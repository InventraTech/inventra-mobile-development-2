package com.inventraoficial.inventra.feature.auth.google.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.inventraoficial.inventra.InventraApplication
import com.inventraoficial.inventra.core.designsystem.molecules.InventraUserRole
import com.inventraoficial.inventra.data.local.mustCreateKitchen
import com.inventraoficial.inventra.data.local.toSessionUser
import com.inventraoficial.inventra.data.remote.auth.google.GoogleSignInCancelledException
import com.inventraoficial.inventra.data.remote.dto.auth.AccessType
import com.inventraoficial.inventra.data.repository.AuthRepository
import com.inventraoficial.inventra.data.repository.GoogleRegistrationRequiredException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GoogleSignInViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(GoogleSignInUiState())
    val uiState: StateFlow<GoogleSignInUiState> = _uiState.asStateFlow()

    fun onGoogleClick(getIdToken: suspend () -> Result<String>) {
        if (_uiState.value.isLoading) return

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val idToken =
                getIdToken().getOrElse { error ->
                    val message = error.message.takeUnless { error is GoogleSignInCancelledException }
                    _uiState.update { it.copy(isLoading = false, errorMessage = message) }
                    return@launch
                }
            signIn(idToken, accessType = null)
        }
    }

    fun onRoleSelect(role: InventraUserRole) {
        _uiState.update { state ->
            state.copy(pendingSignUp = state.pendingSignUp?.copy(role = role), errorMessage = null)
        }
    }

    fun onConfirmSignUp() {
        val state = _uiState.value
        val signUp = state.pendingSignUp
        if (signUp == null || state.isLoading) return

        val role = signUp.role
        if (role == null) {
            _uiState.update { it.copy(errorMessage = "Escolha um cargo") }
        } else {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            viewModelScope.launch { signIn(signUp.idToken, role.toAccessType()) }
        }
    }

    fun onDismissSignUp() {
        _uiState.update { it.copy(pendingSignUp = null, isLoading = false, errorMessage = null) }
    }

    private suspend fun signIn(
        idToken: String,
        accessType: AccessType?,
    ) {
        authRepository
            .loginWithGoogle(idToken, accessType)
            .onSuccess { user ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        pendingSignUp = null,
                        isLoggedIn = true,
                        mustCreateKitchen = user.toSessionUser().mustCreateKitchen,
                    )
                }
            }.onFailure { error ->
                if (error is GoogleRegistrationRequiredException) {
                    val signUp = GoogleSignUp(idToken = idToken, name = error.name, email = error.email)
                    _uiState.update { it.copy(isLoading = false, pendingSignUp = signUp) }
                } else {
                    _uiState.update { it.copy(isLoading = false, errorMessage = error.message) }
                }
            }
    }

    private fun InventraUserRole.toAccessType(): AccessType =
        when (this) {
            InventraUserRole.Supervisor -> AccessType.SUPERVISOR
            InventraUserRole.Estoquista -> AccessType.ESTOQUISTA
        }

    companion object {
        val Factory: ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    val application = this[APPLICATION_KEY] as InventraApplication
                    GoogleSignInViewModel(application.container.authRepository)
                }
            }
    }
}
