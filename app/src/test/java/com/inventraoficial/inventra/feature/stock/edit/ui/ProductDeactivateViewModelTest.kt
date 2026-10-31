package com.inventraoficial.inventra.feature.stock.edit.ui

import com.inventraoficial.inventra.data.repository.FakeProductRepository
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProductDeactivateViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var productRepository: FakeProductRepository
    private lateinit var viewModel: ProductDeactivateViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        productRepository = FakeProductRepository()
        viewModel = ProductDeactivateViewModel(42, productRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `tocar em desativar pede confirmacao antes de chamar a api`() {
        viewModel.onDeactivateClick()

        assertTrue(viewModel.uiState.value.isConfirmVisible)
        assertTrue(productRepository.deactivated.isEmpty())
    }

    @Test
    fun `cancelar fecha a confirmacao sem desativar`() {
        viewModel.onDeactivateClick()
        viewModel.onDismiss()

        assertFalse(viewModel.uiState.value.isConfirmVisible)
        assertTrue(productRepository.deactivated.isEmpty())
    }

    @Test
    fun `confirmar desativa o produto`() =
        runTest(testDispatcher) {
            viewModel.onDeactivateClick()
            viewModel.onConfirm()
            advanceUntilIdle()

            assertEquals(listOf(42), productRepository.deactivated)
            assertTrue(viewModel.uiState.value.isDeactivated)
            assertFalse(viewModel.uiState.value.isConfirmVisible)
        }

    @Test
    fun `falha ao desativar mostra o erro e continua na tela`() =
        runTest(testDispatcher) {
            productRepository.deactivateResult = Result.failure(Exception("Sem permissão"))
            viewModel.onConfirm()
            advanceUntilIdle()

            assertEquals("Sem permissão", viewModel.uiState.value.errorMessage)
            assertFalse(viewModel.uiState.value.isDeactivated)
            assertFalse(viewModel.uiState.value.isDeactivating)
        }
}
