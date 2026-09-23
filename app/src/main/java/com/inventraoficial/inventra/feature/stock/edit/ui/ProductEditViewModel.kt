package com.inventraoficial.inventra.feature.stock.edit.ui

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ProductEditViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ProductEditUiState())
    val uiState: StateFlow<ProductEditUiState> = _uiState.asStateFlow()

    init {
        loadProduct()
    }

    private fun loadProduct() {
        // simula o produto ja carregado da tela de detalhe, vindo da API no futuro
        _uiState.value =
            _uiState.value.copy(
                productName = "Leite Integral",
                category = "Laticínios",
                supplier = "Seara Alimentos",
                storage = "Câmara fria 02",
                quantity = "18 L",
            )
    }

    fun onProductNameChange(value: String) {
        _uiState.value = _uiState.value.copy(productName = value)
    }

    fun onCategoryChange(value: String) {
        _uiState.value = _uiState.value.copy(category = value)
    }

    fun onSupplierChange(value: String) {
        _uiState.value = _uiState.value.copy(supplier = value)
    }

    fun onStorageChange(value: String) {
        _uiState.value = _uiState.value.copy(storage = value)
    }

    fun onQuantityChange(value: String) {
        _uiState.value = _uiState.value.copy(quantity = value)
    }

    fun onSaveClick() {
        // sem persistencia real configurada ainda
    }
}
