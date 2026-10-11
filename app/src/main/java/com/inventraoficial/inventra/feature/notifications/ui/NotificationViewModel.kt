package com.inventraoficial.inventra.feature.notifications.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.inventraoficial.inventra.InventraApplication
import com.inventraoficial.inventra.core.designsystem.atoms.InventraStatusIconVariant
import com.inventraoficial.inventra.core.designsystem.organisms.InventraNotification
import com.inventraoficial.inventra.data.remote.dto.alert.AlertResponse
import com.inventraoficial.inventra.data.remote.dto.alert.AlertSeverity
import com.inventraoficial.inventra.data.repository.AlertRepository
import com.inventraoficial.inventra.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class NotificationViewModel(
    private val alertRepository: AlertRepository,
    private val authRepository: AuthRepository,
    private val zone: ZoneId = ZoneId.systemDefault(),
    private val today: () -> LocalDate = { LocalDate.now(zone) },
) : ViewModel() {
    private val _uiState = MutableStateFlow(NotificationUiState())
    val uiState: StateFlow<NotificationUiState> = _uiState.asStateFlow()

    private var alerts: List<AlertResponse> = emptyList()

    fun onScreenVisible() {
        load()
    }

    fun onRetryClick() {
        load()
    }

    fun onSeeAllClick() {
        _uiState.update { it.copy(isHistoryExpanded = !it.isHistoryExpanded) }
        refreshGroupedNotifications()
    }

    fun onNotificationClick(notification: InventraNotification) {
        if (!notification.isUnread) return
        setRead(notification.id, read = true)
        viewModelScope.launch {
            alertRepository
                .markAsRead(notification.id)
                .onFailure { setRead(notification.id, read = false) }
        }
    }

    private fun load() {
        _uiState.update { it.copy(isLoading = alerts.isEmpty(), errorMessage = null) }
        viewModelScope.launch {
            val kitchenId = authRepository.currentUser.first()?.kitchenId
            if (kitchenId == null) {
                alerts = emptyList()
                _uiState.update { it.copy(isLoading = false) }
                refreshGroupedNotifications()
                return@launch
            }
            alertRepository
                .alerts(kitchenId)
                .onSuccess { result ->
                    alerts = result.sortedByDescending { LocalDateTime.parse(it.createdAt) }
                    _uiState.update { it.copy(isLoading = false) }
                    refreshGroupedNotifications()
                }.onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = error.message) }
                }
        }
    }

    private fun setRead(
        id: Int,
        read: Boolean,
    ) {
        alerts = alerts.map { if (it.id == id) it.copy(read = read) else it }
        refreshGroupedNotifications()
    }

    private fun refreshGroupedNotifications() {
        val state = _uiState.value
        val cutoff = today().minusDays(1)
        val visible =
            alerts
                .map { it.toNotification(zone) }
                .filter { state.isHistoryExpanded || !it.date.isBefore(cutoff) }
        _uiState.update { it.copy(groupedNotifications = visible.groupBy { n -> labelForDate(n.date) }) }
    }

    private fun labelForDate(date: LocalDate): String {
        val today = today()
        return when (date) {
            today -> "Hoje"
            today.minusDays(1) -> "Ontem"
            else -> date.format(dayFormatter)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    val application = this[APPLICATION_KEY] as InventraApplication
                    NotificationViewModel(
                        alertRepository = application.container.alertRepository,
                        authRepository = application.container.authRepository,
                    )
                }
            }
    }
}

private const val EXPIRATION_TYPE = "EXPIRATION"
private const val STOCK_TYPE = "STOCK"

private val ptBr: Locale = Locale.forLanguageTag("pt-BR")
private val dayFormatter = DateTimeFormatter.ofPattern("d 'de' MMMM", ptBr)
private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

private fun AlertResponse.createdAtIn(zone: ZoneId): ZonedDateTime =
    LocalDateTime.parse(createdAt).atZone(ZoneOffset.UTC).withZoneSameInstant(zone)

private fun AlertResponse.toNotification(zone: ZoneId): InventraNotification {
    val created = createdAtIn(zone)
    val productName = product?.name
    return InventraNotification(
        id = id,
        variant =
            when (severity) {
                AlertSeverity.CRITICAL, AlertSeverity.HIGH -> InventraStatusIconVariant.Danger
                AlertSeverity.MEDIUM, AlertSeverity.LOW -> InventraStatusIconVariant.Info
            },
        title =
            when {
                productName == null -> message
                type == EXPIRATION_TYPE -> "$productName venceu"
                type == STOCK_TYPE -> "$productName abaixo do mínimo"
                else -> message
            },
        subtitle =
            when {
                productName == null -> ""
                type == EXPIRATION_TYPE -> batch?.let { "Lote ${it.batchNumber}" } ?: message
                type == STOCK_TYPE -> "Reponha o estoque"
                else -> productName
            },
        timestamp = created.format(timeFormatter),
        date = created.toLocalDate(),
        isUnread = !read,
    )
}
