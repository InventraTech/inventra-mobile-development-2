package com.inventraoficial.inventra.feature.suppliers.ui

import androidx.lifecycle.ViewModel
import com.inventraoficial.inventra.R
import com.inventraoficial.inventra.core.designsystem.organisms.InventraBottomDestination
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SupplierViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(SupplierUiState())
    val uiState: StateFlow<SupplierUiState> = _uiState.asStateFlow()

    // simula o formato que a API deve devolver no futuro
    private val allSuppliers: List<Supplier> = buildFakeSuppliers()

    init {
        refreshVisibleSuppliers()
    }

    @Suppress("MagicNumber")
    private fun buildFakeSuppliers(): List<Supplier> =
        listOf(
            Supplier(R.drawable.ic_inventra_logo, "Seara Alimentos", 5, "VPQ 50", "Carnes"),
            Supplier(R.drawable.ic_inventra_logo, "Friboi", 4, "VPQ 44", "Carnes"),
            Supplier(R.drawable.ic_inventra_logo, "Piracanjuba", 4, "VPQ 41", "Laticínios"),
            Supplier(R.drawable.ic_inventra_logo, "Camil Alimentos", 3, "VPQ 35", "Grãos"),
            Supplier(R.drawable.ic_inventra_logo, "JBS", 5, "VPQ 48", "Carnes"),
        )

    private fun refreshVisibleSuppliers() {
        val query = _uiState.value.searchQuery
        val filter = _uiState.value.selectedFilter

        val visibleSuppliers =
            allSuppliers.filter { supplier ->
                val matchesFilter = filter == "Todos" || supplier.category == filter
                val matchesQuery = supplier.name.contains(query, ignoreCase = true)
                matchesFilter && matchesQuery
            }

        _uiState.value = _uiState.value.copy(suppliers = visibleSuppliers)
    }

    fun onSearchQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        refreshVisibleSuppliers()
    }

    fun onFilterSelect(filter: String) {
        _uiState.value = _uiState.value.copy(selectedFilter = filter)
        refreshVisibleSuppliers()
    }

    fun onFilterIconClick() {
        // sem comportamento definido ainda
    }

    @Suppress("UnusedParameter")
    fun onSupplierClick(supplier: Supplier) {
        // sem navegação configurada ainda
    }

    fun onQrScanClick() {
        // sem navegação configurada ainda
    }

    @Suppress("UnusedParameter")
    fun onBottomNavSelect(destination: InventraBottomDestination) {
        // sem navegação configurada ainda
    }
}
