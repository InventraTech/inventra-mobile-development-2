package com.inventraoficial.inventra.feature.profile.preferences.ui

import com.inventraoficial.inventra.data.local.FakeAppPreferences
import com.inventraoficial.inventra.data.local.ThemeMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AccountPreferencesViewModelTest {
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `mostra o tema salvo`() =
        runTest(testDispatcher) {
            val viewModel = AccountPreferencesViewModel(FakeAppPreferences(initial = ThemeMode.Dark))
            advanceUntilIdle()

            assertEquals(ThemeMode.Dark, viewModel.uiState.value.theme)
        }

    @Test
    fun `sem escolha salva usa o tema do sistema`() =
        runTest(testDispatcher) {
            val viewModel = AccountPreferencesViewModel(FakeAppPreferences())
            advanceUntilIdle()

            assertEquals(ThemeMode.System, viewModel.uiState.value.theme)
        }

    @Test
    fun `escolher um tema salva e atualiza a tela`() =
        runTest(testDispatcher) {
            val preferences = FakeAppPreferences()
            val viewModel = AccountPreferencesViewModel(preferences)
            advanceUntilIdle()

            viewModel.onThemeSelect(ThemeMode.Light)
            advanceUntilIdle()

            assertEquals(listOf(ThemeMode.Light), preferences.savedModes)
            assertEquals(ThemeMode.Light, viewModel.uiState.value.theme)
        }

    @Test
    fun `as opcoes aparecem como claro escuro e sistema`() {
        assertEquals(listOf("Claro", "Escuro", "Sistema"), ThemeMode.entries.map { it.label })
    }
}
