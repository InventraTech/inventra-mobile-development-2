package com.inventraoficial.inventra.feature.stock.stockout.ui

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class StockOutViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(StockOutUiState())
    val uiState: StateFlow<StockOutUiState> = _uiState.asStateFlow()

    init {
        loadBatch()
    }

    private fun loadBatch() {
        // simula os dados que viriam da navegação (produto/lote selecionados na tela de detalhe)
        _uiState.value =
            _uiState.value.copy(
                productName = "Arroz Camil",
                batchCode = "000067",
            )
    }

    fun onQuantityChange(quantity: Int) {
        _uiState.value = _uiState.value.copy(quantity = quantity)
    }

    fun onReasonChange(reason: String) {
        _uiState.value = _uiState.value.copy(reason = reason)
    }

    fun onConfirmClick() {
        // sem persistência real configurada ainda
    }

    fun onCancelClick() {
        // sem navegação configurada ainda
    }

    fun onBackClick() {
        // sem navegação configurada ainda
    }
}
