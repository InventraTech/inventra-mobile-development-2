package com.inventraoficial.inventra.feature.scan.ui

data class ScanUiState(
    val hasCameraPermission: Boolean = false,
    val isFlashOn: Boolean = false,
    val instruction: String = "Posicione o código de barras na moldura",
    val isProcessing: Boolean = false,
    val errorMessage: String? = null,
)
