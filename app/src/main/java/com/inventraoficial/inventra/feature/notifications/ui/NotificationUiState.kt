package com.inventraoficial.inventra.feature.notifications.ui

import com.inventraoficial.inventra.core.designsystem.organisms.InventraNotification

data class NotificationUiState(
    val groupedNotifications: Map<String, List<InventraNotification>> = emptyMap(),
    val isHistoryExpanded: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)
