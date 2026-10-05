package com.inventraoficial.inventra.feature.requirements

import com.inventraoficial.inventra.feature.requirements.history.ui.HistoryItem
import com.inventraoficial.inventra.feature.requirements.list.ui.Requirement
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object RequirementsSharedData {
    private val _processedItems =
        MutableStateFlow<List<HistoryItem>>(emptyList())
    val processedItems: StateFlow<List<HistoryItem>> =
        _processedItems.asStateFlow()

    fun addProcessedItem(requirement: Requirement) {
        val historyItem =
            HistoryItem(
                id = requirement.id,
                imageRes = requirement.imageRes,
                productName = requirement.productName,
                description = requirement.description,
                status = requirement.status,
            )
        val currentItems = _processedItems.value.toMutableList()
        currentItems.add(0, historyItem)
        _processedItems.value = currentItems
    }

    fun getProcessedItems(): List<HistoryItem> = _processedItems.value

    fun clear() {
        _processedItems.value = emptyList()
    }
}
