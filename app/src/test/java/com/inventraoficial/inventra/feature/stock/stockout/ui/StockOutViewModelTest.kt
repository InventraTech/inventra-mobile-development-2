package com.inventraoficial.inventra.feature.stock.stockout.ui

import com.inventraoficial.inventra.data.local.SessionUser
import com.inventraoficial.inventra.data.remote.dto.auth.AccessType
import com.inventraoficial.inventra.data.remote.dto.stock.BatchConsumptionResponse
import com.inventraoficial.inventra.data.remote.dto.stock.ConsumeProductRequest
import com.inventraoficial.inventra.data.remote.dto.stock.ConsumedBatch
import com.inventraoficial.inventra.data.remote.dto.stock.StockBatchStatus
import com.inventraoficial.inventra.data.remote.dto.stock.StockMovementReason
import com.inventraoficial.inventra.data.repository.FakeAuthRepository
import com.inventraoficial.inventra.data.repository.FakeProductRepository
import com.inventraoficial.inventra.data.repository.FakeStockRepository
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
class StockOutViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var productRepository: FakeProductRepository
    private lateinit var stockRepository: FakeStockRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        val arroz = FakeProductRepository.product(1, "Arroz Camil", "kg", brand = "Camil", categoryName = "Grãos")
        productRepository = FakeProductRepository(listOf(arroz))
        stockRepository = FakeStockRepository()
        stockRepository.kitchenStock =
            listOf(
                FakeStockRepository
                    .stockItem(1, "Arroz Camil", "Grãos", totalQuantity = 24.5, activeBatches = 2)
                    .copy(nextExpirationDate = "2026-11-03"),
            )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(kitchenId: Int? = 7): StockOutViewModel {
        val user = SessionUser("1", "Mauro", "mauro@inventra.com", AccessType.ESTOQUISTA, kitchenId)
        return StockOutViewModel(1, productRepository, stockRepository, FakeAuthRepository(initialUser = user))
    }

    @Test
    fun `carrega o produto e o saldo da cozinha`() =
        runTest(testDispatcher) {
            val viewModel = createViewModel()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertFalse(state.isLoading)
            assertEquals("Arroz Camil", state.productName)
            assertEquals("Camil · Grãos", state.details)
            assertEquals("kg", state.unitSymbol)
            assertEquals("24,5", state.available)
            assertEquals("03/11/2026", state.nextExpiration)
            assertTrue(state.hasStock)
            assertEquals(listOf(7), stockRepository.kitchenStockCalls)
        }

    @Test
    fun `produto fora do resumo da cozinha fica sem saldo`() =
        runTest(testDispatcher) {
            stockRepository.kitchenStock = emptyList()
            val viewModel = createViewModel()
            advanceUntilIdle()

            assertEquals("0", viewModel.uiState.value.available)
            assertFalse(viewModel.uiState.value.hasStock)
        }

    @Test
    fun `sem cozinha mostra o aviso`() =
        runTest(testDispatcher) {
            val viewModel = createViewModel(kitchenId = null)
            advanceUntilIdle()

            assertEquals("Você ainda não faz parte de uma cozinha.", viewModel.uiState.value.errorMessage)
        }

    @Test
    fun `falha ao carregar mostra o erro e tentar de novo busca outra vez`() =
        runTest(testDispatcher) {
            stockRepository.kitchenStockError = Exception("Sem conexão")
            val viewModel = createViewModel()
            advanceUntilIdle()
            assertEquals("Sem conexão", viewModel.uiState.value.errorMessage)

            stockRepository.kitchenStockError = null
            viewModel.onRetryClick()
            advanceUntilIdle()

            assertNull(viewModel.uiState.value.errorMessage)
            assertEquals("24,5", viewModel.uiState.value.available)
        }

    @Test
    fun `quantidade aceita so numeros, virgula e ponto`() {
        val viewModel = createViewModel()

        viewModel.onQuantityChange("2,5")
        viewModel.onQuantityChange("2,5a")

        assertEquals("2,5", viewModel.uiState.value.quantity)
    }

    @Test
    fun `sem quantidade nao chama a API`() =
        runTest(testDispatcher) {
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.onConfirmClick()

            assertEquals("Informe a quantidade da baixa", viewModel.uiState.value.submitError)
            assertTrue(stockRepository.consumeCalls.isEmpty())
        }

    @Test
    fun `quantidade maior que o saldo nao chama a API`() =
        runTest(testDispatcher) {
            val viewModel = createViewModel()
            advanceUntilIdle()
            viewModel.onQuantityChange("30")

            viewModel.onConfirmClick()

            assertEquals("A quantidade passa do saldo da cozinha (24,5 kg)", viewModel.uiState.value.submitError)
            assertTrue(stockRepository.consumeCalls.isEmpty())
        }

    @Test
    fun `confirmar envia a quantidade com virgula e o motivo e mostra os lotes`() =
        runTest(testDispatcher) {
            stockRepository.consumeResult =
                Result.success(
                    listOf(
                        BatchConsumptionResponse(ConsumedBatch(1, "L2026-001"), 2.0, 0.0, StockBatchStatus.WRITTEN_OFF),
                        BatchConsumptionResponse(ConsumedBatch(2, "L2026-002"), 0.5, 10.0, StockBatchStatus.ACTIVE),
                    ),
                )
            val viewModel = createViewModel()
            advanceUntilIdle()
            viewModel.onQuantityChange("2,5")
            viewModel.onReasonSelect(StockOutReason.Loss)

            viewModel.onConfirmClick()
            assertTrue(viewModel.uiState.value.isSubmitting)
            advanceUntilIdle()

            val expectedRequest = ConsumeProductRequest(7, 1, 2.5, StockMovementReason.LOSS)
            assertEquals(listOf(expectedRequest), stockRepository.consumeCalls)
            assertEquals(
                listOf(
                    ConsumedBatchLine("L2026-001", "2 kg", "0 kg"),
                    ConsumedBatchLine("L2026-002", "0,5 kg", "10 kg"),
                ),
                viewModel.uiState.value.consumedBatches,
            )
            assertFalse(viewModel.uiState.value.isSubmitting)
        }

    @Test
    fun `falha na baixa mostra a mensagem do servidor`() =
        runTest(testDispatcher) {
            stockRepository.consumeResult = Result.failure(Exception("Saldo insuficiente na cozinha."))
            val viewModel = createViewModel()
            advanceUntilIdle()
            viewModel.onQuantityChange("1")

            viewModel.onConfirmClick()
            advanceUntilIdle()

            assertEquals("Saldo insuficiente na cozinha.", viewModel.uiState.value.submitError)
            assertNull(viewModel.uiState.value.consumedBatches)
        }
}
