package com.inventraoficial.inventra.feature.suppliers.list.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.inventraoficial.inventra.InventraApplication
import com.inventraoficial.inventra.data.remote.dto.auth.AccessType
import com.inventraoficial.inventra.data.remote.dto.supplier.SupplierResponse
import com.inventraoficial.inventra.data.repository.AuthRepository
import com.inventraoficial.inventra.data.repository.SupplierRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Suppress("TooManyFunctions")
class SupplierViewModel(
    private val supplierRepository: SupplierRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(SupplierUiState())
    val uiState: StateFlow<SupplierUiState> = _uiState.asStateFlow()

    private var allSuppliers: List<SupplierResponse> = emptyList()

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

    fun onFilterIconClick() {
        _uiState.update { it.copy(draftFilters = it.filters) }
    }

    fun onFiltersDismiss() {
        _uiState.update { it.copy(draftFilters = null) }
    }

    fun onSortSelect(sort: SupplierSort) {
        updateDraft { it.copy(sort = sort) }
    }

    fun onMinRatingSelect(minRating: MinRating) {
        updateDraft { it.copy(minRating = minRating) }
    }

    fun onContactToggle(contact: SupplierContact) {
        updateDraft { draft ->
            val contacts = if (contact in draft.contacts) draft.contacts - contact else draft.contacts + contact
            draft.copy(contacts = contacts)
        }
    }

    fun onClearFilters() {
        updateDraft { SupplierFilters() }
    }

    fun onApplyFilters() {
        val draft = _uiState.value.draftFilters ?: return
        _uiState.update { it.copy(filters = draft, draftFilters = null) }
        refreshVisibleSuppliers()
    }

    private fun updateDraft(transform: (SupplierFilters) -> SupplierFilters) {
        _uiState.update { state -> state.copy(draftFilters = state.draftFilters?.let(transform)) }
    }

    private fun load() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val user = authRepository.currentUser.first()
            val kitchenId = user?.kitchenId
            _uiState.update { it.copy(canCreate = user?.accessType == AccessType.SUPERVISOR) }
            if (kitchenId == null) {
                allSuppliers = emptyList()
                _uiState.update { it.copy(isLoading = false, suppliers = emptyList()) }
                return@launch
            }
            supplierRepository
                .suppliers(kitchenId)
                .onSuccess { suppliers ->
                    allSuppliers = suppliers
                    _uiState.update { it.copy(isLoading = false) }
                    refreshVisibleSuppliers()
                }.onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = error.message) }
                }
        }
    }

    private fun refreshVisibleSuppliers() {
        val state = _uiState.value
        val query = state.searchQuery.trim()
        val filters = state.filters
        val visibleSuppliers =
            allSuppliers
                .filter { supplier ->
                    supplier.legalName.contains(query, ignoreCase = true) &&
                        (supplier.rating ?: 0) >= filters.minRating.stars &&
                        filters.contacts.all { supplier.has(it) }
                }.sortedWith(filters.sort.comparator())
                .map { it.toSupplier() }
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

private fun SupplierResponse.has(contact: SupplierContact): Boolean =
    when (contact) {
        SupplierContact.WhatsApp -> !whatsapp.isNullOrBlank()
        SupplierContact.Email -> !email.isNullOrBlank()
    }

private fun SupplierSort.comparator(): Comparator<SupplierResponse> {
    val byName = compareBy<SupplierResponse> { it.legalName.lowercase() }
    return when (this) {
        SupplierSort.Name -> byName
        SupplierSort.Rating -> compareByDescending<SupplierResponse> { it.rating ?: 0 }.then(byName)
    }
}

private fun SupplierResponse.toSupplier() =
    Supplier(
        id = id,
        photoUrl = photoUrl,
        name = legalName,
        rating = rating ?: 0,
        badgeText = cnpj,
    )
