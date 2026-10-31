package com.inventraoficial.inventra.feature.stock.productcreate.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.inventraoficial.inventra.InventraApplication
import com.inventraoficial.inventra.core.designsystem.organisms.InventraPickerOption
import com.inventraoficial.inventra.data.remote.dto.auth.AccessType
import com.inventraoficial.inventra.data.remote.dto.product.BarcodeLookupResponse
import com.inventraoficial.inventra.data.remote.dto.product.BarcodeSource
import com.inventraoficial.inventra.data.remote.dto.product.CategoryResponse
import com.inventraoficial.inventra.data.remote.dto.product.CreateProductRequest
import com.inventraoficial.inventra.data.remote.dto.product.KitchenParametersRequest
import com.inventraoficial.inventra.data.remote.dto.product.UnitResponse
import com.inventraoficial.inventra.data.repository.AuthRepository
import com.inventraoficial.inventra.data.repository.ProductRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Cadastro de produto pelo supervisor, normalmente a partir de um código escaneado que não está no catálogo.
// Com o código, já preenche nome, marca e unidade com o que o Open Food Facts conhece.
class ProductCreateViewModel(
    private val barcode: String?,
    private val productRepository: ProductRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProductCreateUiState(barcode = barcode.orEmpty()))
    val uiState: StateFlow<ProductCreateUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun onRetryClick() = load()

    fun onNameChange(name: String) {
        _uiState.update { it.copy(name = name.take(MAX_NAME_LENGTH), submitError = null) }
    }

    fun onBrandChange(brand: String) {
        _uiState.update { it.copy(brand = brand.take(MAX_BRAND_LENGTH)) }
    }

    fun onBarcodeChange(barcode: String) {
        if (barcode.all { it.isDigit() }) {
            _uiState.update { it.copy(barcode = barcode.take(MAX_BARCODE_LENGTH), submitError = null) }
        }
    }

    fun onUnitSelect(unitId: Int?) {
        _uiState.update { it.copy(unitId = unitId, submitError = null) }
    }

    fun onCategorySelect(categoryId: Int?) {
        _uiState.update { it.copy(categoryId = categoryId) }
    }

    fun onMinStockChange(minStock: String) {
        if (minStock.matches(decimalInput)) {
            _uiState.update { it.copy(minStock = minStock, submitError = null) }
        }
    }

    fun onSaveClick() {
        val state = _uiState.value
        if (state.isSubmitting) return
        val unitId = state.unitId
        val minStock = state.minStock.toDecimal()
        val error =
            when {
                state.name.isBlank() -> "Informe o nome do produto"
                unitId == null -> "Escolha a unidade de medida"
                state.minStock.isNotBlank() && minStock == null -> "Informe o estoque mínimo só com números"
                else -> null
            }
        if (error != null || unitId == null) {
            _uiState.update { it.copy(submitError = error) }
            return
        }

        val request =
            CreateProductRequest(
                name = state.name.trim(),
                brand = state.brand.trim().ifEmpty { null },
                categoryId = state.categoryId,
                unitId = unitId,
                barcode = state.barcode.trim().ifEmpty { null },
            )
        _uiState.update { it.copy(isSubmitting = true, submitError = null) }
        viewModelScope.launch {
            productRepository
                .create(request)
                .onSuccess { product ->
                    val linked = linkToKitchen(product.id, minStock)
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            createdProduct = SavedProduct(product.id, product.name),
                            kitchenLinkFailed = !linked,
                        )
                    }
                }.onFailure { failure ->
                    _uiState.update { it.copy(isSubmitting = false, submitError = failure.message) }
                }
        }
    }

    private suspend fun linkToKitchen(
        productId: Int,
        minStock: Double?,
    ): Boolean {
        val kitchenId = authRepository.currentUser.first()?.kitchenId ?: return false
        return productRepository
            .setKitchenParameters(productId, KitchenParametersRequest(kitchenId, minStock))
            .isSuccess
    }

    private fun load() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val user = authRepository.currentUser.first()
            if (user?.accessType != AccessType.SUPERVISOR) {
                _uiState.update { it.copy(isLoading = false, errorMessage = ONLY_SUPERVISOR) }
                return@launch
            }
            val units = async { productRepository.units() }
            // Categoria é opcional: se a lista não vier, o formulário segue sem ela.
            val categories = async { productRepository.categories().getOrDefault(emptyList()) }
            // Sem a consulta do código o cadastro continua, só não vem preenchido.
            val lookup = barcode?.let { code -> async { productRepository.findByBarcode(code, user.kitchenId) } }

            val unitsResult = units.await()
            val unitOptions = unitsResult.getOrNull()
            if (unitOptions == null) {
                val message = unitsResult.exceptionOrNull()?.message
                _uiState.update { it.copy(isLoading = false, errorMessage = message) }
                return@launch
            }
            val categoryOptions = categories.await()
            val found = lookup?.await()?.getOrNull()
            _uiState.update {
                it
                    .copy(
                        isLoading = false,
                        units = unitOptions.toUnitOptions(),
                        categories = categoryOptions.toCategoryOptions(),
                    ).prefilledWith(found, unitOptions)
            }
        }
    }

    companion object {
        private const val ONLY_SUPERVISOR = "Só o supervisor da cozinha pode cadastrar produtos."

        fun factory(barcode: String?): ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    val application = this[APPLICATION_KEY] as InventraApplication
                    ProductCreateViewModel(
                        barcode = barcode,
                        productRepository = application.container.productRepository,
                        authRepository = application.container.authRepository,
                    )
                }
            }
    }
}

