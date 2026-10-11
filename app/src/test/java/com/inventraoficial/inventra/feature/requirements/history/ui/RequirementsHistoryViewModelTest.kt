package com.inventraoficial.inventra.feature.requirements.history.ui

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
import org.junit.Before
import org.junit.Test
import java.time.ZoneOffset

@OptIn(ExperimentalCoroutinesApi::class)
class RequirementsHistoryViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: FakeRequisitionRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository =
            FakeRequisitionRepository(
                requisitions =
                    listOf(
                        requisition(1, status = RequisitionStatus.UNDER_REVIEW),
                        requisition(2, status = RequisitionStatus.APPROVED, createdAt = "2026-10-09T10:00:00"),
                        requisition(
                            3,
                            status = RequisitionStatus.REJECTED,
                            createdAt = "2026-10-10T10:00:00",
                            reason = "Fora do orçamento",
                        ),
                        requisition(4, status = RequisitionStatus.CANCELLED, requester = otherBuyer),
                    ),
            )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun visibleViewModel(user: SessionUser): RequirementsHistoryViewModel {
        val viewModel =
            RequirementsHistoryViewModel(
                authRepository = FakeAuthRepository(initialUser = user),
                requisitionRepository = repository,
                zone = ZoneOffset.UTC,
            )
        viewModel.onScreenVisible()
        return viewModel
    }

    @Test
    fun `mostra so as requisicoes ja decididas da mais nova para a mais antiga`() =
        runTest(testDispatcher) {
            val viewModel = visibleViewModel(RequisitionTestUsers.supervisor)
            advanceUntilIdle()

            assertEquals(
                listOf(4, 3, 2),
                viewModel.uiState.value.requisitions
                    .map { it.id },
            )
        }

    @Test
    fun `comprador ve so as proprias`() =
        runTest(testDispatcher) {
            val viewModel = visibleViewModel(RequisitionTestUsers.buyer)
            advanceUntilIdle()

            assertEquals(
                listOf(3, 2),
                viewModel.uiState.value.requisitions
                    .map { it.id },
            )
        }

    @Test
    fun `filtra por status`() =
        runTest(testDispatcher) {
            val viewModel = visibleViewModel(RequisitionTestUsers.supervisor)
            advanceUntilIdle()

            viewModel.onFilterSelect(HistoryFilter.Rejected)
            assertEquals(
                listOf(3),
                viewModel.uiState.value.requisitions
                    .map { it.id },
            )

            viewModel.onFilterSelect(HistoryFilter.All)
            assertEquals(3, viewModel.uiState.value.requisitions.size)
        }

    @Test
    fun `recusada mostra o motivo e aprovada mostra a origem`() =
        runTest(testDispatcher) {
            val viewModel = visibleViewModel(RequisitionTestUsers.supervisor)
            advanceUntilIdle()

            val byId =
                viewModel.uiState.value.requisitions
                    .associateBy { it.id }
            assertEquals("Motivo: Fora do orçamento", byId.getValue(3).details)
            assertEquals("Reposição semanal", byId.getValue(2).details)
        }
}
