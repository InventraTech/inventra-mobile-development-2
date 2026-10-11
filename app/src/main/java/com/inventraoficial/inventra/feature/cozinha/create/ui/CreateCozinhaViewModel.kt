package com.inventraoficial.inventra.feature.cozinha.create.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.inventraoficial.inventra.InventraApplication
import com.inventraoficial.inventra.data.repository.AuthRepository
import com.inventraoficial.inventra.data.repository.KitchenRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CreateCozinhaViewModel(
    private val kitchenRepository: KitchenRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(CreateCozinhaUiState())
    val uiState: StateFlow<CreateCozinhaUiState> = _uiState.asStateFlow()

    fun onNameChange(name: String) {
        _uiState.update { it.copy(name = name) }
    }

    fun onAddressChange(address: String) {
        _uiState.update { it.copy(address = address) }
    }

    fun onCreateClick() {
        val state = _uiState.value
        val erro = validate(state)
        if (erro != null) {
            _uiState.update { it.copy(errorMessage = erro) }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val address = state.address.trim().ifBlank { null }
            kitchenRepository
                .createKitchen(state.name.trim(), address)
                .onSuccess {
                    authRepository.refreshCurrentUser()
                    _uiState.update { it.copy(isLoading = false, isCreated = true) }
                }.onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = error.message) }
                }
        }
    }

    fun onCloseClick() {
        viewModelScope.launch {
            authRepository.logout()
            _uiState.update { it.copy(isLoggedOut = true) }
        }
    }

    private fun validate(state: CreateCozinhaUiState): String? =
        when {
            state.name.isBlank() -> "Informe o nome da cozinha"
            state.name.trim().length > MAX_NAME_LENGTH ->
                "O nome pode ter no máximo $MAX_NAME_LENGTH caracteres"
            state.address.trim().length > MAX_ADDRESS_LENGTH ->
                "O endereço pode ter no máximo $MAX_ADDRESS_LENGTH caracteres"
            else -> null
        }

    companion object {
        private const val MAX_NAME_LENGTH = 120
        private const val MAX_ADDRESS_LENGTH = 255

        val Factory: ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    val application = this[APPLICATION_KEY] as InventraApplication
                    CreateCozinhaViewModel(
                        kitchenRepository = application.container.kitchenRepository,
                        authRepository = application.container.authRepository,
                    )
                }
            }
    }
}
