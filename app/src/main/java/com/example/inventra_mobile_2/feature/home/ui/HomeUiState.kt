package com.example.inventra_mobile_2.feature.home.ui

import com.example.inventra_mobile_2.core.designsystem.organisms.InventraPriorityItem
import com.example.inventra_mobile_2.core.designsystem.organisms.InventraStat

data class HomeUiState(
    val userName: String = "",
    val dateText: String = "",
    val priorityItems: List<InventraPriorityItem> = emptyList(),
    val stats: List<InventraStat> = emptyList()
) {

}