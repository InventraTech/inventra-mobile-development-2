package com.inventraoficial.inventra.feature.stock.list.ui

import com.inventraoficial.inventra.data.local.FakeStockCache
import com.inventraoficial.inventra.data.local.SessionUser
import com.inventraoficial.inventra.data.remote.dto.auth.AccessType
import com.inventraoficial.inventra.data.remote.dto.stock.StockUnit
import com.inventraoficial.inventra.data.repository.FakeAuthRepository
import com.inventraoficial.inventra.data.repository.FakeStockRepository
import com.inventraoficial.inventra.data.repository.FakeStockRepository.Companion.stockItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
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
    private lateinit var stockCache: FakeStockCache

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        stockRepository = FakeStockRepository()
        stockCache = FakeStockCache()
        stockRepository.kitchenStock =
            listOf(
                stockItem(1, "Arroz Camil", "Grãos", totalQuantity = 24.0, activeBatches = 2)
                    .copy(nextExpirationDate = "2026-10-15"),
                stockItem(2, "Leite Integral", "Laticínios", totalQuantity = 1.5, activeBatches = 1)
                    .copy(unit = StockUnit(id = 2, symbol = "L"), nextExpirationDate = "2026-10-30"),
                stockItem(3, "Carne Bovina", "Carnes", totalQuantity = 0.0, activeBatches = 0).copy(lowStock = true),
                stockItem(4, "Sal Refinado", null, totalQuantity = 1250.0, activeBatches = 3)
                    .copy(nextExpirationDate = "2027-03-01"),
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
                stockCache = stockCache,
                today = { LocalDate.of(2026, 10, 12) },
                clock = { SAVED_AT },
            )
        viewModel.onScreenVisible()
        return viewModel
    }

    private fun TestScope.loadedViewModel(): StockListViewModel {
        val viewModel = createVisibleViewModel()
        advanceUntilIdle()
        return viewModel
    }

    private fun StockListViewModel.titles() = uiState.value.products.map { it.title }

    private fun StockListViewModel.applyFilters(change: StockListViewModel.() -> Unit) {
        onFilterIconClick()
        change()
        onApplyFilters()
    }

    @Test
    fun `mostra a foto do produto quando a API manda`() =
        runTest(testDispatcher) {
            val foto = "https://images.openfoodfacts.org/images/products/789/600/671/6112/front_pt.jpg"
            stockRepository.kitchenStock =
                listOf(
                    stockItem(1, "Arroz Camil", "Grãos", totalQuantity = 24.0, activeBatches = 2).let {
                        it.copy(product = it.product.copy(photoUrl = foto))
                    },
                    stockItem(2, "Sal Refinado", null, totalQuantity = 1.0, activeBatches = 1),
                )

            val viewModel = loadedViewModel()

            val products = viewModel.uiState.value.products
            assertEquals(foto, products.first { it.title == "Arroz Camil" }.photoUrl)
            assertNull(products.first { it.title == "Sal Refinado" }.photoUrl)
        }

    @Test
    fun `carrega o estoque da cozinha do usuario em ordem alfabetica`() =
        runTest(testDispatcher) {
            val viewModel = createVisibleViewModel()
            assertTrue(viewModel.uiState.value.isLoading)

            advanceUntilIdle()

            assertFalse(viewModel.uiState.value.isLoading)
            assertEquals(listOf(7), stockRepository.kitchenStockCalls)
            assertEquals(listOf("Arroz Camil", "Carne Bovina", "Leite Integral", "Sal Refinado"), viewModel.titles())
        }

    @Test
    fun `formata quantidade lotes e estoque baixo`() =
        runTest(testDispatcher) {
            val viewModel = loadedViewModel()

            val products = viewModel.uiState.value.products
            assertEquals(listOf("24 kg", "0 kg", "1,5 L", "1.250 kg"), products.map { it.quantity })
            assertEquals(
                listOf("2 lotes", "Sem lotes · abaixo do mínimo", "1 lote", "3 lotes"),
                products.map { it.batchInfo },
            )
        }

    @Test
    fun `categorias da cozinha alimentam os chips e o filtro`() =
        runTest(testDispatcher) {
            val viewModel = loadedViewModel()

            val categories = listOf("Carnes", "Grãos", "Laticínios", "Sem categoria")
            assertEquals(categories, viewModel.uiState.value.categoryOptions)
            assertEquals(
                listOf("Todos", "Carnes", "Grãos", "Laticínios", "Sem categoria"),
                viewModel.uiState.value.filterOptions,
            )
        }

    @Test
    fun `busca por nome ignora maiusculas`() =
        runTest(testDispatcher) {
            val viewModel = loadedViewModel()

            viewModel.onSearchQueryChange("LEITE ")

            assertEquals(listOf("Leite Integral"), viewModel.titles())
        }

    @Test
    fun `chip de categoria e busca combinam`() =
        runTest(testDispatcher) {
            val viewModel = loadedViewModel()

            viewModel.onFilterSelect("Grãos")
            assertEquals(listOf("Arroz Camil"), viewModel.titles())
            assertEquals("Grãos", viewModel.uiState.value.selectedFilter)

            viewModel.onSearchQueryChange("leite")
            assertTrue(viewModel.titles().isEmpty())

            viewModel.onFilterSelect(ALL_FILTER)
            assertEquals(listOf("Leite Integral"), viewModel.titles())
        }

    @Test
    fun `abrir o filtro copia os filtros atuais para o rascunho`() =
        runTest(testDispatcher) {
            val viewModel = loadedViewModel()
            viewModel.onFilterSelect("Carnes")

            viewModel.onFilterIconClick()

            assertEquals(
                setOf("Carnes"),
                viewModel.uiState.value.draftFilters
                    ?.categories,
            )
        }

    @Test
    fun `mudar o rascunho so vale depois de aplicar`() =
        runTest(testDispatcher) {
            val viewModel = loadedViewModel()

            viewModel.onFilterIconClick()
            viewModel.onCategoryToggle("Grãos")
            assertEquals(4, viewModel.titles().size)

            viewModel.onApplyFilters()

            assertNull(viewModel.uiState.value.draftFilters)
            assertEquals(listOf("Arroz Camil"), viewModel.titles())
        }

    @Test
    fun `fechar o filtro descarta o rascunho`() =
        runTest(testDispatcher) {
            val viewModel = loadedViewModel()

            viewModel.onFilterIconClick()
            viewModel.onStatusToggle(StockStatus.LowStock)
            viewModel.onFiltersDismiss()

            assertNull(viewModel.uiState.value.draftFilters)
            assertEquals(StockFilters(), viewModel.uiState.value.filters)
            assertEquals(4, viewModel.titles().size)
        }

    @Test
    fun `varias categorias mostram qualquer uma delas e nenhum chip fica marcado`() =
        runTest(testDispatcher) {
            val viewModel = loadedViewModel()

            viewModel.applyFilters {
                onCategoryToggle("Grãos")
                onCategoryToggle("Laticínios")
            }

            assertEquals(listOf("Arroz Camil", "Leite Integral"), viewModel.titles())
            assertEquals("", viewModel.uiState.value.selectedFilter)
            assertEquals(2, viewModel.uiState.value.filters.activeCount)
        }

    @Test
    fun `status filtram por validade proxima estoque baixo e sem estoque`() =
        runTest(testDispatcher) {
            val viewModel = loadedViewModel()

            viewModel.applyFilters { onStatusToggle(StockStatus.ExpiringSoon) }
            assertEquals(listOf("Arroz Camil"), viewModel.titles())

            viewModel.applyFilters {
                onStatusToggle(StockStatus.ExpiringSoon)
                onStatusToggle(StockStatus.LowStock)
            }
            assertEquals(listOf("Carne Bovina"), viewModel.titles())

            viewModel.applyFilters { onStatusToggle(StockStatus.OutOfStock) }
            assertEquals(listOf("Carne Bovina"), viewModel.titles())
        }

    @Test
    fun `ordena por validade mais proxima com os sem validade no fim`() =
        runTest(testDispatcher) {
            val viewModel = loadedViewModel()

            viewModel.applyFilters { onSortSelect(StockSort.Expiration) }

            assertEquals(listOf("Arroz Camil", "Leite Integral", "Sal Refinado", "Carne Bovina"), viewModel.titles())
            assertEquals(1, viewModel.uiState.value.filters.activeCount)
        }

    @Test
    fun `ordena pela menor quantidade`() =
        runTest(testDispatcher) {
            val viewModel = loadedViewModel()

            viewModel.applyFilters { onSortSelect(StockSort.Quantity) }

            assertEquals(listOf("Carne Bovina", "Leite Integral", "Arroz Camil", "Sal Refinado"), viewModel.titles())
        }

    @Test
    fun `limpar volta o rascunho para o padrao`() =
        runTest(testDispatcher) {
            val viewModel = loadedViewModel()
            viewModel.applyFilters {
                onSortSelect(StockSort.Quantity)
                onCategoryToggle("Carnes")
            }

            viewModel.applyFilters { onClearFilters() }

            assertEquals(StockFilters(), viewModel.uiState.value.filters)
            assertEquals(4, viewModel.titles().size)
            assertEquals(ALL_FILTER, viewModel.uiState.value.selectedFilter)
        }

    @Test
    fun `recarregar mantem o filtro escolhido sem mostrar o carregando de novo`() =
        runTest(testDispatcher) {
            val viewModel = loadedViewModel()
            viewModel.onFilterSelect("Carnes")

            viewModel.onScreenVisible()
            assertFalse(viewModel.uiState.value.isLoading)
            advanceUntilIdle()

            assertEquals("Carnes", viewModel.uiState.value.selectedFilter)
            assertEquals(listOf("Carne Bovina"), viewModel.titles())
        }

    @Test
    fun `categoria que sumiu da cozinha sai do filtro`() =
        runTest(testDispatcher) {
            val viewModel = loadedViewModel()
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
            val viewModel = loadedViewModel()
            assertEquals("Sem conexão", viewModel.uiState.value.errorMessage)

            stockRepository.kitchenStockError = null
            viewModel.onRetryClick()
            advanceUntilIdle()

            assertNull(viewModel.uiState.value.errorMessage)
            assertEquals(4, viewModel.titles().size)
        }

    @Test
    fun `lista que chegou da api fica salva no cache da cozinha`() =
        runTest(testDispatcher) {
            loadedViewModel()

            val cached = stockCache.saved.getValue(7)
            assertEquals(4, cached.items.size)
            assertEquals(SAVED_AT, cached.savedAt)
        }

    @Test
    fun `sem conexao mostra o estoque salvo com o aviso`() =
        runTest(testDispatcher) {
            loadedViewModel()
            stockRepository.kitchenStockError = IOException("Sem conexão")

            val offline = loadedViewModel()

            val state = offline.uiState.value
            assertNull(state.errorMessage)
            assertEquals(4, offline.titles().size)
            assertTrue(state.offlineMessage.orEmpty().startsWith("Sem conexão. Mostrando o estoque salvo em"))
        }

    @Test
    fun `sem conexao e sem cache mostra o erro`() =
        runTest(testDispatcher) {
            stockRepository.kitchenStockError = IOException("Sem conexão")

            val viewModel = loadedViewModel()

            assertEquals("Sem conexão", viewModel.uiState.value.errorMessage)
            assertNull(viewModel.uiState.value.offlineMessage)
        }

    @Test
    fun `voltando a conexao o aviso de estoque salvo some`() =
        runTest(testDispatcher) {
            loadedViewModel()
            stockRepository.kitchenStockError = IOException("Sem conexão")
            val viewModel = loadedViewModel()

            stockRepository.kitchenStockError = null
            viewModel.onRetryClick()
            advanceUntilIdle()

            assertNull(viewModel.uiState.value.offlineMessage)
        }

    private companion object {
        const val SAVED_AT = 1_793_000_000_000L
    }
}
