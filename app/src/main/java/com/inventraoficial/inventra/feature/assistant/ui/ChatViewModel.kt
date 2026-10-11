package com.inventraoficial.inventra.feature.assistant.ui

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ChatViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    fun onInputChange(value: String) {
        _uiState.value = _uiState.value.copy(inputValue = value)
    }

    fun onSuggestionClick(suggestion: String) {
        _uiState.value = _uiState.value.copy(inputValue = suggestion)
    }

    fun onSend() {
        _uiState.value = _uiState.value.copy(inputValue = "")
    }

    fun onMenuClick() = Unit
}
