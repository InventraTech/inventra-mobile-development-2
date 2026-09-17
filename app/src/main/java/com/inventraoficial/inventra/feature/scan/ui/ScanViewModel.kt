package com.inventraoficial.inventra.feature.scan.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ScanViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ScanUiState())
    val uiState: StateFlow<ScanUiState> = _uiState.asStateFlow()

    fun onBackClick() {
        // sem navegação configurada ainda
    }

    fun onFlashClick() {
        _uiState.value = _uiState.value.copy(isFlashOn = !_uiState.value.isFlashOn)
    }

    fun onPermissionResult(granted: Boolean) {
        _uiState.value =
            _uiState.value.copy(
                hasCameraPermission = granted,
                errorMessage = if (granted) null else "Permita o acesso à câmera para escanear",
            )
    }

    fun onScan() {
        if (_uiState.value.isProcessing) return

        _uiState.value = _uiState.value.copy(isProcessing = true, errorMessage = null)

        viewModelScope.launch {
            delay(1000) // placeholder até a leitura de OCR real existir
            _uiState.value =
                _uiState.value.copy(
                    isProcessing = false,
                    instruction = "Código lido com sucesso",
                )
        }
    }
}
