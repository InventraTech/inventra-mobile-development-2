package com.inventraoficial.inventra.feature.requirements.history.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inventraoficial.inventra.R
import com.inventraoficial.inventra.feature.requirements.RequirementsSharedData
import com.inventraoficial.inventra.feature.requirements.list.ui.RequirementStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

private const val SUBSCRIPTION_TIMEOUT_MS = 5000L

class RequirementsHistoryViewModel : ViewModel() {
    private val initialHistoryItems: List<HistoryItem> = buildFakeHistoryItems()
    
    private val _uiState = MutableStateFlow(RequirementsHistoryUiState(historyItems = initialHistoryItems))
    val uiState: StateFlow<RequirementsHistoryUiState> = 
        combine(
            _uiState,
            RequirementsSharedData.processedItems
        ) { currentState, newItems ->
            val allItems = newItems + initialHistoryItems
            currentState.copy(historyItems = allItems)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT_MS),
            initialValue = _uiState.value
        )

    private fun buildFakeHistoryItems(): List<HistoryItem> =
        listOf(
            HistoryItem(
                id = "h1",
                imageRes = R.drawable.ic_inventra_logo,
                productName = "Baião - Arroz Camil",
                description = "Solicitação aprovada",
                status = RequirementStatus.ACCEPTED,
            ),
            HistoryItem(
                id = "h2",
                imageRes = R.drawable.ic_inventra_logo,
                productName = "Eduardo Passos",
                description = "Entrada como Estoquista recusada",
                status = RequirementStatus.REJECTED,
            ),
            HistoryItem(
                id = "h3",
                imageRes = R.drawable.ic_inventra_logo,
                productName = "Compra - Leite Integral",
                description = "Solicitação aprovada",
                status = RequirementStatus.ACCEPTED,
            ),
        )
}
