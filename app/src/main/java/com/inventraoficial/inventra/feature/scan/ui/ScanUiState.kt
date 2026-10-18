package com.inventraoficial.inventra.feature.scan.ui

sealed interface ScanResult {
    val barcode: String

    data class Found(
        override val barcode: String,
        val productId: Int,
        val name: String,
        val details: String?,
        val photoUrl: String?,
        val stock: String,
        val nextExpiration: String?,
        val hasStock: Boolean,
    ) : ScanResult

    data class NotRegistered(
        override val barcode: String,
        val suggestion: String?,
        val isReporting: Boolean = false,
        val isReported: Boolean = false,
        val reportError: String? = null,
    ) : ScanResult
}

data class ScanUiState(
    val hasCameraPermission: Boolean = false,
    val permissionMessage: String? = null,
    val isFlashOn: Boolean = false,
    val lookingUpBarcode: String? = null,
    val result: ScanResult? = null,
    val errorMessage: String? = null,
    val canManageStock: Boolean = false,
    val canReport: Boolean = false,
    val canCreateProduct: Boolean = false,
    val isManualEntryOpen: Boolean = false,
    val manualCode: String = "",
) {
    // Enquanto mostra um resultado, um erro ou o campo manual, a câmera continua ligada mas a leitura fica pausada.
    val isScanPaused: Boolean
        get() = lookingUpBarcode != null || result != null || errorMessage != null || isManualEntryOpen
}

@Suppress("LongParameterList")
data class ScanActions(
    val onBackClick: () -> Unit,
    val onFlashClick: () -> Unit,
    val onPermissionResult: (Boolean) -> Unit,
    val onBarcodeDetected: (String) -> Unit,
    val onScanAgainClick: () -> Unit,
    val onStockOutClick: (Int) -> Unit,
    val onStockEntryClick: (Int) -> Unit,
    val onSeeProductClick: (Int) -> Unit,
    val onReportClick: () -> Unit,
    val onCreateProductClick: (String) -> Unit,
    val onManualEntryClick: () -> Unit,
    val onManualCodeChange: (String) -> Unit,
    val onManualCodeSubmit: () -> Unit,
    val onManualEntryDismiss: () -> Unit,
)
