package com.inventraoficial.inventra.feature.cozinha.create.ui

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CreateCozinhaViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(CreateCozinhaUiState())
    val uiState: StateFlow<CreateCozinhaUiState> = _uiState.asStateFlow()

    fun onNameChange(name: String) {
        _uiState.value = _uiState.value.copy(name = name)
    }

    fun onAddressChange(address: String) {
        _uiState.value = _uiState.value.copy(address = address)
    }

    fun onTypeSelect(type: EstablishmentType) {
        _uiState.value = _uiState.value.copy(establishmentType = type)
    }

    @Suppress("ReturnCount")
    fun onCreateClick(): Boolean {
        val estadoAtual = _uiState.value

        if (estadoAtual.name.isBlank() || estadoAtual.address.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Preencha todos os campos")
            return false
        }

        if (estadoAtual.establishmentType == null) {
            _uiState.value = _uiState.value.copy(errorMessage = "Escolha o tipo de estabelecimento")
            return false
        }

        _uiState.value = _uiState.value.copy(errorMessage = null)
        return true
    }
}
