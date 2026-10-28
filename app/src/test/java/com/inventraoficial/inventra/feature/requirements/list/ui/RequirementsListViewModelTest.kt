package com.inventraoficial.inventra.feature.requirements.list.ui

import com.inventraoficial.inventra.data.local.SessionUser
import com.inventraoficial.inventra.data.remote.dto.auth.AccessType
import com.inventraoficial.inventra.data.remote.dto.requisition.AddRequisitionItemRequest
import com.inventraoficial.inventra.data.remote.dto.requisition.CreateRequisitionRequest
import com.inventraoficial.inventra.data.remote.dto.requisition.RequisitionStatus
import com.inventraoficial.inventra.data.remote.dto.requisition.RequisitionType
import com.inventraoficial.inventra.data.repository.FakeAuthRepository
import com.inventraoficial.inventra.data.repository.FakePreListRepository
import com.inventraoficial.inventra.data.repository.FakePreListRepository.Companion.preList
import com.inventraoficial.inventra.data.repository.FakePreListRepository.Companion.preListItem
import com.inventraoficial.inventra.data.repository.FakeRequisitionRepository
import com.inventraoficial.inventra.data.repository.FakeRequisitionRepository.Companion.otherBuyer
import com.inventraoficial.inventra.data.repository.FakeRequisitionRepository.Companion.requisition
import com.inventraoficial.inventra.feature.requirements.RequisitionTestUsers
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
import java.time.ZoneId

