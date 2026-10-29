package com.inventraoficial.inventra.feature.stock.stockentry.ui

import com.inventraoficial.inventra.data.local.SessionUser
import com.inventraoficial.inventra.data.remote.dto.auth.AccessType
import com.inventraoficial.inventra.data.remote.dto.stock.RegisterStockEntryRequest
import com.inventraoficial.inventra.data.repository.FakeAuthRepository
import com.inventraoficial.inventra.data.repository.FakeProductRepository
import com.inventraoficial.inventra.data.repository.FakeStockRepository
import com.inventraoficial.inventra.data.repository.FakeSupplierRepository
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
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneOffset

@OptIn(ExperimentalCoroutinesApi::class)
class StockEntryViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private val today = LocalDate.of(2026, 10, 17)
    private val clock = Clock.fixed(today.atStartOfDay(ZoneOffset.UTC).toInstant(), ZoneOffset.UTC)
    private lateinit var productRepository: FakeProductRepository
    private lateinit var stockRepository: FakeStockRepository
    private lateinit var supplierRepository: FakeSupplierRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        val leite = FakeProductRepository.product(5013, "Leite Integral", "L", "Italac", "Laticínios")
        productRepository = FakeProductRepository(listOf(leite))
        stockRepository = FakeStockRepository()
        supplierRepository =
            FakeSupplierRepository(
                listOf(
                    FakeSupplierRepository.supplier(1, "Laticínios Bom Leite"),
                    FakeSupplierRepository.supplier(2, "Distribuidora Antiga").copy(active = false),
                ),
            )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(kitchenId: Int? = 7): StockEntryViewModel {
        val user = SessionUser("1", "Mauro", "mauro@inventra.com", AccessType.ESTOQUISTA, kitchenId)
        return StockEntryViewModel(
            productId = 5013,
            productRepository = productRepository,
            stockRepository = stockRepository,
            supplierRepository = supplierRepository,
            authRepository = FakeAuthRepository(initialUser = user),
            clock = clock,
        )
    }

    private fun StockEntryViewModel.fillRequired() {
        onQuantityChange("12,5")
        onBatchNumberChange("L2026-010")
        onExpirationDateSelect(LocalDate.of(2026, 11, 30))
    }

    @Test
    fun `carrega o produto, a data de hoje e so os fornecedores ativos`() =
        runTest(testDispatcher) {
            val viewModel = createViewModel()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertFalse(state.isLoading)
            assertEquals("Leite Integral", state.productName)
            assertEquals("Italac · Laticínios", state.details)
            assertEquals("L", state.unitSymbol)
            assertEquals(today, state.entryDate)
            assertEquals(listOf(SupplierOption(1, "Laticínios Bom Leite")), state.suppliers)
            assertEquals(1, supplierRepository.allCalls)
            assertTrue(supplierRepository.kitchenCalls.isEmpty())
        }

    @Test
    fun `sem cozinha mostra o aviso`() =
        runTest(testDispatcher) {
            val viewModel = createViewModel(kitchenId = null)
            advanceUntilIdle()

            assertEquals("Você ainda não faz parte de uma cozinha.", viewModel.uiState.value.errorMessage)
        }

    @Test
    fun `falha nos fornecedores nao impede a entrada`() =
        runTest(testDispatcher) {
            supplierRepository.error = Exception("Sem conexão")
            val viewModel = createViewModel()
            advanceUntilIdle()

            assertNull(viewModel.uiState.value.errorMessage)
            assertTrue(
                viewModel.uiState.value.suppliers
                    .isEmpty(),
            )
        }

    @Test
    fun `falha no produto mostra o erro e tentar de novo busca outra vez`() =
        runTest(testDispatcher) {
            productRepository.error = Exception("Sem conexão")
            val viewModel = createViewModel()
            advanceUntilIdle()
            assertEquals("Sem conexão", viewModel.uiState.value.errorMessage)

            productRepository.error = null
            viewModel.onRetryClick()
            advanceUntilIdle()

            assertNull(viewModel.uiState.value.errorMessage)
            assertEquals("Leite Integral", viewModel.uiState.value.productName)
        }

    @Test
    fun `campos de numero aceitam so digitos, virgula e ponto e textos param em 50 caracteres`() {
        val viewModel = createViewModel()

        viewModel.onQuantityChange("2,5")
        viewModel.onQuantityChange("2,5a")
        viewModel.onUnitPriceChange("4.50")
        viewModel.onUnitPriceChange("R$ 4")
        viewModel.onBatchNumberChange("L".repeat(60))

        val state = viewModel.uiState.value
        assertEquals("2,5", state.quantity)
        assertEquals("4.50", state.unitPrice)
        assertEquals(50, state.batchNumber.length)
    }

    @Test
    fun `cada campo obrigatorio que falta mostra a mensagem certa sem chamar a API`() =
        runTest(testDispatcher) {
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.onConfirmClick()
            assertEquals("Informe a quantidade recebida", viewModel.uiState.value.submitError)

            viewModel.onQuantityChange("1,2345")
            viewModel.onConfirmClick()
            assertEquals("Use no máximo 3 casas decimais na quantidade", viewModel.uiState.value.submitError)

            viewModel.onQuantityChange("12")
            viewModel.onBatchNumberChange("   ")
            viewModel.onConfirmClick()
            assertEquals("Informe o número do lote", viewModel.uiState.value.submitError)

            viewModel.onBatchNumberChange("L2026-010")
            viewModel.onConfirmClick()
            assertEquals("Informe a validade", viewModel.uiState.value.submitError)

            assertTrue(stockRepository.registerEntryCalls.isEmpty())
        }

    @Test
    fun `datas e preco invalidos nao chamam a API`() =
        runTest(testDispatcher) {
            val viewModel = createViewModel()
            advanceUntilIdle()
            viewModel.fillRequired()

            viewModel.onEntryDateSelect(today.plusDays(1))
            viewModel.onConfirmClick()
            assertEquals("A data de entrada não pode ser no futuro", viewModel.uiState.value.submitError)

            viewModel.onEntryDateSelect(today)
            viewModel.onExpirationDateSelect(today.minusDays(1))
            viewModel.onConfirmClick()
            assertEquals("A validade não pode ser anterior à data de entrada", viewModel.uiState.value.submitError)

            viewModel.onExpirationDateSelect(LocalDate.of(2026, 11, 30))
            viewModel.onUnitPriceChange("4,505")
            viewModel.onConfirmClick()
            assertEquals("Preço unitário inválido", viewModel.uiState.value.submitError)

            assertTrue(stockRepository.registerEntryCalls.isEmpty())
        }

    @Test
    fun `confirmar envia o lote com os opcionais e mostra o lote criado`() =
        runTest(testDispatcher) {
            val viewModel = createViewModel()
            advanceUntilIdle()
            viewModel.fillRequired()
            viewModel.onSupplierSelect(1)
            viewModel.onInvoiceNumberChange(" 12345 ")
            viewModel.onUnitPriceChange("4,50")

            viewModel.onConfirmClick()
            assertTrue(viewModel.uiState.value.isSubmitting)
            advanceUntilIdle()

            val expected =
                RegisterStockEntryRequest(
                    productId = 5013,
                    kitchenId = 7,
                    batchNumber = "L2026-010",
                    initialQuantity = 12.5,
                    entryDate = "2026-10-17",
                    expirationDate = "2026-11-30",
                    supplierId = 1,
                    invoiceNumber = "12345",
                    unitPrice = 4.5,
                )
            assertEquals(listOf(expected), stockRepository.registerEntryCalls)
            assertEquals(RegisteredBatch("L2026-010", "12,5 L", "30/11/2026"), viewModel.uiState.value.registeredBatch)
            assertFalse(viewModel.uiState.value.isSubmitting)
        }

    @Test
    fun `opcionais vazios vao como nulos`() =
        runTest(testDispatcher) {
            val viewModel = createViewModel()
            advanceUntilIdle()
            viewModel.fillRequired()

            viewModel.onConfirmClick()
            advanceUntilIdle()

            val request = stockRepository.registerEntryCalls.single()
            assertNull(request.supplierId)
            assertNull(request.invoiceNumber)
            assertNull(request.unitPrice)
        }

    @Test
    fun `falha na entrada mostra a mensagem do servidor`() =
        runTest(testDispatcher) {
            stockRepository.registerEntryError = Exception("Produto desativado não pode receber entrada de estoque.")
            val viewModel = createViewModel()
            advanceUntilIdle()
            viewModel.fillRequired()

            viewModel.onConfirmClick()
            advanceUntilIdle()

            assertEquals(
                "Produto desativado não pode receber entrada de estoque.",
                viewModel.uiState.value.submitError,
            )
            assertNull(viewModel.uiState.value.registeredBatch)
        }
}
