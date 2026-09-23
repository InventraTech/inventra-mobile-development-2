package com.inventraoficial.inventra.feature.stock.detail.ui

import androidx.lifecycle.ViewModel
import com.inventraoficial.inventra.core.designsystem.organisms.InventraBatch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class StockDetailViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(StockDetailUiState())
    val uiState: StateFlow<StockDetailUiState> = _uiState.asStateFlow()

    init {
        loadProductDetail()
    }

    private fun loadProductDetail() {
        // simula o formato que a API deve devolver no futuro
        _uiState.value =
            _uiState.value.copy(
                productName = "Leite Integral",
                totalValue = "18 L",
                totalCaption = "Em 3 lotes de 6 litros.",
                batches =
                    listOf(
                        InventraBatch(dateLabel = "AMANHÃ", code = "000067", quantity = "6 L", isUrgent = true),
                        InventraBatch(dateLabel = "06/07/2026", code = "000069", quantity = "6 L"),
                        InventraBatch(dateLabel = "15/07/2026", code = "000071", quantity = "6 L"),
                    ),
                supplier = "Seara Alimentos",
                storage = "Câmara fria 02",
            )
    }

    fun onSeeAllBatchesClick() {
        // sem tela de lista de lotes ainda
    }

    fun onEntriesClick() {
        // sem tela de histórico de entradas ainda
    }

    fun onExitsClick() {
        // sem tela de histórico de saídas ainda
    }
}
