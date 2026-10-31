package com.inventraoficial.inventra.feature.stock.edit.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.inventraoficial.inventra.InventraApplication
import com.inventraoficial.inventra.data.remote.dto.auth.AccessType
import com.inventraoficial.inventra.data.remote.dto.product.KitchenParametersRequest
import com.inventraoficial.inventra.data.remote.dto.product.KitchenParametersResponse
import com.inventraoficial.inventra.data.remote.dto.product.ProductResponse
import com.inventraoficial.inventra.data.remote.dto.product.UpdateProductRequest
import com.inventraoficial.inventra.data.repository.AuthRepository
import com.inventraoficial.inventra.data.repository.ProductRepository
import com.inventraoficial.inventra.feature.stock.productform.MAX_BARCODE_LENGTH
import com.inventraoficial.inventra.feature.stock.productform.MAX_BRAND_LENGTH
import com.inventraoficial.inventra.feature.stock.productform.MAX_NAME_LENGTH
import com.inventraoficial.inventra.feature.stock.productform.ProductFormState
import com.inventraoficial.inventra.feature.stock.productform.isMinStockInput
import com.inventraoficial.inventra.feature.stock.productform.toCategoryOptions
import com.inventraoficial.inventra.feature.stock.productform.toDecimal
import com.inventraoficial.inventra.feature.stock.productform.toMinStockText
import com.inventraoficial.inventra.feature.stock.productform.toUnitOptions
import com.inventraoficial.inventra.feature.stock.productform.validationError
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProductEditViewModel(
    private val productId: Int,
    private val productRepository: ProductRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProductEditUiState())
    val uiState: StateFlow<ProductEditUiState> = _uiState.asStateFlow()

    private var kitchenId: Int? = null
    private var savedParameters: KitchenParametersResponse? = null
    private var savedMinStock = ""

    init {
        load()
    }

    fun onRetryClick() = load()

    fun onNameChange(name: String) = updateForm { it.copy(name = name.take(MAX_NAME_LENGTH)) }

    fun onBrandChange(brand: String) = updateForm { it.copy(brand = brand.take(MAX_BRAND_LENGTH)) }

    fun onBarcodeChange(barcode: String) {
        if (barcode.all { it.isDigit() }) updateForm { it.copy(barcode = barcode.take(MAX_BARCODE_LENGTH)) }
    }

    fun onUnitSelect(unitId: Int?) = updateForm { it.copy(unitId = unitId) }

    fun onCategorySelect(categoryId: Int?) = updateForm { it.copy(categoryId = categoryId) }

    fun onMinStockChange(minStock: String) {
        if (minStock.isMinStockInput()) updateForm { it.copy(minStock = minStock) }
    }

    fun onSaveClick() {
        val state = _uiState.value
        if (state.isSubmitting) return
        val form = state.form
        val unitId = form.unitId
        val error = form.validationError()
        if (error != null || unitId == null) {
            _uiState.update { it.copy(submitError = error) }
            return
        }
        val request =
            UpdateProductRequest(
                name = form.name.trim(),
                brand = form.brand.trim().ifEmpty { null },
                categoryId = form.categoryId,
                unitId = unitId,
                barcode = form.barcode.trim().ifEmpty { null },
            )
        _uiState.update { it.copy(isSubmitting = true, submitError = null) }
        viewModelScope.launch {
            val result =
                productRepository.update(productId, request).mapCatching {
                    if (form.minStock != savedMinStock) {
                        val kitchen = checkNotNull(kitchenId) { NO_KITCHEN }
                        val parameters = savedParameters.withMinStock(kitchen, form.minStock.toDecimal())
                        productRepository.setKitchenParameters(productId, parameters).getOrThrow()
                    }
                }
            result
                .onSuccess { _uiState.update { it.copy(isSubmitting = false, isSaved = true) } }
                .onFailure { failure ->
                    _uiState.update { it.copy(isSubmitting = false, submitError = failure.message) }
                }
        }
    }

    private fun updateForm(transform: (ProductFormState) -> ProductFormState) {
        _uiState.update { it.copy(form = transform(it.form), submitError = null) }
    }

    private fun load() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val user = authRepository.currentUser.first()
            if (user?.accessType != AccessType.SUPERVISOR) {
                _uiState.update { it.copy(isLoading = false, errorMessage = ONLY_SUPERVISOR) }
                return@launch
            }
            kitchenId = user.kitchenId
            val product = async { productRepository.product(productId) }
            val units = async { productRepository.units() }
            val categories = async { productRepository.categories().getOrDefault(emptyList()) }
            val parameters =
                async { user.kitchenId?.let { productRepository.kitchenParameters(productId, it).getOrNull() } }

            val productResult = product.await()
            val unitsResult = units.await()
            val loadError = productResult.exceptionOrNull() ?: unitsResult.exceptionOrNull()
            if (loadError != null) {
                _uiState.update { it.copy(isLoading = false, errorMessage = loadError.message) }
                return@launch
            }
            savedParameters = parameters.await()
            savedMinStock = savedParameters?.minStock?.toMinStockText().orEmpty()
            val form =
                productResult.getOrThrow().toForm(savedMinStock).copy(
                    units = unitsResult.getOrThrow().toUnitOptions(),
                    categories = categories.await().toCategoryOptions(),
                )
            _uiState.update { it.copy(isLoading = false, form = form) }
        }
    }

    companion object {
        private const val ONLY_SUPERVISOR = "Só o supervisor da cozinha pode editar produtos."
        private const val NO_KITCHEN = "Entre em uma cozinha para definir o estoque mínimo."

        fun factory(productId: Int): ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    val application = this[APPLICATION_KEY] as InventraApplication
                    ProductEditViewModel(
                        productId = productId,
                        productRepository = application.container.productRepository,
                        authRepository = application.container.authRepository,
                    )
                }
            }
    }
}

private fun KitchenParametersResponse?.withMinStock(
    kitchenId: Int,
    minStock: Double?,
) = KitchenParametersRequest(
    kitchenId = kitchenId,
    minStock = minStock,
    maxStock = this?.maxStock,
    averageDailyConsumption = this?.averageDailyConsumption,
)

private fun ProductResponse.toForm(minStock: String) =
    ProductFormState(
        name = name,
        brand = brand.orEmpty(),
        barcode = barcode.orEmpty(),
        unitId = unit.id,
        categoryId = category?.id,
        minStock = minStock,
    )
