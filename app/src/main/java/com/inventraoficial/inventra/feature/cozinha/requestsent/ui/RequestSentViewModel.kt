package com.inventraoficial.inventra.feature.cozinha.requestsent.ui

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class RequestSentViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(RequestSentUiState())
    val uiState: StateFlow<RequestSentUiState> = _uiState.asStateFlow()

    init {
        // simula o nome da cozinha que viria da tela anterior; o Screen ainda
        // nao carrega argumentos, entao fica fixo ate isso existir
        _uiState.value = _uiState.value.copy(cozinhaName = "Restaurante Sabor Caseiro")
    }
}
