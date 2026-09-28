package com.inventraoficial.inventra.feature.suppliers.detail.ui

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SupplierDetailViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(SupplierDetailUiState())
    val uiState: StateFlow<SupplierDetailUiState> = _uiState.asStateFlow()

    init {
        loadSupplierDetail()
    }

    @Suppress("MagicNumber")
    private fun loadSupplierDetail() {
        // simula o formato que a API deve devolver no futuro
        _uiState.value =
            _uiState.value.copy(
                name = "Seara Alimentos",
                rating = 5,
                badgeText = "VPQ 50",
                phone = "(11) 4002-8922",
                email = "contato@seara.com.br",
                address = "Av. das Indústrias, 450 – São Paulo, SP",
                products =
                    listOf(
                        SupplierProduct(name = "Arroz Camil", infoText = "R\$7,00/un · 3 dias"),
                        SupplierProduct(name = "Feijão Carioca", infoText = "R\$7,00/un · 3 dias"),
                    ),
                deliveryHistory =
                    listOf(
                        DeliveryHistoryEntry(date = "14/08/2026", productName = "Arroz Camil", quantityLabel = "12 un"),
                        DeliveryHistoryEntry(date = "02/08/2026", productName = "Feijão Carioca", quantityLabel = "8 un"),
                    ),
            )
    }
}
