package com.inventraoficial.inventra.feature.home.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.inventraoficial.inventra.InventraApplication
import com.inventraoficial.inventra.R
import com.inventraoficial.inventra.core.designsystem.molecules.InventraStatTone
import com.inventraoficial.inventra.core.designsystem.molecules.InventraUrgency
import com.inventraoficial.inventra.core.designsystem.organisms.InventraPriorityItem
import com.inventraoficial.inventra.core.designsystem.organisms.InventraStat
import com.inventraoficial.inventra.data.remote.dto.stock.LowStockAlertResponse
import com.inventraoficial.inventra.data.remote.dto.stock.StockBatchResponse
import com.inventraoficial.inventra.data.repository.AlertRepository
import com.inventraoficial.inventra.data.repository.AuthRepository
import com.inventraoficial.inventra.data.repository.StockRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit.DAYS
import java.util.Locale

class HomeViewModel(
    private val authRepository: AuthRepository,
    private val stockRepository: StockRepository,
    private val alertRepository: AlertRepository,
    private val today: () -> LocalDate = { LocalDate.now() },
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun onRetryClick() {
        load()
    }

    fun onScreenVisible() {
        viewModelScope.launch {
            val kitchenId = authRepository.currentUser.first()?.kitchenId ?: return@launch
            alertRepository
                .unreadCount(kitchenId)
                .onSuccess { count -> _uiState.update { it.copy(unreadNotifications = count) } }
        }
    }

    private fun load() {
        val date = today()
        _uiState.update { it.copy(dateText = date.toDateText(), isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val user = authRepository.currentUser.first()
            _uiState.update { it.copy(userName = user?.name?.firstName().orEmpty()) }

            val kitchenId = user?.kitchenId
            if (kitchenId == null) {
                _uiState.update { it.copy(isLoading = false) }
                return@launch
            }

            val expiring = stockRepository.expiringSoon(kitchenId, EXPIRING_DAYS)
            val lowStock = stockRepository.lowStock()
            val error = expiring.exceptionOrNull() ?: lowStock.exceptionOrNull()
            if (error != null) {
                _uiState.update { it.copy(isLoading = false, errorMessage = error.message) }
                return@launch
            }

            val batches = expiring.getOrThrow().sortedBy { it.expirationDate }
            _uiState.update {
                it.copy(
                    priorityItems = batches.map { batch -> batch.toPriorityItem(date) },
                    stats = buildStats(batches, lowStock.getOrThrow(), date),
                    isLoading = false,
                )
            }
        }
    }

    companion object {
        const val EXPIRING_DAYS = 7

        val Factory: ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    val application = this[APPLICATION_KEY] as InventraApplication
                    HomeViewModel(
                        authRepository = application.container.authRepository,
                        stockRepository = application.container.stockRepository,
                        alertRepository = application.container.alertRepository,
                    )
                }
            }
    }
}

private val ptBr: Locale = Locale.forLanguageTag("pt-BR")
private val dateFormatter = DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM 'de' yyyy", ptBr)

private fun LocalDate.toDateText(): String = format(dateFormatter).replaceFirstChar { it.titlecase(ptBr) }

private fun String.firstName(): String = trim().substringBefore(" ")

private fun StockBatchResponse.daysLeft(today: LocalDate) = DAYS.between(today, LocalDate.parse(expirationDate))

private fun StockBatchResponse.toPriorityItem(today: LocalDate): InventraPriorityItem {
    val days = daysLeft(today)
    return InventraPriorityItem(
        imageRes = R.drawable.ic_inventra_logo,
        title = product.name,
        statusText =
            when (days) {
                0L -> "Vence hoje"
                1L -> "Vence amanhã"
                else -> "Vence em $days dias"
            },
        urgency = if (days <= 1) InventraUrgency.Tomorrow else InventraUrgency.Normal,
        batch = "Lote $batchNumber",
        productId = product.id,
    )
}

private fun buildStats(
    batches: List<StockBatchResponse>,
    lowStock: List<LowStockAlertResponse>,
    today: LocalDate,
): List<InventraStat> {
    val urgent = batches.count { it.daysLeft(today) <= 1 }
    val valueAtRisk = batches.sumOf { it.currentQuantity * (it.unitPrice ?: 0.0) }
    return listOf(
        InventraStat(
            label = "Vencendo",
            value = batches.size.plural("lote", "lotes"),
            trend = "nos próximos ${HomeViewModel.EXPIRING_DAYS} dias",
            tone = batches.size.tone(),
        ),
        InventraStat(
            label = "Urgentes",
            value = urgent.plural("lote", "lotes"),
            trend = "vencem hoje ou amanhã",
            tone = urgent.tone(),
        ),
        InventraStat(
            label = "Valor em risco",
            value = String.format(ptBr, "R$ %,.2f", valueAtRisk),
            trend = "em lotes a vencer",
            tone = if (valueAtRisk > 0) InventraStatTone.Negative else InventraStatTone.Positive,
        ),
        InventraStat(
            label = "Estoque baixo",
            value = lowStock.size.plural("produto", "produtos"),
            trend = "abaixo do mínimo",
            tone = lowStock.size.tone(),
        ),
    )
}

private fun Int.plural(
    singular: String,
    plural: String,
): String = "$this ${if (this == 1) singular else plural}"

private fun Int.tone(): InventraStatTone = if (this > 0) InventraStatTone.Negative else InventraStatTone.Positive
