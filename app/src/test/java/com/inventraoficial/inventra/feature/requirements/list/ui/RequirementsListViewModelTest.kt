package com.inventraoficial.inventra.feature.requirements.list.ui

import com.inventraoficial.inventra.data.local.SessionUser
import com.inventraoficial.inventra.data.remote.dto.requisition.RequisitionStatus
import com.inventraoficial.inventra.data.repository.FakeAuthRepository
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
}
