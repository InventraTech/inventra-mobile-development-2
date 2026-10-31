package com.inventraoficial.inventra.feature.suppliers.detail.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.inventraoficial.inventra.InventraApplication
import com.inventraoficial.inventra.data.remote.dto.auth.AccessType
import com.inventraoficial.inventra.data.remote.dto.stock.StockBatchResponse
import com.inventraoficial.inventra.data.remote.dto.supplier.SupplierResponse
import com.inventraoficial.inventra.data.repository.AuthRepository
import com.inventraoficial.inventra.data.repository.StockRepository
import com.inventraoficial.inventra.data.repository.SupplierPhotoRepository
import com.inventraoficial.inventra.data.repository.SupplierRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class SupplierDetailViewModel(
    private val supplierId: Int,
    private val supplierRepository: SupplierRepository,
    private val authRepository: AuthRepository,
    private val stockRepository: StockRepository,
    private val supplierPhotoRepository: SupplierPhotoRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(SupplierDetailUiState())
    val uiState: StateFlow<SupplierDetailUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun onRetryClick() {
        load()
    }

    fun onPhotoClick() {
        val photo = _uiState.value.photo
        if (!photo.canEdit || photo.isUploading) return
        updatePhoto { it.copy(isSheetVisible = true, errorMessage = null) }
    }

    fun onPhotoSheetDismiss() = updatePhoto { it.copy(isSheetVisible = false) }

    fun onPhotoPicked(uri: String?) {
        updatePhoto { it.copy(isSheetVisible = false) }
        if (uri == null) return
        sendPhoto(localUrl = uri) { supplierPhotoRepository.update(supplierId, uri) }
    }

    fun onRemovePhotoClick() = sendPhoto(localUrl = null) { supplierPhotoRepository.remove(supplierId) }

    fun onCameraDenied() = updatePhoto { it.copy(isSheetVisible = false, errorMessage = CAMERA_DENIED) }

    private fun sendPhoto(
        localUrl: String?,
        call: suspend () -> Result<SupplierResponse>,
    ) {
        val previous = _uiState.value.photo.url
        updatePhoto { it.copy(url = localUrl, isSheetVisible = false, isUploading = true, errorMessage = null) }
        viewModelScope.launch {
            call()
                .onSuccess { supplier -> updatePhoto { it.copy(url = supplier.photoUrl, isUploading = false) } }
                .onFailure { error ->
                    updatePhoto { it.copy(url = previous, isUploading = false, errorMessage = error.message) }
                }
        }
    }

    private fun updatePhoto(transform: (SupplierPhotoState) -> SupplierPhotoState) {
        _uiState.update { it.copy(photo = transform(it.photo)) }
    }

    private fun load() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val supplier =
                supplierRepository.supplier(supplierId).getOrElse { error ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = error.message) }
                    return@launch
                }
            val user = authRepository.currentUser.first()
            val kitchenId = user?.kitchenId
            val batches =
                if (kitchenId == null) {
                    Result.success(emptyList())
                } else {
                    stockRepository.batches(kitchenId)
                }
            val delivered = batches.getOrNull()?.filter { it.supplier?.id == supplierId }
            val canEditPhoto = user?.accessType == AccessType.SUPERVISOR
            _uiState.update { it.withSupplier(supplier, delivered, canEditPhoto) }
        }
    }

    companion object {
        private const val MAX_HISTORY = 5
        private const val CAMERA_DENIED = "Permita o uso da câmera para tirar a foto, ou escolha uma da galeria."

        private fun SupplierDetailUiState.withSupplier(
            supplier: SupplierResponse,
            delivered: List<StockBatchResponse>?,
            canEditPhoto: Boolean,
        ): SupplierDetailUiState {
            val newestFirst = delivered.orEmpty().sortedByDescending { it.entryDate }
            return copy(
                photo = photo.copy(url = supplier.photoUrl, canEdit = canEditPhoto),
                name = supplier.legalName,
                rating = supplier.rating ?: 0,
                badgeText = supplier.cnpj,
                phone = supplier.whatsapp?.takeIf { it.isNotBlank() },
                email = supplier.email?.takeIf { it.isNotBlank() },
                products = buildProducts(newestFirst),
                deliveryHistory = newestFirst.take(MAX_HISTORY).map { it.toHistoryEntry() },
                showsDeliveries = delivered != null,
                isLoading = false,
            )
        }

        fun factory(supplierId: Int): ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    val application = this[APPLICATION_KEY] as InventraApplication
                    SupplierDetailViewModel(
                        supplierId = supplierId,
                        supplierRepository = application.container.supplierRepository,
                        authRepository = application.container.authRepository,
                        stockRepository = application.container.stockRepository,
                        supplierPhotoRepository = application.container.supplierPhotoRepository,
                    )
                }
            }
    }
}

private val ptBr: Locale = Locale.forLanguageTag("pt-BR")
private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

private fun buildProducts(newestFirst: List<StockBatchResponse>): List<SupplierProduct> =
    newestFirst
        .distinctBy { it.product.id }
        .map { latest ->
            SupplierProduct(
                name = latest.product.name,
                infoText =
                    latest.unitPrice?.let { "Último preço ${String.format(ptBr, "R$ %,.2f", it)}" }
                        ?: "Sem preço informado",
            )
        }.sortedBy { it.name.lowercase() }

private fun StockBatchResponse.toHistoryEntry() =
    DeliveryHistoryEntry(
        date = LocalDate.parse(entryDate).format(dateFormatter),
        productName = product.name,
        quantityLabel = "Qtd. ${initialQuantity.formatQuantity()}",
    )

private fun Double.formatQuantity(): String = if (this % 1.0 == 0.0) toLong().toString() else toString()
