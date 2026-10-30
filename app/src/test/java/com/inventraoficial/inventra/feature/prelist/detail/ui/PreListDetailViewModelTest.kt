package com.inventraoficial.inventra.feature.prelist.detail.ui

import com.inventraoficial.inventra.data.remote.dto.requisition.RequisitionType
import com.inventraoficial.inventra.data.repository.FakeAuthRepository
import com.inventraoficial.inventra.data.repository.FakePreListRepository
import com.inventraoficial.inventra.data.repository.FakePreListRepository.Companion.preList
import com.inventraoficial.inventra.data.repository.FakePreListRepository.Companion.preListItem
import com.inventraoficial.inventra.data.repository.FakeRequisitionRepository
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
import org.junit.Before
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class PreListDetailViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var preListRepository: FakePreListRepository
    private lateinit var requisitionRepository: FakeRequisitionRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        preListRepository =
            FakePreListRepository(
                preLists =
                    listOf(
                        preList(
                            2,
                            "2026-10-24",
                            listOf(
                                preListItem(10, "Leite Integral", 12.0, supplierId = 2),
                                preListItem(11, "Manteiga", 4.0, supplierId = 2),
                                preListItem(12, "Frango", 8.5, supplierId = 1),
                                preListItem(13, "Sal", 1.0, supplierId = null),
                            ),
                        ),
                    ),
            )
        requisitionRepository = FakeRequisitionRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() =
        PreListDetailViewModel(
            preListId = 2,
            authRepository = FakeAuthRepository(initialUser = RequisitionTestUsers.buyer),
            preListRepository = preListRepository,
            requisitionRepository = requisitionRepository,
        )

    @Test
    fun `mostra os itens agrupados por fornecedor`() =
        runTest(testDispatcher) {
            val viewModel = createViewModel()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertFalse(state.isLoading)
            assertEquals("24/10", state.dateText)
            assertEquals("4 itens · 2 fornecedores", state.summaryText)
            assertEquals(listOf("Fornecedor 1", "Fornecedor 2", "Sem fornecedor"), state.groups.map { it.supplierName })
            assertEquals(listOf("Leite Integral", "Manteiga"), state.groups[1].items.map { it.name })
            assertEquals(
                "Qtd. 8,5",
                state.groups[0]
                    .items
                    .single()
                    .quantityText,
            )
        }

    @Test
    fun `transformar em compra cria o rascunho e abre o editor`() =
        runTest(testDispatcher) {
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.onConvertClick()
            advanceUntilIdle()

            val created = requisitionRepository.created.single()
            assertEquals(RequisitionType.PURCHASE, created.type)
            assertEquals("Pré-lista 24/10", created.origin)
            assertEquals(listOf(10, 11, 12, 13), requisitionRepository.addedItems.map { it.second.productId })
            assertEquals(100, viewModel.uiState.value.openedDraftId)
        }

    @Test
    fun `falha ao carregar mostra o erro e tentar novamente recarrega`() =
        runTest(testDispatcher) {
            preListRepository.error = IOException("Sem conexão")
            val viewModel = createViewModel()
            advanceUntilIdle()
            assertEquals("Sem conexão", viewModel.uiState.value.errorMessage)

            preListRepository.error = null
            viewModel.onRetryClick()
            advanceUntilIdle()

            assertNull(viewModel.uiState.value.errorMessage)
            assertEquals(3, viewModel.uiState.value.groups.size)
        }

    @Test
    fun `falha ao transformar mostra o erro sem abrir o editor`() =
        runTest(testDispatcher) {
            val viewModel = createViewModel()
            advanceUntilIdle()

            requisitionRepository.actionError = IOException("Sem conexão")
            viewModel.onConvertClick()
            advanceUntilIdle()

            assertEquals("Sem conexão", viewModel.uiState.value.convertErrorMessage)
            assertNull(viewModel.uiState.value.openedDraftId)
        }
}