@OptIn(ExperimentalCoroutinesApi::class)
class RequirementsListViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: FakeRequisitionRepository
    private lateinit var preListRepository: FakePreListRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository =
            FakeRequisitionRepository(
                requisitions =
                    listOf(
                        requisition(1, createdAt = "2026-10-10T12:00:00").copy(
                            itemCount = 4,
                            estimatedTotal = 131.76,
                            itemNames = listOf("Leite Integral", "Arroz Camil", "Feijão Carioca"),
                        ),
                        requisition(2, requester = otherBuyer, createdAt = "2026-10-11T02:00:00")
                            .copy(itemCount = 1, itemNames = listOf("Tomate")),
                        requisition(3, status = RequisitionStatus.APPROVED),
                        requisition(4, status = RequisitionStatus.DRAFT, createdAt = "2026-10-09T12:00:00"),
                    ),
            )
        preListRepository =
            FakePreListRepository(
                preLists =
                    listOf(
                        preList(1, "2026-10-23", listOf(preListItem(1, "Tomate", 3.0))),
                        preList(
                            2,
                            "2026-10-24",
                            listOf(
                                preListItem(10, "Leite Integral", 12.0, supplierId = 1),
                                preListItem(11, "Manteiga", 4.0, supplierId = 1, photoUrl = "https://img/manteiga.jpg"),
                                preListItem(12, "Frango", 8.5, supplierId = 2),
                                preListItem(13, "Arroz", 10.0, supplierId = 3, photoUrl = "https://img/arroz.jpg"),
                                preListItem(14, "Sal", 1.0, supplierId = null),
                            ),
                        ),
                    ),
            )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun visibleViewModel(user: SessionUser?): RequirementsListViewModel {
        val viewModel =
            RequirementsListViewModel(
                authRepository = FakeAuthRepository(initialUser = user),
                requisitionRepository = repository,
                preListRepository = preListRepository,
                zone = ZoneId.of("America/Sao_Paulo"),
            )
        viewModel.onScreenVisible()
        return viewModel
    }

    @Test
    fun `supervisor ve todas as requisicoes em analise da mais nova para a mais antiga`() =
        runTest(testDispatcher) {
            val viewModel = visibleViewModel(RequisitionTestUsers.supervisor)
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertFalse(state.isLoading)
            assertTrue(state.isSupervisor)
            assertEquals(listOf(2, 1), state.requisitions.map { it.id })
            assertEquals(listOf(Triple(7, null, null)), repository.listCalls)
        }

    @Test
    fun `comprador ve os proprios rascunhos primeiro e depois as em analise`() =
        runTest(testDispatcher) {
            val viewModel = visibleViewModel(RequisitionTestUsers.buyer)
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertFalse(state.isSupervisor)
            assertEquals(listOf(4, 1), state.requisitions.map { it.id })
            assertEquals(RequisitionStatus.DRAFT, state.requisitions.first().status)
            assertEquals(listOf(Triple(7, null, "buy-1")), repository.listCalls)
        }

    @Test
    fun `card mostra tipo, solicitante, data no fuso do celular e resumo dos itens`() =
        runTest(testDispatcher) {
            val viewModel = visibleViewModel(RequisitionTestUsers.supervisor)
            advanceUntilIdle()

            val (newest, oldest) = viewModel.uiState.value.requisitions
            assertEquals("Compra #2", newest.title)
            assertEquals("Paulo Lima · 10/10/2026", newest.subtitle)
            assertEquals("Tomate", newest.details)
            assertEquals("Leite Integral, Arroz Camil e mais 2 · R$ 131,76", oldest.details)
        }

    @Test
    fun `usuario sem cozinha fica com a lista vazia`() =
        runTest(testDispatcher) {
            val viewModel = visibleViewModel(RequisitionTestUsers.supervisor.copy(kitchenId = null))
            advanceUntilIdle()

            assertTrue(
                viewModel.uiState.value.requisitions
                    .isEmpty(),
            )
        }

    @Test
    fun `falha na api mostra o erro e tentar novamente recarrega`() =
        runTest(testDispatcher) {
            repository.error = IOException("Sem conexão")
            val viewModel = visibleViewModel(RequisitionTestUsers.supervisor)
            advanceUntilIdle()
            assertEquals("Sem conexão", viewModel.uiState.value.errorMessage)

            repository.error = null
            viewModel.onRetryClick()
            advanceUntilIdle()

            assertNull(viewModel.uiState.value.errorMessage)
            assertEquals(2, viewModel.uiState.value.requisitions.size)
        }

    @Test
    fun `comprador ve a pre-lista mais recente no topo`() =
        runTest(testDispatcher) {
            val viewModel = visibleViewModel(RequisitionTestUsers.buyer)
            advanceUntilIdle()

            val preList = viewModel.uiState.value.preList!!
            assertEquals(2, preList.id)
            assertEquals("24/10", preList.dateText)
            assertEquals("5 itens · 3 fornecedores", preList.summaryText)
            assertEquals(listOf("https://img/manteiga.jpg", "https://img/arroz.jpg", null, null), preList.photoUrls)
            assertEquals(1, preList.extraCount)
            assertEquals(listOf(2), preListRepository.detailCalls)
        }

    @Test
    fun `estoquista nao busca a pre-lista`() =
        runTest(testDispatcher) {
            val viewModel = visibleViewModel(RequisitionTestUsers.buyer.copy(accessType = AccessType.ESTOQUISTA))
            advanceUntilIdle()

            assertNull(viewModel.uiState.value.preList)
            assertEquals(0, preListRepository.listCalls)
        }

    @Test
    fun `falha na pre-lista esconde o card sem derrubar as requisicoes`() =
        runTest(testDispatcher) {
            preListRepository.error = IOException("Sem conexão")
            val viewModel = visibleViewModel(RequisitionTestUsers.supervisor)
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertNull(state.preList)
            assertNull(state.errorMessage)
            assertEquals(2, state.requisitions.size)
        }

    @Test
    fun `transformar em compra cria o rascunho com os itens e abre o editor`() =
        runTest(testDispatcher) {
            val viewModel = visibleViewModel(RequisitionTestUsers.buyer)
            advanceUntilIdle()

            viewModel.onPreListConvertClick()
            assertTrue(viewModel.uiState.value.isConvertingPreList)
            advanceUntilIdle()

            assertEquals(
                listOf(CreateRequisitionRequest(RequisitionType.PURCHASE, "Pré-lista 24/10", 7)),
                repository.created,
            )
            assertEquals(
                listOf(
                    AddRequisitionItemRequest(10, 12.0, suggestedSupplierId = 1),
                    AddRequisitionItemRequest(11, 4.0, suggestedSupplierId = 1),
                    AddRequisitionItemRequest(12, 8.5, suggestedSupplierId = 2),
                    AddRequisitionItemRequest(13, 10.0, suggestedSupplierId = 3),
                    AddRequisitionItemRequest(14, 1.0),
                ),
                repository.addedItems.map { it.second },
            )
            assertEquals(100, viewModel.uiState.value.openedDraftId)
            assertFalse(viewModel.uiState.value.isConvertingPreList)
            assertNull(viewModel.uiState.value.preList)

            viewModel.onDraftOpened()
            assertNull(viewModel.uiState.value.openedDraftId)
        }

    @Test
    fun `pre-lista que ja virou compra nao aparece de novo`() =
        runTest(testDispatcher) {
            val viewModel = visibleViewModel(RequisitionTestUsers.buyer)
            advanceUntilIdle()
            viewModel.onPreListConvertClick()
            advanceUntilIdle()

            viewModel.onScreenVisible()
            advanceUntilIdle()

            assertNull(viewModel.uiState.value.preList)
            assertEquals(1, repository.created.size)
        }

    @Test
    fun `compra cancelada ou recusada da pre-lista deixa o card voltar`() =
        runTest(testDispatcher) {
            repository.requisitions +=
                listOf(
                    requisition(50, status = RequisitionStatus.CANCELLED).copy(origin = "Pré-lista 24/10"),
                    requisition(51, status = RequisitionStatus.REJECTED).copy(origin = "Pré-lista 24/10"),
                    requisition(52, type = RequisitionType.TRANSFER).copy(origin = "Pré-lista 24/10"),
                )
            val viewModel = visibleViewModel(RequisitionTestUsers.buyer)
            advanceUntilIdle()

            assertEquals(
                2,
                viewModel.uiState.value.preList
                    ?.id,
            )
        }

    @Test
    fun `falha no meio da conversao cancela o rascunho incompleto`() =
        runTest(testDispatcher) {
            repository.addItemErrorAfter = 2
            val viewModel = visibleViewModel(RequisitionTestUsers.buyer)
            advanceUntilIdle()

            viewModel.onPreListConvertClick()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertEquals(listOf("cancel" to 100), repository.actions)
            assertEquals("Produto inativo.", state.preListErrorMessage)
            assertNull(state.openedDraftId)
            assertEquals(2, state.preList?.id)
        }

    @Test
    fun `falha ao transformar mostra o erro no card`() =
        runTest(testDispatcher) {
            val viewModel = visibleViewModel(RequisitionTestUsers.buyer)
            advanceUntilIdle()

            repository.actionError = IOException("Sem conexão")
            viewModel.onPreListConvertClick()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertEquals("Sem conexão", state.preListErrorMessage)
            assertNull(state.openedDraftId)
            assertFalse(state.isConvertingPreList)
        }
}
