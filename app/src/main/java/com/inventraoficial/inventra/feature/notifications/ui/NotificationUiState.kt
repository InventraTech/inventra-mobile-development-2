package com.inventraoficial.inventra.feature.notifications.ui

import com.inventraoficial.inventra.core.designsystem.organisms.InventraBottomDestination
import com.inventraoficial.inventra.core.designsystem.organisms.InventraNotification

data class NotificationUiState(
    val filterOptions: List<String> = listOf("Todos", "Alertas", "Sistema"),
    val selectedFilter: String = "Todos",
    val groupedNotifications: Map<String, List<InventraNotification>> = emptyMap(),
    val isHistoryExpanded: Boolean = false,
    val selectedDestination: InventraBottomDestination = InventraBottomDestination.Notifications,
)
