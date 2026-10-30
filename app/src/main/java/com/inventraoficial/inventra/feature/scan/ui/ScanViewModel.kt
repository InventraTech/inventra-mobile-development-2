package com.inventraoficial.inventra.feature.scan.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.inventraoficial.inventra.InventraApplication
import com.inventraoficial.inventra.data.remote.dto.auth.AccessType
import com.inventraoficial.inventra.data.remote.dto.product.BarcodeLookupResponse
import com.inventraoficial.inventra.data.remote.dto.product.BarcodeSource
import com.inventraoficial.inventra.data.repository.AlertRepository
import com.inventraoficial.inventra.data.repository.AuthRepository
import com.inventraoficial.inventra.data.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Suppress("TooManyFunctions")
class ScanViewModel(
    private val productRepository: ProductRepository,
    private val alertRepository: AlertRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ScanUiState())
    val uiState: StateFlow<ScanUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val accessType = authRepository.currentUser.first()?.accessType
            _uiState.update {
                it.copy(
                    canManageStock = accessType == AccessType.SUPERVISOR || accessType == AccessType.ESTOQUISTA,
                    canReport = accessType == AccessType.ESTOQUISTA,
                    canCreateProduct = accessType == AccessType.SUPERVISOR,
                )
            }
        }
    }

    fun onPermissionResult(granted: Boolean) {
        _uiState.update {
            it.copy(hasCameraPermission = granted, permissionMessage = if (granted) null else PERMISSION_DENIED)
        }
    }

    fun onFlashClick() {
        _uiState.update { it.copy(isFlashOn = !it.isFlashOn) }
    }

    fun onBarcodeDetected(barcode: String) {
        if (_uiState.value.isScanPaused) return
        lookup(barcode)
    }

    fun onScanAgainClick() {
        _uiState.update { it.copy(result = null, errorMessage = null) }
    }

    fun onManualEntryClick() {
        _uiState.update { it.copy(isManualEntryOpen = true, manualCode = "") }
    }

    fun onManualCodeChange(code: String) {
        if (code.all { it.isDigit() }) _uiState.update { it.copy(manualCode = code) }
    }

    fun onManualEntryDismiss() {
        _uiState.update { it.copy(isManualEntryOpen = false) }
    }

    fun onManualCodeSubmit() {
        val code = _uiState.value.manualCode.trim()
        if (code.isEmpty()) return
        _uiState.update { it.copy(isManualEntryOpen = false, result = null, errorMessage = null) }
        lookup(code)
    }

    fun onReportClick() {
        val result = _uiState.value.result as? ScanResult.NotRegistered ?: return
        if (result.isReporting || result.isReported) return

        updateNotRegistered { it.copy(isReporting = true, reportError = null) }
        viewModelScope.launch {
            val kitchenId = authRepository.currentUser.first()?.kitchenId
            if (kitchenId == null) {
                updateNotRegistered { it.copy(isReporting = false, reportError = NO_KITCHEN) }
                return@launch
            }
            alertRepository
                .reportUnregisteredProduct(kitchenId, result.barcode)
                .onSuccess { updateNotRegistered { it.copy(isReporting = false, isReported = true) } }
                .onFailure { error ->
                    updateNotRegistered { it.copy(isReporting = false, reportError = error.message) }
                }
        }
    }

    private fun lookup(barcode: String) {
        _uiState.update { it.copy(lookingUpBarcode = barcode, errorMessage = null) }
        viewModelScope.launch {
            val kitchenId = authRepository.currentUser.first()?.kitchenId
            productRepository
                .findByBarcode(barcode, kitchenId)
                .onSuccess { response ->
                    _uiState.update { it.copy(lookingUpBarcode = null, result = response.toResult(barcode)) }
                }.onFailure { error ->
                    _uiState.update { it.copy(lookingUpBarcode = null, errorMessage = error.message) }
                }
        }
    }

    private fun updateNotRegistered(transform: (ScanResult.NotRegistered) -> ScanResult.NotRegistered) {
        _uiState.update { state ->
            val result = state.result as? ScanResult.NotRegistered ?: return@update state
            state.copy(result = transform(result))
        }
    }

    companion object {
        private const val PERMISSION_DENIED = "Permita o acesso à câmera para escanear"
        private const val NO_KITCHEN = "Você ainda não faz parte de uma cozinha."

        val Factory: ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    val application = this[APPLICATION_KEY] as InventraApplication
                    ScanViewModel(
                        application.container.productRepository,
                        application.container.alertRepository,
                        application.container.authRepository,
                    )
                }
            }
    }
}

private val ptBr: Locale = Locale.forLanguageTag("pt-BR")
private val quantityFormat = DecimalFormat("#,##0.###", DecimalFormatSymbols(ptBr))
private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

private fun BarcodeLookupResponse?.toResult(barcode: String): ScanResult {
    val product = this?.product
    if (this == null || source != BarcodeSource.CATALOG || product == null) {
        val suggestion = this?.suggestion?.let { listOfNotNull(it.name, it.brand, it.quantity).joinToString(" · ") }
        return ScanResult.NotRegistered(
            barcode = barcode,
            suggestion = suggestion?.ifEmpty { null },
            photoUrl = this?.suggestion?.imageUrl,
        )
    }
    val total = kitchenStock?.totalQuantity ?: 0.0
    val batches = kitchenStock?.activeBatches ?: 0
    val batchesText = if (batches == 1) "1 lote" else "$batches lotes"
    return ScanResult.Found(
        barcode = barcode,
        productId = product.id,
        name = product.name,
        details = listOfNotNull(product.brand, product.category?.name).joinToString(" · ").ifEmpty { null },
        photoUrl = product.photoUrl,
        stock =
            if (total > 0.0) {
                "${quantityFormat.format(total)} ${product.unit.symbol} em $batchesText"
            } else {
                "Sem saldo na cozinha"
            },
        nextExpiration = kitchenStock?.nextExpirationDate?.let { LocalDate.parse(it).format(dateFormatter) },
        hasStock = total > 0.0,
    )
}
