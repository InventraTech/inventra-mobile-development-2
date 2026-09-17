package com.inventraoficial.inventra.feature.scan.ui

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ScanViewModelTest {
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
    fun `onFlashClick inverte isFlashOn`() {
        val viewModel = ScanViewModel()

        viewModel.onFlashClick()
        assertTrue(viewModel.uiState.value.isFlashOn)

        viewModel.onFlashClick()
        assertFalse(viewModel.uiState.value.isFlashOn)
    }

    @Test
    fun `onPermissionResult concedida atualiza hasCameraPermission e limpa errorMessage`() {
        val viewModel = ScanViewModel()

        viewModel.onPermissionResult(true)

        assertTrue(viewModel.uiState.value.hasCameraPermission)
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `onPermissionResult negada preenche errorMessage`() {
        val viewModel = ScanViewModel()

        viewModel.onPermissionResult(false)

        assertFalse(viewModel.uiState.value.hasCameraPermission)
        assertEquals("Permita o acesso à câmera para escanear", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `onScan liga isProcessing imediatamente, antes do delay terminar`() =
        runTest(testDispatcher) {
            val viewModel = ScanViewModel()

            viewModel.onScan()

            assertTrue(viewModel.uiState.value.isProcessing)
        }

    @Test
    fun `onScan conclui apos o delay e atualiza a instrucao`() =
        runTest(testDispatcher) {
            val viewModel = ScanViewModel()

            viewModel.onScan()
            advanceUntilIdle()

            assertFalse(viewModel.uiState.value.isProcessing)
            assertEquals("Código lido com sucesso", viewModel.uiState.value.instruction)
        }

    @Test
    fun `onScan chamado de novo durante o processamento nao reinicia o estado`() =
        runTest(testDispatcher) {
            val viewModel = ScanViewModel()

            viewModel.onScan()
            viewModel.onScan()
            advanceUntilIdle()

            assertFalse(viewModel.uiState.value.isProcessing)
            assertEquals("Código lido com sucesso", viewModel.uiState.value.instruction)
        }
}