// Limites do backend.
private const val MAX_NAME_LENGTH = 150
private const val MAX_BRAND_LENGTH = 80
private const val MAX_BARCODE_LENGTH = 50

private val decimalInput = Regex("^\\d{0,9}([.,]\\d{0,3})?$")

private fun String.toDecimal(): Double? = trim().replace(',', '.').toDoubleOrNull()

// Unidades simples (KG, L, UN...) primeiro; as embalagens com tamanho no código (CX500G...) depois.
private fun List<UnitResponse>.toUnitOptions(): List<InventraPickerOption> =
    sortedBy { unit -> unit.symbol.any { it.isDigit() } }
        .map { unit ->
            val description = unit.description
            if (description == null) {
                InventraPickerOption(unit.id, unit.symbol)
            } else {
                InventraPickerOption(unit.id, description, unit.symbol)
            }
        }

private fun List<CategoryResponse>.toCategoryOptions(): List<InventraPickerOption> =
    sortedBy { it.name.lowercase() }.map { InventraPickerOption(it.id, it.name) }

private fun ProductCreateUiState.prefilledWith(
    found: BarcodeLookupResponse?,
    units: List<UnitResponse>,
): ProductCreateUiState {
    val product = found?.product
    val suggestion = found?.suggestion
    return when {
        found?.source == BarcodeSource.CATALOG && product != null ->
            copy(existingProduct = SavedProduct(product.id, product.name))
        suggestion != null ->
            copy(
                isPrefilled = suggestion.name != null || suggestion.brand != null,
                name = suggestion.name.orEmpty().take(MAX_NAME_LENGTH),
                brand = suggestion.brand.orEmpty().take(MAX_BRAND_LENGTH),
                unitId = suggestion.quantity?.let { units.guessUnitId(it) },
            )
        else -> this
    }
}

// "2 L", "350ml", "12 x 1,5 kg": usa a última unidade que aparecer.
private fun List<UnitResponse>.guessUnitId(quantity: String): Int? {
    val symbol =
        quantityUnit
            .findAll(quantity)
            .lastOrNull()
            ?.groupValues
            ?.get(1) ?: return null
    return firstOrNull { it.symbol.equals(symbol, ignoreCase = true) }?.id
}

private val quantityUnit = Regex("\\d\\s*(kg|g|l|ml)\\b", RegexOption.IGNORE_CASE)
