package com.inventraoficial.inventra.feature.notifications.ui

import androidx.lifecycle.ViewModel
import com.inventraoficial.inventra.core.designsystem.atoms.InventraStatusIconVariant
import com.inventraoficial.inventra.core.designsystem.organisms.InventraNotification
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class NotificationViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(NotificationUiState())
    val uiState: StateFlow<NotificationUiState> = _uiState.asStateFlow()

    // simula o formato que a API deve devolver no futuro: uma lista PLANA,
    // cada notificação com sua própria data. Já inclui dias mais antigos —
    // "Ver todas" só decide quanto disso aparece, não busca nada novo (ainda).
    private val allNotifications: List<InventraNotification> = buildFakeNotifications()

    init {
        refreshGroupedNotifications()
    }

    @Suppress("MagicNumber")
    private fun buildFakeNotifications(): List<InventraNotification> {
        val today = LocalDate.now()
        return listOf(
            InventraNotification(
                variant = InventraStatusIconVariant.Danger,
                title = "Leite integral vence amanhã!",
                subtitle = "Lote 000067",
                timestamp = "08:30",
                date = today,
            ),
            InventraNotification(
                variant = InventraStatusIconVariant.Info,
                title = "Novo fornecedor cadastrado",
                subtitle = "Seara",
                timestamp = "07:15",
                date = today,
            ),
            InventraNotification(
                variant = InventraStatusIconVariant.Success,
                title = "Entrada de estoque confirmada",
                subtitle = "Lote 000042",
                timestamp = "18:20",
                date = today.minusDays(1),
            ),
            InventraNotification(
                variant = InventraStatusIconVariant.Info,
                title = "Pedido de compra aprovado",
                subtitle = "Fornecedor JBS",
                timestamp = "14:10",
                date = today.minusDays(2),
            ),
            InventraNotification(
                variant = InventraStatusIconVariant.Danger,
                title = "Farinha de trigo vencida",
                subtitle = "Lote 000031",
                timestamp = "09:00",
                date = today.minusDays(7),
            ),
        )
    }

    private fun refreshGroupedNotifications() {
        val isExpanded = _uiState.value.isHistoryExpanded
        // sem expandir, mostra só hoje e ontem
        val cutoff = LocalDate.now().minusDays(1)
        val visibleNotifications =
            if (isExpanded) {
                allNotifications
            } else {
                allNotifications.filter { !it.date.isBefore(cutoff) }
            }

        _uiState.value =
            _uiState.value.copy(
                groupedNotifications = visibleNotifications.groupBy { labelForDate(it.date) },
            )
    }

    private fun labelForDate(date: LocalDate): String {
        val today = LocalDate.now()
        return when (date) {
            today -> "Hoje"
            today.minusDays(1) -> "Ontem"
            else -> date.format(DateTimeFormatter.ofPattern("d 'de' MMMM", Locale("pt", "BR")))
        }
    }

    fun onFilterSelect(filter: String) {
        _uiState.value = _uiState.value.copy(selectedFilter = filter)
    }

    fun onSeeAllClick() {
        _uiState.value = _uiState.value.copy(isHistoryExpanded = !_uiState.value.isHistoryExpanded)
        refreshGroupedNotifications()
    }
}
