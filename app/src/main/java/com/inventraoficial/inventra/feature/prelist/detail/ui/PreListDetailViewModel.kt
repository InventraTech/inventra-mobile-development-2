package com.inventraoficial.inventra.feature.prelist.detail.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.inventraoficial.inventra.InventraApplication
import com.inventraoficial.inventra.data.remote.dto.prelist.PreListResponse
import com.inventraoficial.inventra.data.repository.AuthRepository
import com.inventraoficial.inventra.data.repository.PreListRepository
import com.inventraoficial.inventra.data.repository.RequisitionRepository
import com.inventraoficial.inventra.feature.prelist.createPurchaseFrom
import com.inventraoficial.inventra.feature.prelist.shortDateText
import com.inventraoficial.inventra.feature.prelist.summaryText
import com.inventraoficial.inventra.feature.requirements.quantityText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PreListDetailViewModel(
    private val preListId: Int,
    private val authRepository: AuthRepository,
    private val preListRepository: PreListRepository,
    private val requisitionRepository: RequisitionRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(PreListDetailUiState())
    val uiState: StateFlow<PreListDetailUiState> = _uiState.asStateFlow()
    private var preList: PreListResponse? = null

    init {
        load()
    }

    fun onRetryClick() {
        load()
    }

    fun onConvertClick() {
        val current = preList ?: return
        if (_uiState.value.isConverting) return
        _uiState.update { it.copy(isConverting = true, convertErrorMessage = null) }
        viewModelScope.launch {
            val kitchenId = authRepository.currentUser.first()?.kitchenId
            runCatching { requisitionRepository.createPurchaseFrom(current, requireNotNull(kitchenId) { NO_KITCHEN }) }
                .onSuccess { draftId -> _uiState.update { it.copy(isConverting = false, openedDraftId = draftId) } }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(isConverting = false, convertErrorMessage = error.message ?: CONVERT_ERROR)
                    }
                }
        }
    }

    fun onDraftOpened() {
        _uiState.update { it.copy(openedDraftId = null) }
    }

    private fun load() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            preListRepository
                .preList(preListId)
                .onSuccess { loaded ->
                    preList = loaded
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            dateText = loaded.shortDateText(),
                            summaryText = loaded.summaryText(),
                            groups = loaded.groups(),
                        )
                    }
                }.onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = error.message) }
                }
        }
    }

    companion object {
        private const val NO_KITCHEN = "Entre em uma cozinha para criar requisições."
        private const val CONVERT_ERROR = "Não foi possível transformar a pré-lista em compra."

        fun factory(preListId: Int): ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    val application = this[APPLICATION_KEY] as InventraApplication
                    PreListDetailViewModel(
                        preListId = preListId,
                        authRepository = application.container.authRepository,
                        preListRepository = application.container.preListRepository,
                        requisitionRepository = application.container.requisitionRepository,
                    )
                }
            }
    }
}

private fun PreListResponse.groups(): List<PreListSupplierGroup> =
    items
        .groupBy { it.supplier?.legalName ?: "Sem fornecedor" }
        .toSortedMap()
        .map { (supplierName, supplierItems) ->
            PreListSupplierGroup(
                supplierName = supplierName,
                items =
                    supplierItems.sortedBy { it.product.name }.map { item ->
                        PreListItemUi(
                            id = item.id,
                            name = item.product.name,
                            photoUrl = item.product.photoUrl,
                            quantityText = "Qtd. ${item.quantity.quantityText()}",
                        )
                    },
            )
        }
