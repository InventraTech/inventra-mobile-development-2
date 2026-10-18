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

    fun onFilterSelect(filter: NotificationFilter) {
        _uiState.update { it.copy(selectedFilter = filter) }
        refreshGroupedNotifications()
    }

    fun onNotificationClick(notification: InventraNotification) {
        if (!notification.isUnread) return
        setRead(setOf(notification.id), read = true)
        viewModelScope.launch {
            alertRepository
                .markAsRead(notification.id)
                .onFailure { setRead(setOf(notification.id), read = false) }
        }
    }

    fun onMarkAllReadClick() {
        val unreadIds = alerts.filterNot { it.read }.map { it.id }.toSet()
        if (unreadIds.isEmpty()) return
        setRead(unreadIds, read = true)
        viewModelScope.launch {
            val kitchenId = authRepository.currentUser.first()?.kitchenId ?: return@launch
            alertRepository
                .markAllAsRead(kitchenId)
                .onFailure { setRead(unreadIds, read = false) }
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
        ids: Set<Int>,
        read: Boolean,
    ) {
        alerts = alerts.map { if (it.id in ids) it.copy(read = read) else it }
        refreshGroupedNotifications()
    }

    private fun refreshGroupedNotifications() {
        val state = _uiState.value
        val cutoff = today().minusDays(1)
        val visible =
            alerts
                .filter { state.selectedFilter.matches(it.type) }
                .map { it.toNotification(zone) }
                .filter { state.isHistoryExpanded || !it.date.isBefore(cutoff) }
        _uiState.update {
            it.copy(
                groupedNotifications = visible.groupBy { n -> labelForDate(n.date) },
                targets = alerts.mapNotNull { alert -> alert.target()?.let { target -> alert.id to target } }.toMap(),
                hasUnread = alerts.any { alert -> !alert.read },
            )
        }
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
private const val EXPIRING_TYPE = "EXPIRING"
private const val STOCK_TYPE = "STOCK"
private const val UNREGISTERED_PRODUCT_TYPE = "UNREGISTERED_PRODUCT"
private const val ACCESS_REQUEST_TYPE = "ACCESS_REQUEST"
private const val REQUISITION_SUBMITTED_TYPE = "REQUISITION_SUBMITTED"
private const val REQUISITION_APPROVED_TYPE = "REQUISITION_APPROVED"
private const val REQUISITION_REJECTED_TYPE = "REQUISITION_REJECTED"
private const val REQUISITION_PREFIX = "REQUISITION_"

private val stockTypes = setOf(EXPIRATION_TYPE, EXPIRING_TYPE, STOCK_TYPE, UNREGISTERED_PRODUCT_TYPE)

private val ptBr: Locale = Locale.forLanguageTag("pt-BR")
private val dayFormatter = DateTimeFormatter.ofPattern("d 'de' MMMM", ptBr)
private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

private fun NotificationFilter.matches(type: String): Boolean =
    when (this) {
        NotificationFilter.All -> true
        NotificationFilter.Alerts -> type in stockTypes
        NotificationFilter.System -> type !in stockTypes
    }

private fun AlertResponse.target(): NotificationTarget? =
    when {
        type == ACCESS_REQUEST_TYPE -> NotificationTarget.Members
        type.startsWith(REQUISITION_PREFIX) -> refKey?.toIntOrNull()?.let { NotificationTarget.Requisition(it) }
        type in stockTypes -> product?.let { NotificationTarget.Product(it.id) }
        else -> null
    }

private fun AlertResponse.createdAtIn(zone: ZoneId): ZonedDateTime =
    LocalDateTime.parse(createdAt).atZone(ZoneOffset.UTC).withZoneSameInstant(zone)

private fun AlertResponse.title(): String {
    val productName = product?.name
    return when (type) {
        EXPIRATION_TYPE -> productName?.let { "$it venceu" }
        EXPIRING_TYPE -> productName?.let { "$it vence em breve" }
        STOCK_TYPE -> productName?.let { "$it abaixo do mínimo" }
        UNREGISTERED_PRODUCT_TYPE -> "Produto não cadastrado"
        ACCESS_REQUEST_TYPE -> "Pedido de entrada na cozinha"
        REQUISITION_SUBMITTED_TYPE -> "Nova requisição para análise"
        REQUISITION_APPROVED_TYPE -> "Requisição aprovada"
        REQUISITION_REJECTED_TYPE -> "Requisição recusada"
        else -> null
    } ?: message
}

private fun AlertResponse.subtitle(): String {
    val usesMessage = title() != message
    return when (type) {
        EXPIRATION_TYPE -> batch?.let { "Lote ${it.batchNumber}" } ?: message
        STOCK_TYPE -> if (product != null) "Reponha o estoque" else ""
        else -> if (usesMessage) message else ""
    }
}

private fun AlertResponse.variant(): InventraStatusIconVariant =
    when {
        type == REQUISITION_APPROVED_TYPE -> InventraStatusIconVariant.Success
        type == ACCESS_REQUEST_TYPE || type == REQUISITION_SUBMITTED_TYPE -> InventraStatusIconVariant.Action
        severity == AlertSeverity.CRITICAL || severity == AlertSeverity.HIGH -> InventraStatusIconVariant.Danger
        else -> InventraStatusIconVariant.Info
    }

private fun AlertResponse.toNotification(zone: ZoneId): InventraNotification {
    val created = createdAtIn(zone)
    return InventraNotification(
        id = id,
        variant = variant(),
        title = title(),
        subtitle = subtitle(),
        timestamp = created.format(timeFormatter),
        date = created.toLocalDate(),
        isUnread = !read,
    )
}
