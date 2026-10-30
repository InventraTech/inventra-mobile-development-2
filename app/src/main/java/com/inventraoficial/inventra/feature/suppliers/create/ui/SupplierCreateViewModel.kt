package com.inventraoficial.inventra.feature.suppliers.create.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.inventraoficial.inventra.InventraApplication
import com.inventraoficial.inventra.data.remote.dto.supplier.CreateSupplierRequest
import com.inventraoficial.inventra.data.repository.SupplierRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SupplierCreateViewModel(
    private val supplierRepository: SupplierRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(SupplierCreateUiState())
    val uiState: StateFlow<SupplierCreateUiState> = _uiState.asStateFlow()

    fun onLegalNameChange(name: String) = updateForm { it.copy(legalName = name.take(MAX_NAME_LENGTH)) }

    fun onCnpjChange(cnpj: String) {
        if (cnpj.all { it.isDigit() }) updateForm { it.copy(cnpj = cnpj.take(CNPJ_DIGITS)) }
    }

    fun onEmailChange(email: String) = updateForm { it.copy(email = email.trim().take(MAX_EMAIL_LENGTH)) }

    fun onWhatsappChange(whatsapp: String) {
        if (whatsapp.all { it.isDigit() }) updateForm { it.copy(whatsapp = whatsapp.take(MAX_WHATSAPP_LENGTH)) }
    }

    fun onRatingSelect(rating: Int) = updateForm { it.copy(rating = if (it.rating == rating) 0 else rating) }

    fun onSaveClick() {
        val state = _uiState.value
        if (state.isSubmitting) return
        val error = state.validationError()
        if (error != null) {
            _uiState.update { it.copy(submitError = error) }
            return
        }
        val request =
            CreateSupplierRequest(
                legalName = state.legalName.trim(),
                cnpj = state.cnpj.formatCnpj(),
                email = state.email.ifBlank { null },
                whatsapp = state.whatsapp.ifBlank { null },
                rating = state.rating.takeIf { it > 0 },
            )
        _uiState.update { it.copy(isSubmitting = true, submitError = null) }
        viewModelScope.launch {
            supplierRepository
                .create(request)
                .onSuccess { supplier ->
                    _uiState.update {
                        it.copy(isSubmitting = false, created = CreatedSupplier(supplier.id, supplier.legalName))
                    }
                }.onFailure { failure ->
                    val existing = findByCnpj(state.cnpj)
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            submitError = if (existing == null) failure.message else null,
                            existing = existing,
                        )
                    }
                }
        }
    }

    fun onUseExistingClick() {
        val existing = _uiState.value.existing ?: return
        _uiState.update { it.copy(created = existing) }
    }

    private suspend fun findByCnpj(cnpj: String): CreatedSupplier? =
        supplierRepository
            .allSuppliers()
            .getOrNull()
            ?.firstOrNull { supplier -> supplier.cnpj.filter { it.isDigit() } == cnpj }
            ?.let { CreatedSupplier(it.id, it.legalName) }

    private fun updateForm(transform: (SupplierCreateUiState) -> SupplierCreateUiState) {
        _uiState.update { transform(it).copy(submitError = null, existing = null) }
    }

    companion object {
        val Factory: ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    val application = this[APPLICATION_KEY] as InventraApplication
                    SupplierCreateViewModel(application.container.supplierRepository)
                }
            }
    }
}

private const val MAX_NAME_LENGTH = 150
private const val MAX_EMAIL_LENGTH = 150
private const val MAX_WHATSAPP_LENGTH = 20
private const val CNPJ_DIGITS = 14

private val emailFormat = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")

private fun SupplierCreateUiState.validationError(): String? =
    when {
        legalName.isBlank() -> "Informe a razão social do fornecedor"
        cnpj.length != CNPJ_DIGITS -> "O CNPJ precisa ter 14 números"
        email.isNotBlank() && !email.matches(emailFormat) -> "Informe um e-mail válido"
        else -> null
    }

private val cnpjParts = Regex("^(\\d{2})(\\d{3})(\\d{3})(\\d{4})(\\d{2})$")

internal fun String.formatCnpj(): String = replace(cnpjParts, "$1.$2.$3/$4-$5")
