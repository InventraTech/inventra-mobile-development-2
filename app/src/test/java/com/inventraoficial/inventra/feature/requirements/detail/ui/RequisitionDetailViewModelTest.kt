package com.inventraoficial.inventra.feature.requirements.detail.ui

import com.inventraoficial.inventra.data.local.SessionUser
import com.inventraoficial.inventra.data.remote.dto.requisition.RequisitionStatus
import com.inventraoficial.inventra.data.repository.FakeAuthRepository
import com.inventraoficial.inventra.data.repository.FakeRequisitionRepository
import com.inventraoficial.inventra.data.repository.FakeRequisitionRepository.Companion.item
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
import java.time.ZoneOffset

@OptIn(ExperimentalCoroutinesApi::class)
class RequisitionDetailViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: FakeRequisitionRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository =
            FakeRequisitionRepository(
                requisitions =
                    listOf(
                        requisition(12),
                        requisition(13, status = RequisitionStatus.APPROVED),
                    ),
                itemsById =
                    mapOf(
                        12 to
                            listOf(
                                item(1, "Leite Integral", 24.0, estimatedPrice = 5.49),
                                item(2, "Arroz Camil", 10.0),
                            ),
                    ),
            )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(
        user: SessionUser = RequisitionTestUsers.supervisor,
        id: Int = 12,
    ) = RequisitionDetailViewModel(
        requisitionId = id,
        authRepository = FakeAuthRepository(initialUser = user),
        requisitionRepository = repository,
        zone = ZoneOffset.UTC,
    )

    @Test
    fun `mostra os dados e os itens da requisicao`() =
        runTest(testDispatcher) {
            val viewModel = createViewModel()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertFalse(state.isLoading)
            assertEquals("Compra #12", state.title)
            assertEquals("Maria Souza", state.requesterName)
            assertEquals("11/10/2026", state.dateText)
            assertEquals("Reposição semanal", state.origin)
            assertEquals(listOf("Leite Integral", "Arroz Camil"), state.items.map { it.name })
            assertEquals("Qtd. 24", state.items.first().quantityText)
            assertEquals("Preço estimado R$ 5,49", state.items.first().priceText)
            assertEquals("R$ 131,76", state.totalText)
        }

    @Test
    fun `supervisor pode analisar e comprador pode cancelar a propria`() =
        runTest(testDispatcher) {
            val asSupervisor = createViewModel(RequisitionTestUsers.supervisor)
            val asBuyer = createViewModel(RequisitionTestUsers.buyer)
            advanceUntilIdle()

            assertTrue(asSupervisor.uiState.value.canReview)
            assertFalse(asSupervisor.uiState.value.canCancel)
            assertFalse(asBuyer.uiState.value.canReview)
            assertTrue(asBuyer.uiState.value.canCancel)
        }

    @Test
    fun `requisicao ja decidida nao tem acoes`() =
        runTest(testDispatcher) {
            val viewModel = createViewModel(id = 13)
            advanceUntilIdle()

            assertFalse(viewModel.uiState.value.canReview)
            assertFalse(viewModel.uiState.value.canCancel)
        }

    @Test
    fun `aprovar pede confirmacao e depois fecha a tela`() =
        runTest(testDispatcher) {
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.onApproveClick()
            assertEquals(PendingAction.Approve, viewModel.uiState.value.pendingAction)

            viewModel.onDialogConfirm()
            advanceUntilIdle()

            assertEquals(listOf("approve" to 12), repository.actions)
            assertTrue(viewModel.uiState.value.isFinished)
        }

    @Test
    fun `recusar sem motivo nao chama a api`() =
        runTest(testDispatcher) {
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.onRejectClick()
            viewModel.onReasonChange("   ")
            viewModel.onDialogConfirm()
            advanceUntilIdle()

            assertTrue(repository.actions.isEmpty())
            assertEquals("Informe o motivo.", viewModel.uiState.value.actionErrorMessage)
            assertEquals(PendingAction.Reject, viewModel.uiState.value.pendingAction)
        }

    @Test
    fun `recusar com motivo envia o motivo sem espacos nas pontas`() =
        runTest(testDispatcher) {
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.onRejectClick()
            viewModel.onReasonChange("  Fora do orçamento  ")
            viewModel.onDialogConfirm()
            advanceUntilIdle()

            assertEquals(listOf("reject" to 12), repository.actions)
            assertEquals(listOf("Fora do orçamento"), repository.reasons)
            assertTrue(viewModel.uiState.value.isFinished)
        }

    @Test
    fun `comprador cancela com motivo`() =
        runTest(testDispatcher) {
            val viewModel = createViewModel(RequisitionTestUsers.buyer)
            advanceUntilIdle()

            viewModel.onCancelClick()
            viewModel.onReasonChange("Pedido duplicado")
            viewModel.onDialogConfirm()
            advanceUntilIdle()

            assertEquals(listOf("cancel" to 12), repository.actions)
        }

    @Test
    fun `erro na acao fecha a janela e mostra a mensagem`() =
        runTest(testDispatcher) {
            repository.actionError = IOException("Estoque insuficiente para atender a quantidade solicitada.")
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.onApproveClick()
            viewModel.onDialogConfirm()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertNull(state.pendingAction)
            assertFalse(state.isFinished)
            assertEquals("Estoque insuficiente para atender a quantidade solicitada.", state.actionErrorMessage)
        }

    @Test
    fun `requisicao que nao existe mostra erro`() =
        runTest(testDispatcher) {
            val viewModel = createViewModel(id = 99)
            advanceUntilIdle()

            assertEquals("Requisição não encontrada.", viewModel.uiState.value.errorMessage)
        }
}
