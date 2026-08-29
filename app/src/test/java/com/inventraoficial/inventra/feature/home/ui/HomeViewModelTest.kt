package com.inventraoficial.inventra.feature.home.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeViewModelTest {
    @Test
    fun `estado inicial carrega os dados da Home`() {
        val viewModel = HomeViewModel()

        val uiState = viewModel.uiState.value

        assertEquals("Jones", uiState.userName)
        assertTrue(uiState.dateText.isNotBlank())
        assertEquals(3, uiState.priorityItems.size)
        assertEquals(4, uiState.stats.size)
    }
}
