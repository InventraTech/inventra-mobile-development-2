package com.inventraoficial.inventra.feature.notifications.ui

import com.inventraoficial.inventra.core.designsystem.atoms.InventraStatusIconVariant
import com.inventraoficial.inventra.data.local.SessionUser
import com.inventraoficial.inventra.data.remote.dto.auth.AccessType
import com.inventraoficial.inventra.data.repository.FakeAlertRepository
import com.inventraoficial.inventra.data.repository.FakeAlertRepository.Companion.alert
import com.inventraoficial.inventra.data.repository.FakeAuthRepository
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
class NotificationViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private val saoPaulo = ZoneId.of("America/Sao_Paulo")
    private val today = LocalDate.of(2026, 10, 11)
    private val user =
        SessionUser(
            id = "sup-1",
            name = "Jones",
            email = "jones@inventra.com",
            accessType = AccessType.SUPERVISOR,
            kitchenId = 7,
            kitchenName = "Sabor Filial Sul",
        )
    private lateinit var alertRepository: FakeAlertRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        alertRepository =
            FakeAlertRepository(
                alerts =
                    listOf(
                        alert(2, "STOCK", "2026-10-10T20:00:00", "Óleo de Soja"),
                        alert(1, "EXPIRATION", "2026-10-11T13:00:00.521912", "Leite Integral"),
                        alert(3, "MANUAL", "2026-10-11T02:00:00", read = true),
                        alert(4, "EXPIRATION", "2026-10-01T12:00:00", "Farinha de Trigo", read = true),
                    ),
            )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createVisibleViewModel(sessionUser: SessionUser? = user): NotificationViewModel {
        val viewModel =
            NotificationViewModel(
                alertRepository = alertRepository,
                authRepository = FakeAuthRepository(initialUser = sessionUser),
                zone = saoPaulo,
                today = { today },
            )
        viewModel.onScreenVisible()
        return viewModel
    }

    private fun NotificationViewModel.titlesByDay(): Map<String, List<String>> {
        val groups = uiState.value.groupedNotifications
        return groups.mapValues { (_, items) -> items.map { it.title } }
    }

    @Test
    fun `carrega os alertas da cozinha do usuario`() =
        runTest(testDispatcher) {
            val viewModel = createVisibleViewModel()
            advanceUntilIdle()

            assertFalse(viewModel.uiState.value.isLoading)
            assertEquals(listOf(7), alertRepository.alertsCalls)
        }

    @Test
    fun `agrupa hoje e ontem no fuso do celular do mais novo para o mais antigo`() =
        runTest(testDispatcher) {
            val viewModel = createVisibleViewModel()
            advanceUntilIdle()

            assertEquals(
                mapOf(
                    "Hoje" to listOf("Leite Integral venceu"),
                    "Ontem" to listOf("Mensagem do alerta 3", "Óleo de Soja abaixo do mínimo"),
                ),
                viewModel.titlesByDay(),
            )
        }

    @Test
    fun `converte o horario de UTC para o fuso do celular`() =
        runTest(testDispatcher) {
            val viewModel = createVisibleViewModel()
            advanceUntilIdle()

            val groups = viewModel.uiState.value.groupedNotifications
            assertEquals("10:00", groups.getValue("Hoje").single().timestamp)
            assertEquals(listOf("23:00", "17:00"), groups.getValue("Ontem").map { it.timestamp })
        }

    @Test
    fun `monta titulo subtitulo e cor pelo tipo do alerta`() =
        runTest(testDispatcher) {
            val viewModel = createVisibleViewModel()
            advanceUntilIdle()

            val groups = viewModel.uiState.value.groupedNotifications
            val expiration = groups.getValue("Hoje").single()
            val (manual, stock) = groups.getValue("Ontem")
            assertEquals("Lote QA-1", expiration.subtitle)
            assertEquals(InventraStatusIconVariant.Danger, expiration.variant)
            assertEquals("Reponha o estoque", stock.subtitle)
            assertEquals(InventraStatusIconVariant.Info, stock.variant)
            assertEquals("", manual.subtitle)
        }

    @Test
    fun `nao lidos ficam marcados`() =
        runTest(testDispatcher) {
            val viewModel = createVisibleViewModel()
            advanceUntilIdle()

            val groups = viewModel.uiState.value.groupedNotifications
            assertTrue(groups.getValue("Hoje").single().isUnread)
            assertEquals(listOf(false, true), groups.getValue("Ontem").map { it.isUnread })
        }

    @Test
    fun `ver todas mostra os alertas antigos e ver menos esconde`() =
        runTest(testDispatcher) {
            val viewModel = createVisibleViewModel()
            advanceUntilIdle()

            viewModel.onSeeAllClick()
            assertEquals(
                listOf("Hoje", "Ontem", "1 de outubro"),
                viewModel.uiState.value.groupedNotifications.keys
                    .toList(),
            )

            viewModel.onSeeAllClick()
            assertEquals(
                listOf("Hoje", "Ontem"),
                viewModel.uiState.value.groupedNotifications.keys
                    .toList(),
            )
        }

    @Test
    fun `tocar num nao lido marca como lido`() =
        runTest(testDispatcher) {
            val viewModel = createVisibleViewModel()
            advanceUntilIdle()

            val leite =
                viewModel.uiState.value.groupedNotifications
                    .getValue("Hoje")
                    .single()
            viewModel.onNotificationClick(leite)
            advanceUntilIdle()

            assertEquals(listOf(1), alertRepository.markedAsRead)
            assertFalse(
                viewModel.uiState.value.groupedNotifications
                    .getValue("Hoje")
                    .single()
                    .isUnread,
            )
        }

    @Test
    fun `tocar num ja lido nao chama a api`() =
        runTest(testDispatcher) {
            val viewModel = createVisibleViewModel()
            advanceUntilIdle()

            val manual =
                viewModel.uiState.value.groupedNotifications
                    .getValue("Ontem")
                    .first()
            viewModel.onNotificationClick(manual)
            advanceUntilIdle()

            assertTrue(alertRepository.markedAsRead.isEmpty())
        }

    @Test
    fun `se marcar como lido falhar volta a ficar nao lido`() =
        runTest(testDispatcher) {
            alertRepository.markAsReadError = IOException("Sem conexão")
            val viewModel = createVisibleViewModel()
            advanceUntilIdle()

            val leite =
                viewModel.uiState.value.groupedNotifications
                    .getValue("Hoje")
                    .single()
            viewModel.onNotificationClick(leite)
            advanceUntilIdle()

            assertTrue(
                viewModel.uiState.value.groupedNotifications
                    .getValue("Hoje")
                    .single()
                    .isUnread,
            )
        }

    @Test
    fun `usuario sem cozinha nao chama a api`() =
        runTest(testDispatcher) {
            val viewModel = createVisibleViewModel(user.copy(kitchenId = null, kitchenName = null))
            advanceUntilIdle()

            assertTrue(
                viewModel.uiState.value.groupedNotifications
                    .isEmpty(),
            )
            assertTrue(alertRepository.alertsCalls.isEmpty())
        }

    @Test
    fun `falha na api mostra o erro e tentar novamente recarrega`() =
        runTest(testDispatcher) {
            alertRepository.error = IOException("Sem conexão")
            val viewModel = createVisibleViewModel()
            advanceUntilIdle()
            assertEquals("Sem conexão", viewModel.uiState.value.errorMessage)

            alertRepository.error = null
            viewModel.onRetryClick()
            advanceUntilIdle()

            assertNull(viewModel.uiState.value.errorMessage)
            assertEquals(
                listOf("Hoje", "Ontem"),
                viewModel.uiState.value.groupedNotifications.keys
                    .toList(),
            )
        }
}
