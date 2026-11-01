package com.inventraoficial.inventra.feature.stock.detail.ui

import com.inventraoficial.inventra.data.local.SessionUser
import com.inventraoficial.inventra.data.remote.dto.auth.AccessType
import com.inventraoficial.inventra.data.remote.dto.stock.MovementBatch
import com.inventraoficial.inventra.data.remote.dto.stock.MovementUser
import com.inventraoficial.inventra.data.remote.dto.stock.ProductSummary
import com.inventraoficial.inventra.data.remote.dto.stock.StockBatchStatus
import com.inventraoficial.inventra.data.remote.dto.stock.StockMovementResponse
import com.inventraoficial.inventra.data.remote.dto.stock.StockMovementType
import com.inventraoficial.inventra.data.remote.dto.stock.SupplierSummary
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
import java.io.IOException
import java.time.LocalDate
import java.time.ZoneId

@OptIn(ExperimentalCoroutinesApi::class)
class StockDetailViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private val leiteId = 5013
    private val user =
        SessionUser(
            id = "est-1",
            name = "Mauro Rosa",
            email = "mauro@inventra.com",
            accessType = AccessType.ESTOQUISTA,
            kitchenId = 7,
            kitchenName = "Sabor Filial Sul",
        )
    private lateinit var productRepository: FakeProductRepository
    private lateinit var stockRepository: FakeStockRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        productRepository =
            FakeProductRepository(
                products =
                    listOf(
                        FakeProductRepository.product(
                            leiteId,
                            "Leite Integral",
                            brand = "Italac",
                            categoryName = "Laticínios",
                        ),
                    ),
            )
        stockRepository =
            FakeStockRepository(
                batches =
                    listOf(
                        leite(1, "2026-10-13", 6.0, entry = "2026-10-01", supplier = "Seara Alimentos"),
                        leite(2, "2026-10-20", 4.5, entry = "2026-10-05", supplier = "Italac Distribuidora"),
                        leite(3, "2026-11-30", 6.0, entry = "2026-09-20"),
                        leite(4, "2026-12-31", 2.0, entry = "2026-09-25"),
                        leite(5, "2026-10-10", 3.0, entry = "2026-09-01"),
                        leite(6, "2026-12-31", 0.0, entry = "2026-09-02").copy(status = StockBatchStatus.WRITTEN_OFF),
                        FakeStockRepository.batch(7, "Arroz Camil", "2026-10-14"),
                    ),
            )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun leite(
        id: Int,
        expiration: String,
        quantity: Double,
        entry: String,
        supplier: String? = null,
    ) = FakeStockRepository
        .batch(id, "Leite Integral", expiration, currentQuantity = quantity)
        .copy(
            product = ProductSummary(leiteId, "Leite Integral"),
            batchNumber = "QA-0$id",
            entryDate = entry,
            supplier = supplier?.let { SupplierSummary(id, it) },
        )

    private fun movement(
        id: Int,
        type: StockMovementType,
        quantity: Double,
        reason: String? = null,
        userName: String? = "Mauro Rosa",
    ) = StockMovementResponse(
        id = id,
        type = type,
        reason = reason,
        quantity = quantity,
        balanceAfter = 0.0,
        batch = MovementBatch(1, "QA-01"),
        product = ProductSummary(leiteId, "Leite Integral"),
        user = userName?.let { MovementUser("u-$id", it) },
        createdAt = "2026-10-12T17:30:00",
    )

    private fun createVisibleViewModel(sessionUser: SessionUser? = user): StockDetailViewModel {
        val viewModel =
            StockDetailViewModel(
                productId = leiteId,
                productRepository = productRepository,
                stockRepository = stockRepository,
                authRepository = FakeAuthRepository(initialUser = sessionUser),
                zone = ZoneId.of("America/Sao_Paulo"),
                today = { LocalDate.of(2026, 10, 12) },
            )
        viewModel.onScreenVisible()
        return viewModel
    }

    @Test
    fun `carrega o produto e os lotes dele na cozinha`() =
        runTest(testDispatcher) {
            val viewModel = createVisibleViewModel()
            assertTrue(viewModel.uiState.value.isLoading)
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertFalse(state.isLoading)
            assertEquals("Leite Integral", state.productName)
            assertEquals("Italac · Laticínios", state.productDetails)
            assertEquals(listOf(leiteId), productRepository.calls)
            assertEquals(listOf<Int?>(leiteId), stockRepository.batchesProductIds)
        }

    @Test
    fun `so o supervisor ve o botao de editar`() =
        runTest(testDispatcher) {
            val estoquista = createVisibleViewModel()
            val supervisor = createVisibleViewModel(user.copy(accessType = AccessType.SUPERVISOR))
            advanceUntilIdle()

            assertFalse(estoquista.uiState.value.canEdit)
            assertTrue(supervisor.uiState.value.canEdit)
        }

    @Test
    fun `soma so os lotes ativos e dentro da validade`() =
        runTest(testDispatcher) {
            val viewModel = createVisibleViewModel()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertEquals("18,5 L", state.totalValue)
            assertEquals("Em 4 lotes dentro da validade.", state.totalCaption)
        }

    @Test
    fun `mostra os tres lotes que vencem primeiro e expande para todos`() =
        runTest(testDispatcher) {
            val viewModel = createVisibleViewModel()
            advanceUntilIdle()

            val collapsed = viewModel.uiState.value
            assertEquals(listOf("QA-01", "QA-02", "QA-03"), collapsed.batches.map { it.code })
            assertEquals("AMANHÃ", collapsed.batches.first().dateLabel)
            assertTrue(collapsed.batches.first().isUrgent)
            assertEquals("20/10/2026", collapsed.batches[1].dateLabel)
            assertFalse(collapsed.batches[1].isUrgent)
            assertEquals("4,5 L", collapsed.batches[1].quantity)
            assertEquals("Ver todos os 4 lotes", collapsed.seeAllBatchesText)

            viewModel.onSeeAllBatchesClick()
            assertEquals(4, viewModel.uiState.value.batches.size)
            assertEquals("Mostrar menos", viewModel.uiState.value.seeAllBatchesText)

            viewModel.onSeeAllBatchesClick()
            assertEquals(3, viewModel.uiState.value.batches.size)
        }

    @Test
    fun `fornecedor e o da entrega mais recente`() =
        runTest(testDispatcher) {
            val viewModel = createVisibleViewModel()
            advanceUntilIdle()

            assertEquals("Italac Distribuidora", viewModel.uiState.value.supplier)
        }

    @Test
    fun `produto sem lotes mostra zero e esconde o ver todos`() =
        runTest(testDispatcher) {
            stockRepository.batches = emptyList()
            val viewModel = createVisibleViewModel()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertEquals("0 L", state.totalValue)
            assertEquals("Nenhum lote disponível na cozinha.", state.totalCaption)
            assertTrue(state.batches.isEmpty())
            assertNull(state.seeAllBatchesText)
            assertEquals("Sem fornecedor informado", state.supplier)
        }

    @Test
    fun `saidas mostram quem tirou o motivo e a quantidade no fuso do celular`() =
        runTest(testDispatcher) {
            stockRepository.movements =
                mapOf(
                    StockMovementType.WRITE_OFF to
                        listOf(
                            movement(1, StockMovementType.WRITE_OFF, 6.0, reason = "USAGE"),
                            movement(2, StockMovementType.WRITE_OFF, 1.5, reason = "REQUISITION", userName = null),
                        ),
                )
            val viewModel = createVisibleViewModel()
            advanceUntilIdle()

            viewModel.onExitsClick()
            assertTrue(
                viewModel.uiState.value.history
                    ?.isLoading == true,
            )
            advanceUntilIdle()

            val history = viewModel.uiState.value.history!!
            assertEquals(HistoryType.Exits, history.type)
            assertEquals(listOf(StockMovementType.WRITE_OFF), stockRepository.movementsCalls)
            assertEquals(
                listOf(
                    HistoryEntry("12/10/2026 14:30", "Mauro Rosa · Uso", "−6 L · lote QA-01"),
                    HistoryEntry("12/10/2026 14:30", "Sistema · Requisição", "−1,5 L · lote QA-01"),
                ),
                history.entries,
            )
        }

    @Test
    fun `entradas aparecem com sinal de mais e tocar de novo fecha`() =
        runTest(testDispatcher) {
            stockRepository.movements =
                mapOf(StockMovementType.ENTRY to listOf(movement(3, StockMovementType.ENTRY, 12.0)))
            val viewModel = createVisibleViewModel()
            advanceUntilIdle()

            viewModel.onEntriesClick()
            advanceUntilIdle()
            assertEquals(
                listOf(HistoryEntry("12/10/2026 14:30", "Mauro Rosa", "+12 L · lote QA-01")),
                viewModel.uiState.value.history
                    ?.entries,
            )

            viewModel.onEntriesClick()
            assertNull(viewModel.uiState.value.history)
        }

    @Test
    fun `trocar de entradas para saidas mostra so o ultimo pedido`() =
        runTest(testDispatcher) {
            stockRepository.movements =
                mapOf(
                    StockMovementType.ENTRY to listOf(movement(3, StockMovementType.ENTRY, 12.0)),
                    StockMovementType.WRITE_OFF to emptyList(),
                )
            val viewModel = createVisibleViewModel()
            advanceUntilIdle()

            viewModel.onEntriesClick()
            viewModel.onExitsClick()
            advanceUntilIdle()

            val history = viewModel.uiState.value.history!!
            assertEquals(HistoryType.Exits, history.type)
            assertTrue(history.entries.isEmpty())
        }

    @Test
    fun `falha nas movimentacoes mostra o erro so no historico`() =
        runTest(testDispatcher) {
            stockRepository.movementsError = IOException("Sem conexão")
            val viewModel = createVisibleViewModel()
            advanceUntilIdle()

            viewModel.onExitsClick()
            advanceUntilIdle()

            assertEquals(
                "Sem conexão",
                viewModel.uiState.value.history
                    ?.errorMessage,
            )
            assertNull(viewModel.uiState.value.errorMessage)
        }

    @Test
    fun `falha ao carregar mostra o erro e tentar novamente recarrega`() =
        runTest(testDispatcher) {
            productRepository.error = IOException("Sem conexão")
            val viewModel = createVisibleViewModel()
            advanceUntilIdle()
            assertEquals("Sem conexão", viewModel.uiState.value.errorMessage)

            productRepository.error = null
            viewModel.onRetryClick()
            advanceUntilIdle()

            assertNull(viewModel.uiState.value.errorMessage)
            assertEquals("Leite Integral", viewModel.uiState.value.productName)
        }

    @Test
    fun `usuario sem cozinha ve o produto sem lotes`() =
        runTest(testDispatcher) {
            val viewModel = createVisibleViewModel(user.copy(kitchenId = null, kitchenName = null))
            advanceUntilIdle()

            assertEquals("Leite Integral", viewModel.uiState.value.productName)
            assertTrue(stockRepository.batchesCalls.isEmpty())
            assertEquals("0 L", viewModel.uiState.value.totalValue)
        }
}
