package com.inventraoficial.inventra.feature.suppliers.list.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.inventraoficial.inventra.InventraApplication
import com.inventraoficial.inventra.R
import com.inventraoficial.inventra.data.remote.dto.supplier.SupplierResponse
import com.inventraoficial.inventra.data.repository.AuthRepository
import com.inventraoficial.inventra.data.repository.SupplierRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SupplierViewModel(
    private val supplierRepository: SupplierRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(SupplierUiState())
    val uiState: StateFlow<SupplierUiState> = _uiState.asStateFlow()

    private var allSuppliers: List<Supplier> = emptyList()

    init {
        load()
    }

    fun onRetryClick() {
        load()
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        refreshVisibleSuppliers()
    }

    fun onFilterIconClick() = Unit

    private fun load() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val kitchenId = authRepository.currentUser.first()?.kitchenId
            if (kitchenId == null) {
                allSuppliers = emptyList()
                _uiState.update { it.copy(isLoading = false, suppliers = emptyList()) }
                return@launch
            }
            supplierRepository
                .suppliers(kitchenId)
                .onSuccess { suppliers ->
                    allSuppliers = suppliers.map { it.toSupplier() }
                    _uiState.update { it.copy(isLoading = false) }
                    refreshVisibleSuppliers()
                }.onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = error.message) }
                }
        }
    }

    private fun refreshVisibleSuppliers() {
        val query = _uiState.value.searchQuery.trim()
        val visibleSuppliers = allSuppliers.filter { it.name.contains(query, ignoreCase = true) }
        _uiState.update { it.copy(suppliers = visibleSuppliers) }
    }

    companion object {
        val Factory: ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    val application = this[APPLICATION_KEY] as InventraApplication
                    SupplierViewModel(
                        supplierRepository = application.container.supplierRepository,
                        authRepository = application.container.authRepository,
                    )
                }
            }
    }
}

private fun SupplierResponse.toSupplier() =
    Supplier(
        id = id,
        imageRes = R.drawable.ic_inventra_logo,
        name = legalName,
        rating = rating ?: 0,
        badgeText = cnpj,
    )
