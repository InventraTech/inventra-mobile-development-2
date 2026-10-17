package com.inventraoficial.inventra.feature.notifications.ui

import com.inventraoficial.inventra.core.designsystem.organisms.InventraNotification

data class NotificationUiState(
    val selectedFilter: NotificationFilter = NotificationFilter.All,
    val groupedNotifications: Map<String, List<InventraNotification>> = emptyMap(),
    val targets: Map<Int, NotificationTarget> = emptyMap(),
    val hasUnread: Boolean = false,
    val isHistoryExpanded: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

enum class NotificationFilter(
    val label: String,
) {
    All("Todos"),
    Alerts("Alertas"),
    System("Sistema"),
}

sealed interface NotificationTarget {
    data class Product(
        val productId: Int,
    ) : NotificationTarget

    data class Requisition(
        val requisitionId: Int,
    ) : NotificationTarget

    data object Members : NotificationTarget
}
