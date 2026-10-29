package com.inventraoficial.inventra.feature.stock.stockentry.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.inventraoficial.inventra.InventraApplication
import com.inventraoficial.inventra.data.remote.dto.stock.RegisterStockEntryRequest
import com.inventraoficial.inventra.data.remote.dto.stock.StockBatchResponse
import com.inventraoficial.inventra.data.repository.AuthRepository
import com.inventraoficial.inventra.data.repository.ProductRepository
import com.inventraoficial.inventra.data.repository.StockRepository
import com.inventraoficial.inventra.data.repository.SupplierRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.time.Clock
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Suppress("TooManyFunctions")
class StockEntryViewModel(
    private val productId: Int,
    private val productRepository: ProductRepository,
    private val stockRepository: StockRepository,
    private val supplierRepository: SupplierRepository,
    private val authRepository: AuthRepository,
    private val clock: Clock = Clock.systemDefaultZone(),
) : ViewModel() {
    private val _uiState = MutableStateFlow(StockEntryUiState(entryDate = LocalDate.now(clock)))
    val uiState: StateFlow<StockEntryUiState> = _uiState.asStateFlow()

    private var kitchenId: Int? = null

    init {
        load()
    }

    fun onRetryClick() = load()

    fun onQuantityChange(quantity: String) {
        if (quantity.isDecimalInput()) _uiState.update { it.copy(quantity = quantity, submitError = null) }
    }

    fun onBatchNumberChange(batchNumber: String) {
        _uiState.update { it.copy(batchNumber = batchNumber.take(MAX_TEXT_LENGTH), submitError = null) }
    }

    fun onExpirationDateSelect(date: LocalDate) {
        _uiState.update { it.copy(expirationDate = date, submitError = null) }
    }

    fun onEntryDateSelect(date: LocalDate) {
        _uiState.update { it.copy(entryDate = date, submitError = null) }
    }

    fun onSupplierSelect(supplierId: Int?) {
        _uiState.update { it.copy(supplierId = supplierId) }
    }

    fun onInvoiceNumberChange(invoiceNumber: String) {
        _uiState.update { it.copy(invoiceNumber = invoiceNumber.take(MAX_TEXT_LENGTH)) }
    }

    fun onUnitPriceChange(unitPrice: String) {
        if (unitPrice.isDecimalInput()) _uiState.update { it.copy(unitPrice = unitPrice, submitError = null) }
    }

    fun onConfirmClick() {
        val state = _uiState.value
        val kitchen = kitchenId
        if (state.isSubmitting || kitchen == null) return

        val request = state.toRequest(kitchen)
        val error = request.exceptionOrNull()?.message
        if (error != null) {
            _uiState.update { it.copy(submitError = error) }
            return
        }

        _uiState.update { it.copy(isSubmitting = true, submitError = null) }
        viewModelScope.launch {
            stockRepository
                .registerEntry(request.getOrThrow())
                .onSuccess { batch ->
                    val registered = batch.toRegistered(state.unitSymbol)
                    _uiState.update { it.copy(isSubmitting = false, registeredBatch = registered) }
                }.onFailure { failure ->
                    _uiState.update { it.copy(isSubmitting = false, submitError = failure.message) }
                }
        }
    }

    private fun StockEntryUiState.toRequest(kitchen: Int): Result<RegisterStockEntryRequest> {
        val error = quantityError() ?: batchNumberError() ?: datesError(LocalDate.now(clock)) ?: priceError()
        if (error != null) return Result.failure(IllegalArgumentException(error))
        return runCatching {
            RegisterStockEntryRequest(
                productId = productId,
                kitchenId = kitchen,
                batchNumber = batchNumber.trim(),
                initialQuantity = requireNotNull(quantity.toDecimal()),
                entryDate = requireNotNull(entryDate).toString(),
                expirationDate = requireNotNull(expirationDate).toString(),
                supplierId = supplierId,
                invoiceNumber = invoiceNumber.trim().ifEmpty { null },
                unitPrice = unitPrice.toDecimal(),
            )
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
            // Fornecedor é opcional: se a lista não vier, o formulário segue sem ela.
            val suppliers = async { supplierRepository.allSuppliers().getOrDefault(emptyList()) }
            val productResult = product.await()
            val supplierOptions =
                suppliers
                    .await()
                    .filter { it.active }
                    .map { SupplierOption(it.id, it.legalName) }
            productResult
                .onSuccess { found ->
                    val details = listOfNotNull(found.brand, found.category?.name).joinToString(" · ")
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            productName = found.name,
                            details = details.ifEmpty { null },
                            unitSymbol = found.unit.symbol,
                            suppliers = supplierOptions,
                        )
                    }
                }.onFailure { error -> _uiState.update { it.copy(isLoading = false, errorMessage = error.message) } }
        }
    }

    companion object {
        private const val NO_KITCHEN = "Você ainda não faz parte de uma cozinha."
        private const val MAX_TEXT_LENGTH = 50

        fun factory(productId: Int): ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    val application = this[APPLICATION_KEY] as InventraApplication
                    StockEntryViewModel(
                        productId = productId,
                        productRepository = application.container.productRepository,
                        stockRepository = application.container.stockRepository,
                        supplierRepository = application.container.supplierRepository,
                        authRepository = application.container.authRepository,
                    )
                }
            }
    }
}

private val ptBr: Locale = Locale.forLanguageTag("pt-BR")
private val quantityFormat = DecimalFormat("#,##0.###", DecimalFormatSymbols(ptBr))
private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

// Limites do backend: quantidade com até 3 casas decimais e preço com até 2.
private const val QUANTITY_DECIMALS = 3
private const val PRICE_DECIMALS = 2

private fun StockEntryUiState.quantityError(): String? {
    val value = quantity.toDecimal()
    return when {
        value == null || value <= 0.0 -> "Informe a quantidade recebida"
        quantity.decimalPlaces() > QUANTITY_DECIMALS -> "Use no máximo 3 casas decimais na quantidade"
        else -> null
    }
}

private fun StockEntryUiState.batchNumberError(): String? = "Informe o número do lote".takeIf { batchNumber.isBlank() }

private fun StockEntryUiState.datesError(today: LocalDate): String? =
    when {
        expirationDate == null -> "Informe a validade"
        entryDate == null || entryDate.isAfter(today) -> "A data de entrada não pode ser no futuro"
        expirationDate.isBefore(entryDate) -> "A validade não pode ser anterior à data de entrada"
        else -> null
    }

private fun StockEntryUiState.priceError(): String? {
    if (unitPrice.isBlank()) return null
    val invalid = unitPrice.toDecimal() == null || unitPrice.decimalPlaces() > PRICE_DECIMALS
    return "Preço unitário inválido".takeIf { invalid }
}

private fun String.isDecimalInput(): Boolean = all { it.isDigit() || it == ',' || it == '.' }

private fun String.toDecimal(): Double? = trim().replace(',', '.').toDoubleOrNull()

private fun String.decimalPlaces(): Int = trim().replace(',', '.').substringAfter('.', "").length

private fun StockBatchResponse.toRegistered(unitSymbol: String) =
    RegisteredBatch(
        batchNumber = batchNumber,
        quantity = "${quantityFormat.format(initialQuantity)} $unitSymbol",
        expiration = LocalDate.parse(expirationDate).format(dateFormatter),
    )
