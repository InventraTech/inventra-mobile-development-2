package com.inventraoficial.inventra.feature.stock.list.ui

import com.inventraoficial.inventra.data.local.SessionUser
import com.inventraoficial.inventra.data.remote.dto.auth.AccessType
import com.inventraoficial.inventra.data.remote.dto.stock.StockUnit
import com.inventraoficial.inventra.data.repository.FakeAuthRepository
import com.inventraoficial.inventra.data.repository.FakeStockRepository
import com.inventraoficial.inventra.data.repository.FakeStockRepository.Companion.stockItem
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

@OptIn(ExperimentalCoroutinesApi::class)
class StockListViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private val user =
        SessionUser(
            id = "est-1",
            name = "Mauro Rosa",
            email = "mauro@inventra.com",
            accessType = AccessType.ESTOQUISTA,
            kitchenId = 7,
            kitchenName = "Sabor Filial Sul",
        )
    private lateinit var stockRepository: FakeStockRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        stockRepository = FakeStockRepository()
        stockRepository.kitchenStock =
            listOf(
                stockItem(1, "Arroz Camil", "Grãos", totalQuantity = 24.0, activeBatches = 2),
                stockItem(2, "Leite Integral", "Laticínios", totalQuantity = 1.5, activeBatches = 1)
                    .copy(unit = StockUnit(id = 2, symbol = "L")),
                stockItem(3, "Carne Bovina", "Carnes", totalQuantity = 0.0, activeBatches = 0).copy(lowStock = true),
                stockItem(4, "Sal Refinado", null, totalQuantity = 1250.0, activeBatches = 3),
            )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createVisibleViewModel(sessionUser: SessionUser? = user): StockListViewModel {
        val viewModel =
            StockListViewModel(
                authRepository = FakeAuthRepository(initialUser = sessionUser),
                stockRepository = stockRepository,
            )
        viewModel.onScreenVisible()
        return viewModel
    }

    private fun StockListViewModel.titles() = uiState.value.products.map { it.title }

    @Test
    fun `carrega o estoque da cozinha do usuario`() =
        runTest(testDispatcher) {
            val viewModel = createVisibleViewModel()
            assertTrue(viewModel.uiState.value.isLoading)

            advanceUntilIdle()

            assertFalse(viewModel.uiState.value.isLoading)
            assertEquals(listOf(7), stockRepository.kitchenStockCalls)
            assertEquals(listOf("Arroz Camil", "Leite Integral", "Carne Bovina", "Sal Refinado"), viewModel.titles())
        }

    @Test
    fun `formata quantidade lotes e estoque baixo`() =
        runTest(testDispatcher) {
            val viewModel = createVisibleViewModel()
            advanceUntilIdle()

            val products = viewModel.uiState.value.products
            assertEquals(listOf("24 kg", "1,5 L", "0 kg", "1.250 kg"), products.map { it.quantity })
            assertEquals(
                listOf("2 lotes", "1 lote", "Sem lotes · abaixo do mínimo", "3 lotes"),
                products.map { it.batchInfo },
            )
        }

    @Test
    fun `filtros vem das categorias da cozinha`() =
        runTest(testDispatcher) {
            val viewModel = createVisibleViewModel()
            advanceUntilIdle()

            assertEquals(
                listOf("Todos", "Carnes", "Grãos", "Laticínios", "Sem categoria"),
                viewModel.uiState.value.filterOptions,
            )
        }

    @Test
    fun `busca por nome ignora maiusculas`() =
        runTest(testDispatcher) {
            val viewModel = createVisibleViewModel()
            advanceUntilIdle()

            viewModel.onSearchQueryChange("LEITE ")

            assertEquals(listOf("Leite Integral"), viewModel.titles())
        }

    @Test
    fun `filtro de categoria e busca combinam`() =
        runTest(testDispatcher) {
            val viewModel = createVisibleViewModel()
            advanceUntilIdle()

            viewModel.onFilterSelect("Grãos")
            assertEquals(listOf("Arroz Camil"), viewModel.titles())

            viewModel.onSearchQueryChange("leite")
            assertTrue(viewModel.titles().isEmpty())

            viewModel.onFilterSelect(ALL_FILTER)
            assertEquals(listOf("Leite Integral"), viewModel.titles())
        }

    @Test
    fun `recarregar mantem o filtro escolhido sem mostrar o carregando de novo`() =
        runTest(testDispatcher) {
            val viewModel = createVisibleViewModel()
            advanceUntilIdle()
            viewModel.onFilterSelect("Carnes")

            viewModel.onScreenVisible()
            assertFalse(viewModel.uiState.value.isLoading)
            advanceUntilIdle()

            assertEquals("Carnes", viewModel.uiState.value.selectedFilter)
            assertEquals(listOf("Carne Bovina"), viewModel.titles())
        }

    @Test
    fun `filtro que sumiu da cozinha volta para Todos`() =
        runTest(testDispatcher) {
            val viewModel = createVisibleViewModel()
            advanceUntilIdle()
            viewModel.onFilterSelect("Carnes")

            stockRepository.kitchenStock = stockRepository.kitchenStock.filter { it.category?.name != "Carnes" }
            viewModel.onScreenVisible()
            advanceUntilIdle()

            assertEquals(ALL_FILTER, viewModel.uiState.value.selectedFilter)
            assertEquals(3, viewModel.titles().size)
        }

    @Test
    fun `usuario sem cozinha ve a lista vazia sem chamar a api`() =
        runTest(testDispatcher) {
            val viewModel = createVisibleViewModel(user.copy(kitchenId = null, kitchenName = null))
            advanceUntilIdle()

            assertTrue(viewModel.titles().isEmpty())
            assertTrue(stockRepository.kitchenStockCalls.isEmpty())
            assertFalse(viewModel.uiState.value.isLoading)
        }

    @Test
    fun `falha na api mostra o erro e tentar novamente recarrega`() =
        runTest(testDispatcher) {
            stockRepository.kitchenStockError = IOException("Sem conexão")
            val viewModel = createVisibleViewModel()
            advanceUntilIdle()
            assertEquals("Sem conexão", viewModel.uiState.value.errorMessage)

            stockRepository.kitchenStockError = null
            viewModel.onRetryClick()
            advanceUntilIdle()

            assertNull(viewModel.uiState.value.errorMessage)
            assertEquals(4, viewModel.titles().size)
        }
}
