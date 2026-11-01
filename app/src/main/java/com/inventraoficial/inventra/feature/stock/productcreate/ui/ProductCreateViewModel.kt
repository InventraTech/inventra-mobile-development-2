package com.inventraoficial.inventra.feature.stock.productcreate.ui

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
import com.inventraoficial.inventra.data.remote.dto.product.CreateProductRequest
import com.inventraoficial.inventra.data.remote.dto.product.KitchenParametersRequest
import com.inventraoficial.inventra.data.remote.dto.product.UnitResponse
import com.inventraoficial.inventra.data.repository.AuthRepository
import com.inventraoficial.inventra.data.repository.ProductRepository
import com.inventraoficial.inventra.data.repository.SupplierRepository
import com.inventraoficial.inventra.feature.stock.productform.MAX_BRAND_LENGTH
import com.inventraoficial.inventra.feature.stock.productform.MAX_NAME_LENGTH
import com.inventraoficial.inventra.feature.stock.productform.ProductFormState
import com.inventraoficial.inventra.feature.stock.productform.productFormActions
import com.inventraoficial.inventra.feature.stock.productform.toCategoryOptions
import com.inventraoficial.inventra.feature.stock.productform.toDecimal
import com.inventraoficial.inventra.feature.stock.productform.toSupplierOptions
import com.inventraoficial.inventra.feature.stock.productform.toUnitOptions
import com.inventraoficial.inventra.feature.stock.productform.validationError
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
    private val supplierRepository: SupplierRepository,
) : ViewModel() {
    private val _uiState =
        MutableStateFlow(
            ProductCreateUiState(form = ProductFormState(barcode = barcode.orEmpty(), isSupplierRequired = true)),
        )
    val uiState: StateFlow<ProductCreateUiState> = _uiState.asStateFlow()

    val formActions =
        productFormActions { transform ->
            _uiState.update { it.copy(form = transform(it.form), submitError = null) }
        }

    init {
        load()
    }

    fun onRetryClick() = load()

    fun onSaveClick() {
        val state = _uiState.value
        if (state.isSubmitting) return
        val form = state.form
        val unitId = form.unitId
        val supplierId = form.supplierId
        val error = form.validationError()
        if (error != null || unitId == null || supplierId == null) {
            _uiState.update { it.copy(submitError = error) }
            return
        }

        val request =
            CreateProductRequest(
                name = form.name.trim(),
                brand = form.brand.trim().ifEmpty { null },
                categoryId = form.categoryId,
                unitId = unitId,
                barcode = form.barcode.trim().ifEmpty { null },
                supplierId = supplierId,
                referencePrice = form.referencePrice.toDecimal(),
            )
        _uiState.update { it.copy(isSubmitting = true, submitError = null) }
        viewModelScope.launch {
            productRepository
                .create(request)
                .onSuccess { product ->
                    val kitchenLinked = linkToKitchen(product.id, form.minStock.toDecimal())
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            createdProduct = SavedProduct(product.id, product.name),
                            kitchenLinkFailed = !kitchenLinked,
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
            val suppliers = async { supplierRepository.allSuppliers().getOrDefault(emptyList()) }
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
            val supplierOptions = suppliers.await()
            val found = lookup?.await()?.getOrNull()
            _uiState.update {
                it
                    .copy(
                        isLoading = false,
                        form =
                            it.form.copy(
                                units = unitOptions.toUnitOptions(),
                                categories = categoryOptions.toCategoryOptions(),
                                suppliers = supplierOptions.toSupplierOptions(),
                            ),
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
                        supplierRepository = application.container.supplierRepository,
                    )
                }
            }
    }
}

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
                form =
                    form.copy(
                        name = suggestion.name.orEmpty().take(MAX_NAME_LENGTH),
                        brand = suggestion.brand.orEmpty().take(MAX_BRAND_LENGTH),
                        unitId = suggestion.quantity?.let { units.guessUnitId(it) },
                    ),
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
