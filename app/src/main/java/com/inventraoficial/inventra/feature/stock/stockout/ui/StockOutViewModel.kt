package com.inventraoficial.inventra.feature.stock.stockout.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.inventraoficial.inventra.InventraApplication
import com.inventraoficial.inventra.data.remote.dto.product.ProductResponse
import com.inventraoficial.inventra.data.remote.dto.stock.BatchConsumptionResponse
import com.inventraoficial.inventra.data.remote.dto.stock.StockMovementReason
import com.inventraoficial.inventra.data.repository.AuthRepository
import com.inventraoficial.inventra.data.repository.ProductRepository
import com.inventraoficial.inventra.data.repository.StockRepository
import kotlinx.coroutines.async
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

class StockOutViewModel(
    private val productId: Int,
    private val productRepository: ProductRepository,
    private val stockRepository: StockRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(StockOutUiState())
    val uiState: StateFlow<StockOutUiState> = _uiState.asStateFlow()

    private var kitchenId: Int? = null
    private var availableQuantity = 0.0

    init {
        load()
    }

    fun onRetryClick() = load()

    fun onQuantityChange(quantity: String) {
        if (quantity.all { it.isDigit() || it == ',' || it == '.' }) {
            _uiState.update { it.copy(quantity = quantity, submitError = null) }
        }
    }

    fun onReasonSelect(reason: StockOutReason) {
        _uiState.update { it.copy(reason = reason) }
    }

    fun onConfirmClick() {
        val state = _uiState.value
        if (state.isSubmitting || kitchenId == null) return

        val quantity = state.quantity.toQuantity()
        val error =
            when {
                quantity == null || quantity <= 0.0 -> "Informe a quantidade da baixa"
                quantity > availableQuantity ->
                    "A quantidade passa do saldo da cozinha (${state.available} ${state.unitSymbol})"
                else -> null
            }
        _uiState.update { it.copy(submitError = error, isConfirmDialogOpen = error == null) }
    }

    fun onConfirmDialogDismiss() {
        _uiState.update { it.copy(isConfirmDialogOpen = false) }
    }

    fun onConfirmDialogConfirm() {
        val state = _uiState.value
        val kitchen = kitchenId
        val quantity = state.quantity.toQuantity()
        if (!state.isConfirmDialogOpen || state.isSubmitting) return
        if (kitchen == null || quantity == null) return

        _uiState.update { it.copy(isConfirmDialogOpen = false, isSubmitting = true, submitError = null) }
        viewModelScope.launch {
            stockRepository
                .consume(kitchen, productId, quantity, state.reason.toApi())
                .onSuccess { batches ->
                    val unit = state.unitSymbol
                    _uiState.update {
                        it.copy(isSubmitting = false, consumedBatches = batches.map { batch -> batch.toLine(unit) })
                    }
                }.onFailure { error -> _uiState.update { it.copy(isSubmitting = false, submitError = error.message) } }
        }
    }

    private fun load() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val kitchen = authRepository.currentUser.first()?.kitchenId
            kitchenId = kitchen
            if (kitchen == null) {
                _uiState.update { it.copy(isLoading = false, errorMessage = NO_KITCHEN) }
                return@launch
            }
            val product = async { productRepository.product(productId) }
            val stock = async { stockRepository.kitchenStock(kitchen) }
            val productResult = product.await()
            val stockResult = stock.await()
            val error = productResult.exceptionOrNull() ?: stockResult.exceptionOrNull()
            if (error != null) {
                _uiState.update { it.copy(isLoading = false, errorMessage = error.message) }
                return@launch
            }
            val item = stockResult.getOrThrow().firstOrNull { it.product.id == productId }
            showProduct(
                product = productResult.getOrThrow(),
                available = item?.totalQuantity ?: 0.0,
                nextExpiration = item?.nextExpirationDate,
            )
        }
    }

    private fun showProduct(
        product: ProductResponse,
        available: Double,
        nextExpiration: String?,
    ) {
        availableQuantity = available
        _uiState.update {
            it.copy(
                isLoading = false,
                productName = product.name,
                details = listOfNotNull(product.brand, product.category?.name).joinToString(" · ").ifEmpty { null },
                unitSymbol = product.unit.symbol,
                available = formatQuantity(available),
                nextExpiration = nextExpiration?.let { date -> LocalDate.parse(date).format(dateFormatter) },
                hasStock = available > 0.0,
            )
        }
    }

    companion object {
        private const val NO_KITCHEN = "Você ainda não faz parte de uma cozinha."

        fun factory(productId: Int): ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    val application = this[APPLICATION_KEY] as InventraApplication
                    StockOutViewModel(
                        productId = productId,
                        productRepository = application.container.productRepository,
                        stockRepository = application.container.stockRepository,
                        authRepository = application.container.authRepository,
                    )
                }
            }
    }
}

private val ptBr: Locale = Locale.forLanguageTag("pt-BR")
private val quantityFormat = DecimalFormat("#,##0.###", DecimalFormatSymbols(ptBr))
private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

private fun formatQuantity(value: Double): String = quantityFormat.format(value)

private fun String.toQuantity(): Double? = replace(',', '.').toDoubleOrNull()

private fun StockOutReason.toApi(): StockMovementReason =
    when (this) {
        StockOutReason.Usage -> StockMovementReason.USAGE
        StockOutReason.Loss -> StockMovementReason.LOSS
        StockOutReason.Expired -> StockMovementReason.EXPIRED
    }

private fun BatchConsumptionResponse.toLine(unitSymbol: String) =
    ConsumedBatchLine(
        batchNumber = batch.batchNumber,
        consumed = "${formatQuantity(consumedQuantity)} $unitSymbol",
        remaining = "${formatQuantity(remainingQuantity)} $unitSymbol",
    )
