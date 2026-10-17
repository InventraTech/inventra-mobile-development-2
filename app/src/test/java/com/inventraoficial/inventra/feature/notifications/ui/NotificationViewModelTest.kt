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

    private fun useSystemAlerts() {
        alertRepository.alerts =
            listOf(
                alert(10, "ACCESS_REQUEST", "2026-10-11T14:00:00")
                    .copy(refKey = "55")
                    .copy(message = "Mauro Rosa pediu para entrar na cozinha."),
                alert(11, "REQUISITION_APPROVED", "2026-10-11T13:00:00")
                    .copy(refKey = "12")
                    .copy(message = "Sua requisição #12 foi aprovada."),
                alert(12, "REQUISITION_SUBMITTED", "2026-10-11T12:00:00", read = true).copy(refKey = "13"),
                alert(13, "EXPIRING", "2026-10-11T11:00:00", "Leite Integral")
                    .copy(message = "Lote QA-13 vence em 2 dias (13/10)."),
                alert(14, "UNREGISTERED_PRODUCT", "2026-10-11T10:00:00")
                    .copy(refKey = "7898080640611")
                    .copy(message = "Produto não cadastrado: Leite Italac 1L (7898080640611)"),
            )
    }

    @Test
    fun `monta titulo subtitulo e cor dos tipos novos`() =
        runTest(testDispatcher) {
            useSystemAlerts()
            val viewModel = createVisibleViewModel()
            advanceUntilIdle()

            val today =
                viewModel.uiState.value.groupedNotifications
                    .getValue("Hoje")
            assertEquals(
                listOf(
                    "Pedido de entrada na cozinha",
                    "Requisição aprovada",
                    "Nova requisição para análise",
                    "Leite Integral vence em breve",
                    "Produto não cadastrado",
                ),
                today.map { it.title },
            )
            assertEquals("Mauro Rosa pediu para entrar na cozinha.", today[0].subtitle)
            assertEquals("Lote QA-13 vence em 2 dias (13/10).", today[3].subtitle)
            assertEquals(InventraStatusIconVariant.Action, today[0].variant)
            assertEquals(InventraStatusIconVariant.Success, today[1].variant)
        }

    @Test
    fun `filtro separa alertas de estoque dos eventos do sistema`() =
        runTest(testDispatcher) {
            useSystemAlerts()
            val viewModel = createVisibleViewModel()
            advanceUntilIdle()

            viewModel.onFilterSelect(NotificationFilter.Alerts)
            assertEquals(
                listOf(13, 14),
                viewModel.uiState.value.groupedNotifications.values
                    .flatten()
                    .map { it.id },
            )

            viewModel.onFilterSelect(NotificationFilter.System)
            assertEquals(
                listOf(10, 11, 12),
                viewModel.uiState.value.groupedNotifications.values
                    .flatten()
                    .map { it.id },
            )
        }

    @Test
    fun `cada notificacao sabe para onde levar`() =
        runTest(testDispatcher) {
            useSystemAlerts()
            val viewModel = createVisibleViewModel()
            advanceUntilIdle()

            assertEquals(
                mapOf(
                    10 to NotificationTarget.Members,
                    11 to NotificationTarget.Requisition(12),
                    12 to NotificationTarget.Requisition(13),
                    13 to NotificationTarget.Product(13),
                ),
                viewModel.uiState.value.targets,
            )
        }

    @Test
    fun `marcar todas como lidas zera os nao lidos`() =
        runTest(testDispatcher) {
            val viewModel = createVisibleViewModel()
            advanceUntilIdle()
            assertTrue(viewModel.uiState.value.hasUnread)

            viewModel.onMarkAllReadClick()
            advanceUntilIdle()

            assertEquals(listOf(7), alertRepository.markedAllKitchens)
            assertFalse(viewModel.uiState.value.hasUnread)
            assertTrue(
                viewModel.uiState.value.groupedNotifications.values
                    .flatten()
                    .none { it.isUnread },
            )
        }

    @Test
    fun `se marcar todas falhar os nao lidos voltam`() =
        runTest(testDispatcher) {
            alertRepository.markAllError = IOException("Sem conexão")
            val viewModel = createVisibleViewModel()
            advanceUntilIdle()

            viewModel.onMarkAllReadClick()
            advanceUntilIdle()

            assertTrue(viewModel.uiState.value.hasUnread)
            assertTrue(
                viewModel.uiState.value.groupedNotifications
                    .getValue("Hoje")
                    .single()
                    .isUnread,
            )
        }
}
