package com.inventraoficial.inventra.feature.cozinha.info.ui

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CozinhaInfoViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(CozinhaInfoUiState())
    val uiState: StateFlow<CozinhaInfoUiState> = _uiState.asStateFlow()

    init {
        loadCozinhaInfo()
    }

    private fun loadCozinhaInfo() {
        // simula o formato que a API deve devolver no futuro
        _uiState.value =
            _uiState.value.copy(
                name = "Sabor Filial Sul",
                address = "Av. das Indústrias, 450 – São Paulo, SP",
                establishmentType = "Filial",
            )
    }
}
