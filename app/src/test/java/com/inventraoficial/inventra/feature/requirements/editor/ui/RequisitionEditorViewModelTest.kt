package com.inventraoficial.inventra.feature.requirements.editor.ui

import com.inventraoficial.inventra.data.local.SessionUser
import com.inventraoficial.inventra.data.remote.dto.requisition.AddRequisitionItemRequest
import com.inventraoficial.inventra.data.remote.dto.requisition.CreateRequisitionRequest
import com.inventraoficial.inventra.data.remote.dto.requisition.RequisitionStatus
import com.inventraoficial.inventra.data.remote.dto.requisition.RequisitionType
import com.inventraoficial.inventra.data.repository.FakeAuthRepository
import com.inventraoficial.inventra.data.repository.FakeProductRepository
import com.inventraoficial.inventra.data.repository.FakeRequisitionRepository
import com.inventraoficial.inventra.data.repository.FakeRequisitionRepository.Companion.item
import com.inventraoficial.inventra.data.repository.FakeRequisitionRepository.Companion.requisition
import com.inventraoficial.inventra.data.repository.FakeSupplierRepository
import com.inventraoficial.inventra.feature.requirements.RequisitionTestUsers
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

@OptIn(ExperimentalCoroutinesApi::class)
class RequisitionEditorViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private val leite = ProductOption(5013, "Leite Integral", "L")
    private lateinit var requisitions: FakeRequisitionRepository
    private lateinit var products: FakeProductRepository
    private lateinit var suppliers: FakeSupplierRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        requisitions = FakeRequisitionRepository()
        products =
            FakeProductRepository(
                products =
                    listOf(
                        FakeProductRepository.product(5013, "Leite Integral"),
                        FakeProductRepository.product(5014, "Arroz Camil", unitSymbol = "kg"),
                    ),
            )
        suppliers = FakeSupplierRepository(suppliers = listOf(FakeSupplierRepository.supplier(1, "Italac")))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun TestScope.editor(
        draftId: Int? = null,
        user: SessionUser = RequisitionTestUsers.buyer,
    ): RequisitionEditorViewModel {
        val viewModel =
            RequisitionEditorViewModel(
                draftId = draftId,
                requisitionRepository = requisitions,
                productRepository = products,
                supplierRepository = suppliers,
                authRepository = FakeAuthRepository(initialUser = user),
            )
        viewModel.onScreenVisible()
        advanceUntilIdle()
        return viewModel
    }

    private fun TestScope.addItem(
        viewModel: RequisitionEditorViewModel,
        quantity: String = "20",
        price: String = "",
        supplierId: Int? = null,
    ) {
        viewModel.onAddItemClick()
        viewModel.onProductSelect(leite)
        viewModel.onQuantityChange(quantity)
        viewModel.onPriceChange(price)
        viewModel.onSupplierSelect(supplierId)
        viewModel.onConfirmItem()
        advanceUntilIdle()
    }

    private fun RequisitionEditorViewModel.state() = uiState.value

    @Test
    fun `nova requisicao comeca como compra e carrega os fornecedores da cozinha`() =
        runTest(testDispatcher) {
            val viewModel = editor()

            assertEquals(RequisitionType.PURCHASE, viewModel.state().type)
            assertFalse(viewModel.state().isDraftCreated)
            assertEquals(listOf(SupplierOption(1, "Italac")), viewModel.state().suppliers)
            assertFalse(viewModel.state().canSubmit)
        }

    @Test
    fun `origem e limitada a 20 caracteres`() =
        runTest(testDispatcher) {
            val viewModel = editor()

            viewModel.onOriginChange("Reposição semanal da cozinha")

            assertEquals("Reposição semanal da", viewModel.state().origin)
        }

    @Test
    fun `adicionar item sem origem pede a origem e transferencia pede o destino`() =
        runTest(testDispatcher) {
            val viewModel = editor()

            viewModel.onAddItemClick()
            assertEquals("Informe a origem antes de adicionar itens.", viewModel.state().errorMessage)

            viewModel.onTypeSelect(RequisitionType.TRANSFER)
            viewModel.onAddItemClick()
            assertEquals("Informe o destino antes de adicionar itens.", viewModel.state().errorMessage)
            assertNull(viewModel.state().itemForm)
        }

    @Test
    fun `busca de produto espera o usuario parar de digitar e ignora termos curtos`() =
        runTest(testDispatcher) {
            val viewModel = editor()
            viewModel.onOriginChange("Reposição")
            viewModel.onAddItemClick()

            viewModel.onQueryChange("l")
            advanceUntilIdle()
            assertTrue(products.searches.isEmpty())

            viewModel.onQueryChange("le")
            viewModel.onQueryChange("lei")
            advanceUntilIdle()

            assertEquals(listOf("lei"), products.searches)
            assertEquals(listOf(leite), viewModel.state().itemForm?.results)
        }

    @Test
    fun `item sem produto ou com quantidade invalida nao chama a api`() =
        runTest(testDispatcher) {
            val viewModel = editor()
            viewModel.onOriginChange("Reposição")
            viewModel.onAddItemClick()

            viewModel.onConfirmItem()
            assertEquals("Escolha um produto da lista.", viewModel.state().itemForm?.errorMessage)

            viewModel.onProductSelect(leite)
            viewModel.onQuantityChange("0")
            viewModel.onConfirmItem()
            assertEquals("Informe uma quantidade maior que zero.", viewModel.state().itemForm?.errorMessage)

            advanceUntilIdle()
            assertTrue(requisitions.created.isEmpty())
        }

    @Test
    fun `primeiro item cria o rascunho e trava tipo e origem`() =
        runTest(testDispatcher) {
            val viewModel = editor()
            viewModel.onTypeSelect(RequisitionType.CONSUMPTION)
            viewModel.onOriginChange(" Almoço ")

            addItem(viewModel, quantity = "2,5", price = "4,50")

            val expected = CreateRequisitionRequest(RequisitionType.CONSUMPTION, "Almoço", 7)
            assertEquals(listOf(expected), requisitions.created)
            assertEquals(100, viewModel.state().requisitionId)
            assertNull(viewModel.state().itemForm)
            assertEquals(listOf("Qtd. 2,5"), viewModel.state().items.map { it.quantityText })
            assertEquals("R$ 11,25", viewModel.state().totalText)

            viewModel.onTypeSelect(RequisitionType.PURCHASE)
            viewModel.onOriginChange("Outra")
            assertEquals(RequisitionType.CONSUMPTION, viewModel.state().type)
            assertEquals(" Almoço ", viewModel.state().origin)
        }

    @Test
    fun `segundo item usa o mesmo rascunho`() =
        runTest(testDispatcher) {
            val viewModel = editor()
            viewModel.onOriginChange("Reposição")

            addItem(viewModel)
            addItem(viewModel, quantity = "3")

            assertEquals(1, requisitions.created.size)
            assertEquals(listOf(100, 100), requisitions.addedItems.map { it.first })
            assertEquals(2, viewModel.state().items.size)
            assertTrue(viewModel.state().canSubmit)
        }

    @Test
    fun `fornecedor sugerido so vai em compras`() =
        runTest(testDispatcher) {
            val purchase = editor()
            purchase.onOriginChange("Reposição")
            addItem(purchase, supplierId = 1)

            val consumption = editor()
            consumption.onTypeSelect(RequisitionType.CONSUMPTION)
            consumption.onOriginChange("Almoço")
            addItem(consumption, supplierId = 1)

            assertEquals(
                listOf(
                    AddRequisitionItemRequest(productId = 5013, quantity = 20.0, suggestedSupplierId = 1),
                    AddRequisitionItemRequest(productId = 5013, quantity = 20.0),
                ),
                requisitions.addedItems.map { it.second },
            )
        }

    @Test
    fun `falha ao adicionar mantem o formulario com o erro`() =
        runTest(testDispatcher) {
            val viewModel = editor()
            viewModel.onOriginChange("Reposição")
            requisitions.actionError = IOException("Sem conexão")

            addItem(viewModel)

            assertEquals("Sem conexão", viewModel.state().itemForm?.errorMessage)
            assertFalse(viewModel.state().isSaving)
        }

    @Test
    fun `remover item recarrega a lista`() =
        runTest(testDispatcher) {
            val viewModel = editor()
            viewModel.onOriginChange("Reposição")
            addItem(viewModel)
            val itemId =
                viewModel
                    .state()
                    .items
                    .single()
                    .id

            viewModel.onRemoveItemClick(itemId)
            advanceUntilIdle()

            assertTrue(viewModel.state().items.isEmpty())
            assertFalse(viewModel.state().canSubmit)
        }

    @Test
    fun `enviar para analise fecha o editor`() =
        runTest(testDispatcher) {
            val viewModel = editor()
            viewModel.onOriginChange("Reposição")
            addItem(viewModel)

            viewModel.onSubmitClick()
            advanceUntilIdle()

            assertTrue("submit" to 100 in requisitions.actions)
            assertEquals(EditorResult.Submitted, viewModel.state().result)
        }

    @Test
    fun `descartar sem rascunho so fecha e com rascunho cancela`() =
        runTest(testDispatcher) {
            val empty = editor()
            empty.onDiscardClick()
            advanceUntilIdle()
            assertEquals(EditorResult.Discarded, empty.state().result)
            assertTrue(requisitions.actions.isEmpty())

            val draft = editor()
            draft.onOriginChange("Reposição")
            addItem(draft)
            draft.onDiscardClick()
            advanceUntilIdle()

            assertTrue("cancel" to 100 in requisitions.actions)
            assertEquals(listOf("Rascunho descartado"), requisitions.reasons)
            assertEquals(EditorResult.Discarded, draft.state().result)
        }

    @Test
    fun `abrir um rascunho existente carrega tipo origem e itens`() =
        runTest(testDispatcher) {
            requisitions.requisitions =
                listOf(requisition(20, status = RequisitionStatus.DRAFT, type = RequisitionType.TRANSFER))
            requisitions.itemsById = mapOf(20 to listOf(item(1, "Leite Integral", 24.0, estimatedPrice = 5.0)))

            val viewModel = editor(draftId = 20)

            assertFalse(viewModel.state().isLoading)
            assertEquals(RequisitionType.TRANSFER, viewModel.state().type)
            assertEquals("Reposição semanal", viewModel.state().origin)
            assertEquals(listOf("Leite Integral"), viewModel.state().items.map { it.name })
            assertEquals(
                "Preço est. R$ 5,00 · Italac",
                viewModel
                    .state()
                    .items
                    .single()
                    .details,
            )
            assertEquals("R$ 120,00", viewModel.state().totalText)
        }

    @Test
    fun `requisicao ja enviada nao abre no editor`() =
        runTest(testDispatcher) {
            requisitions.requisitions = listOf(requisition(21))

            val viewModel = editor(draftId = 21)

            assertEquals("Essa requisição já foi enviada.", viewModel.state().errorMessage)
        }
}
