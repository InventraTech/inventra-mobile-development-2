package com.inventraoficial.inventra.feature.home.ui

import com.inventraoficial.inventra.core.designsystem.organisms.InventraPriorityItem
import com.inventraoficial.inventra.core.designsystem.organisms.InventraStat

data class HomeUiState(
    val userName: String = "",
    val dateText: String = "",
    val priorityItems: List<InventraPriorityItem> = emptyList(),
    val stats: List<InventraStat> = emptyList(),
)