package com.inventraoficial.inventra.feature.home.ui

import com.inventraoficial.inventra.core.designsystem.molecules.InventraStatTone
import com.inventraoficial.inventra.core.designsystem.molecules.InventraUrgency
import com.inventraoficial.inventra.data.local.SessionUser
import com.inventraoficial.inventra.data.remote.dto.auth.AccessType
import com.inventraoficial.inventra.data.repository.FakeAlertRepository
import com.inventraoficial.inventra.data.repository.FakeAlertRepository.Companion.alert
import com.inventraoficial.inventra.data.repository.FakeAuthRepository
import com.inventraoficial.inventra.data.repository.FakeStockRepository
import com.inventraoficial.inventra.data.repository.FakeStockRepository.Companion.batch
import com.inventraoficial.inventra.data.repository.FakeStockRepository.Companion.lowStockAlert
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

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private val today = LocalDate.of(2026, 10, 10)
    private val user =
        SessionUser(
            id = "sup-1",
            name = "Jones Silva",
            email = "jones@inventra.com",
            accessType = AccessType.SUPERVISOR,
            kitchenId = 7,
            kitchenName = "Sabor Filial Sul",
        )
    private lateinit var stockRepository: FakeStockRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        stockRepository =
            FakeStockRepository(
                expiring =
                    listOf(
                        batch(1, "Arroz Camil", "2026-10-15", currentQuantity = 2.0, unitPrice = 10.0),
                        batch(2, "Leite Integral", "2026-10-11", currentQuantity = 3.0, unitPrice = 5.5),
                        batch(3, "Feijão Preto", "2026-10-10"),
                    ),
                lowStock = listOf(lowStockAlert("Óleo de Soja")),
            )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private val alertRepository =
        FakeAlertRepository(
            alerts =
                listOf(
                    alert(1, "EXPIRATION", "2026-10-10T08:00:00", "Leite Integral"),
                    alert(2, "STOCK", "2026-10-10T09:00:00", "Óleo de Soja"),
                    alert(3, "STOCK", "2026-10-09T09:00:00", "Arroz", read = true),
                ),
        )

    private fun createViewModel(sessionUser: SessionUser? = user) =
        HomeViewModel(
            authRepository = FakeAuthRepository(initialUser = sessionUser),
            stockRepository = stockRepository,
            alertRepository = alertRepository,
            today = { today },
        )

    @Test
    fun `mostra o primeiro nome e a data de hoje`() =
        runTest(testDispatcher) {
            val viewModel = createViewModel()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertEquals("Jones", state.userName)
            assertEquals("Sábado, 10 de outubro de 2026", state.dateText)
        }

    @Test
    fun `busca os lotes que vencem nos proximos dias da cozinha do usuario`() =
        runTest(testDispatcher) {
            createViewModel()
            advanceUntilIdle()

            assertEquals(listOf(7 to HomeViewModel.EXPIRING_DAYS), stockRepository.expiringCalls)
        }

    @Test
    fun `card de prioridade mostra a foto do produto quando a API manda`() =
        runTest(testDispatcher) {
            val foto = "https://images.openfoodfacts.org/images/products/789/808/064/0017/front_pt.jpg"
            stockRepository =
                FakeStockRepository(
                    expiring =
                        listOf(
                            batch(1, "Leite Integral", "2026-10-11").let {
                                it.copy(product = it.product.copy(photoUrl = foto))
                            },
                            batch(2, "Feijão Preto", "2026-10-12"),
                        ),
                )
            val viewModel = createViewModel()
            advanceUntilIdle()

            val items = viewModel.uiState.value.priorityItems
            assertEquals(foto, items.first { it.title == "Leite Integral" }.photoUrl)
            assertNull(items.first { it.title == "Feijão Preto" }.photoUrl)
        }

    @Test
    fun `ordena as prioridades pela data de vencimento`() =
        runTest(testDispatcher) {
            val viewModel = createViewModel()
            advanceUntilIdle()

            val items = viewModel.uiState.value.priorityItems
            assertEquals(listOf("Feijão Preto", "Leite Integral", "Arroz Camil"), items.map { it.title })
            assertEquals(listOf("Vence hoje", "Vence amanhã", "Vence em 5 dias"), items.map { it.statusText })
            assertEquals(
                listOf(InventraUrgency.Tomorrow, InventraUrgency.Tomorrow, InventraUrgency.Normal),
                items.map { it.urgency },
            )
            assertEquals("Lote 0003", items.first().batch)
        }

    @Test
    fun `monta o resumo do estoque com os dados da api`() =
        runTest(testDispatcher) {
            val viewModel = createViewModel()
            advanceUntilIdle()

            val stats = viewModel.uiState.value.stats
            assertEquals(listOf("3 lotes", "2 lotes", "R$ 36,50", "1 produto"), stats.map { it.value })
            assertTrue(stats.all { it.tone == InventraStatTone.Negative })
        }

    @Test
    fun `sem lotes a vencer o resumo fica positivo`() =
        runTest(testDispatcher) {
            stockRepository.expiring = emptyList()
            stockRepository.lowStock = emptyList()
            val viewModel = createViewModel()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertTrue(state.priorityItems.isEmpty())
            assertEquals(listOf("0 lotes", "0 lotes", "R$ 0,00", "0 produtos"), state.stats.map { it.value })
            assertTrue(state.stats.all { it.tone == InventraStatTone.Positive })
        }

    @Test
    fun `usuario sem cozinha nao chama a api`() =
        runTest(testDispatcher) {
            val viewModel = createViewModel(user.copy(kitchenId = null, kitchenName = null))
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertFalse(state.isLoading)
            assertTrue(stockRepository.expiringCalls.isEmpty())
            assertTrue(state.priorityItems.isEmpty())
        }

    @Test
    fun `falha na api mostra a mensagem de erro`() =
        runTest(testDispatcher) {
            stockRepository.expiringError = IOException("Sem conexão")
            val viewModel = createViewModel()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertFalse(state.isLoading)
            assertEquals("Sem conexão", state.errorMessage)
        }

    @Test
    fun `tentar novamente limpa o erro e recarrega`() =
        runTest(testDispatcher) {
            stockRepository.expiringError = IOException("Sem conexão")
            val viewModel = createViewModel()
            advanceUntilIdle()

            stockRepository.expiringError = null
            viewModel.onRetryClick()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertNull(state.errorMessage)
            assertEquals(3, state.priorityItems.size)
        }

    @Test
    fun `ao aparecer conta as notificacoes nao lidas`() =
        runTest(testDispatcher) {
            val viewModel = createViewModel()
            viewModel.onScreenVisible()
            advanceUntilIdle()

            assertEquals(2, viewModel.uiState.value.unreadNotifications)
        }

    @Test
    fun `sem cozinha o contador fica zerado`() =
        runTest(testDispatcher) {
            val viewModel = createViewModel(user.copy(kitchenId = null, kitchenName = null))
            viewModel.onScreenVisible()
            advanceUntilIdle()

            assertEquals(0, viewModel.uiState.value.unreadNotifications)
        }
}
