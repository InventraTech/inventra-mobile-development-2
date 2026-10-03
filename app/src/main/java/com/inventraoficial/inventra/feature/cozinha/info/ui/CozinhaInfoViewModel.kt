package com.inventraoficial.inventra.feature.cozinha.info.ui

import androidx.lifecycle.ViewModel
import com.inventraoficial.inventra.feature.cozinha.create.ui.EstablishmentType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CozinhaInfoViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(CozinhaInfoUiState())
    val uiState: StateFlow<CozinhaInfoUiState> = _uiState.asStateFlow()

    // guarda o ultimo estado salvo, para descartar em Cancelar
    private var savedState = CozinhaInfoUiState()

    init {
        loadCozinhaInfo()
    }

    private fun loadCozinhaInfo() {
        // simula o formato que a API deve devolver no futuro
        val loaded =
            CozinhaInfoUiState(
                name = "Sabor Filial Sul",
                address = "Av. das Indústrias, 450 – São Paulo, SP",
                establishmentType = EstablishmentType.Filial,
            )
        savedState = loaded
        _uiState.value = loaded
    }

    fun onNameChange(name: String) {
        _uiState.value = _uiState.value.copy(name = name)
    }

    fun onAddressChange(address: String) {
        _uiState.value = _uiState.value.copy(address = address)
    }

    fun onTypeSelect(type: EstablishmentType) {
        _uiState.value = _uiState.value.copy(establishmentType = type)
    }

    fun onEditClick() {
        _uiState.value = _uiState.value.copy(isEditing = true)
    }

    fun onCancelClick() {
        _uiState.value = savedState
    }

    fun onSaveClick() {
        // sem persistencia real ainda; so guarda como o novo estado salvo
        savedState = _uiState.value.copy(isEditing = false)
        _uiState.value = savedState
    }
}
