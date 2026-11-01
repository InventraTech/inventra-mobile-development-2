package com.inventraoficial.inventra.feature.stock.edit.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.inventraoficial.inventra.InventraApplication
import com.inventraoficial.inventra.data.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProductDeactivateUiState(
    val isConfirmVisible: Boolean = false,
    val isDeactivating: Boolean = false,
    val errorMessage: String? = null,
    val isDeactivated: Boolean = false,
)

class ProductDeactivateViewModel(
    private val productId: Int,
    private val productRepository: ProductRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProductDeactivateUiState())
    val uiState: StateFlow<ProductDeactivateUiState> = _uiState.asStateFlow()

    fun onDeactivateClick() = _uiState.update { it.copy(isConfirmVisible = true, errorMessage = null) }

    fun onDismiss() = _uiState.update { it.copy(isConfirmVisible = false) }

    fun onConfirm() {
        if (_uiState.value.isDeactivating) return
        _uiState.update { it.copy(isConfirmVisible = false, isDeactivating = true, errorMessage = null) }
        viewModelScope.launch {
            productRepository
                .deactivate(productId)
                .onSuccess { _uiState.update { it.copy(isDeactivating = false, isDeactivated = true) } }
                .onFailure { error ->
                    _uiState.update { it.copy(isDeactivating = false, errorMessage = error.message ?: ERROR) }
                }
        }
    }

    companion object {
        private const val ERROR = "Não foi possível desativar o produto."

        fun factory(productId: Int): ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    val application = this[APPLICATION_KEY] as InventraApplication
                    ProductDeactivateViewModel(productId, application.container.productRepository)
                }
            }
    }
}
