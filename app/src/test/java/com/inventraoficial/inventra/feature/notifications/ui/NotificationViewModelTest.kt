package com.inventraoficial.inventra.feature.notifications.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NotificationViewModelTest {
    @Test
    fun `estado inicial mostra so Hoje e Ontem`() {
        val viewModel = NotificationViewModel()

        val uiState = viewModel.uiState.value

        assertFalse(uiState.isHistoryExpanded)
        assertEquals(setOf("Hoje", "Ontem"), uiState.groupedNotifications.keys)
        assertEquals(2, uiState.groupedNotifications["Hoje"]?.size)
        assertEquals(1, uiState.groupedNotifications["Ontem"]?.size)
    }

    @Test
    fun `onFilterSelect atualiza o filtro selecionado`() {
        val viewModel = NotificationViewModel()

        viewModel.onFilterSelect("Alertas")

        assertEquals("Alertas", viewModel.uiState.value.selectedFilter)
    }

    @Test
    fun `onSeeAllClick expande o historico`() {
        val viewModel = NotificationViewModel()

        viewModel.onSeeAllClick()
        val uiState = viewModel.uiState.value

        assertTrue(uiState.isHistoryExpanded)
        assertTrue(uiState.groupedNotifications.keys.size > 2)
        assertEquals(5, uiState.groupedNotifications.values.sumOf { it.size })
    }

    @Test
    fun `onSeeAllClick chamado duas vezes volta a recolher`() {
        val viewModel = NotificationViewModel()

        viewModel.onSeeAllClick()
        viewModel.onSeeAllClick()
        val uiState = viewModel.uiState.value

        assertFalse(uiState.isHistoryExpanded)
        assertEquals(setOf("Hoje", "Ontem"), uiState.groupedNotifications.keys)
    }
}
