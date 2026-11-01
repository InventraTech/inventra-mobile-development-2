package com.inventraoficial.inventra.feature.stock.edit.ui

import com.inventraoficial.inventra.data.local.SessionUser
import com.inventraoficial.inventra.data.remote.dto.auth.AccessType
import com.inventraoficial.inventra.data.remote.dto.product.KitchenParametersKitchen
import com.inventraoficial.inventra.data.remote.dto.product.KitchenParametersRequest
import com.inventraoficial.inventra.data.remote.dto.product.KitchenParametersResponse
import com.inventraoficial.inventra.data.remote.dto.product.LinkSupplierRequest
import com.inventraoficial.inventra.data.remote.dto.product.UpdateProductRequest
import com.inventraoficial.inventra.data.repository.FakeAuthRepository
import com.inventraoficial.inventra.data.repository.FakeProductRepository
import com.inventraoficial.inventra.data.repository.FakeProductSupplierRepository
import com.inventraoficial.inventra.data.repository.FakeSupplierRepository
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProductEditViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private val ketchupId = 42
    private lateinit var productRepository: FakeProductRepository
    private val supplierRepository =
        FakeSupplierRepository(
            suppliers =
                listOf(
                    FakeSupplierRepository.supplier(1, "Atacadão"),
                    FakeSupplierRepository.supplier(2, "Seara Alimentos"),
                ),
        )
    private val productSupplierRepository = FakeProductSupplierRepository()
    private val searaLink = FakeProductSupplierRepository.link(2, "Seara", referencePrice = 9.9)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        productRepository =
            FakeProductRepository(
                products =
                    listOf(
                        FakeProductRepository
                            .product(ketchupId, "Ketchup", brand = "Heinz", categoryName = "Mercearia")
                            .copy(barcode = "7896102000122"),
                    ),
            )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(accessType: AccessType = AccessType.SUPERVISOR): ProductEditViewModel {
        val user = SessionUser("1", "Ana", "ana@inventra.com", accessType, kitchenId = 7)
        return ProductEditViewModel(
            productId = ketchupId,
            productRepository = productRepository,
            authRepository = FakeAuthRepository(initialUser = user),
            supplierRepository = supplierRepository,
            productSupplierRepository = productSupplierRepository,
        )
    }

    @Test
    fun `carrega o produto com o estoque minimo da cozinha`() =
        runTest(testDispatcher) {
            productRepository.savedKitchenParameters =
                KitchenParametersResponse(KitchenParametersKitchen(7), minStock = 2.5)
            val viewModel = createViewModel()
            advanceUntilIdle()

            val form = viewModel.uiState.value.form
            assertFalse(viewModel.uiState.value.isLoading)
            assertEquals("Ketchup", form.name)
            assertEquals("Heinz", form.brand)
            assertEquals("7896102000122", form.barcode)
            assertEquals(1, form.unitId)
            assertEquals("2,5", form.minStock)
            assertTrue(form.units.isNotEmpty())
        }

    @Test
    fun `quem nao e supervisor nao edita`() =
        runTest(testDispatcher) {
            val viewModel = createViewModel(accessType = AccessType.ESTOQUISTA)
            advanceUntilIdle()

            assertEquals("Só o supervisor da cozinha pode editar produtos.", viewModel.uiState.value.errorMessage)
            assertTrue(productRepository.calls.isEmpty())
        }

    @Test
    fun `falha ao buscar o produto mostra o erro`() =
        runTest(testDispatcher) {
            productRepository.error = Exception("Produto não encontrado.")
            val viewModel = createViewModel()
            advanceUntilIdle()

            assertEquals("Produto não encontrado.", viewModel.uiState.value.errorMessage)
        }

    @Test
    fun `salvar envia o produto editado e volta`() =
        runTest(testDispatcher) {
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.formActions.onNameChange(" Ketchup Tradicional ")
            viewModel.formActions.onBrandChange("")
            viewModel.formActions.onUnitSelect(6)
            viewModel.onSaveClick()
            advanceUntilIdle()

            assertEquals(
                listOf(ketchupId to UpdateProductRequest("Ketchup Tradicional", null, 1, 6, "7896102000122")),
                productRepository.updateRequests,
            )
            assertTrue(productRepository.kitchenParametersCalls.isEmpty())
            assertTrue(viewModel.uiState.value.isSaved)
        }

    @Test
    fun `mudar o estoque minimo grava na cozinha sem perder o resto`() =
        runTest(testDispatcher) {
            productRepository.savedKitchenParameters =
                KitchenParametersResponse(KitchenParametersKitchen(7), minStock = 2.0, maxStock = 20.0)
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.formActions.onMinStockChange("5")
            viewModel.onSaveClick()
            advanceUntilIdle()

            assertEquals(
                listOf(ketchupId to KitchenParametersRequest(kitchenId = 7, minStock = 5.0, maxStock = 20.0)),
                productRepository.kitchenParametersCalls,
            )
            assertTrue(viewModel.uiState.value.isSaved)
        }

    @Test
    fun `carrega o fornecedor vinculado com o preco`() =
        runTest(testDispatcher) {
            productSupplierRepository.links = listOf(searaLink)
            val viewModel = createViewModel()
            advanceUntilIdle()

            val form = viewModel.uiState.value.form
            assertEquals(2, form.supplierId)
            assertEquals("Seara Alimentos", form.supplierName)
            assertEquals("9,90", form.referencePrice)
        }

    @Test
    fun `sem mexer no fornecedor nao grava o vinculo de novo`() =
        runTest(testDispatcher) {
            productSupplierRepository.links = listOf(searaLink)
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.formActions.onNameChange("Ketchup Heinz")
            viewModel.onSaveClick()
            advanceUntilIdle()

            assertTrue(productSupplierRepository.linkCalls.isEmpty())
            assertTrue(viewModel.uiState.value.isSaved)
        }

    @Test
    fun `mudar o preco grava o vinculo sem perder o codigo do fornecedor`() =
        runTest(testDispatcher) {
            productSupplierRepository.links =
                listOf(FakeProductSupplierRepository.link(2, "Seara", referencePrice = 9.9, supplierCode = "KT-01"))
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.formActions.onReferencePriceChange("11,50")
            viewModel.onSaveClick()
            advanceUntilIdle()

            assertEquals(
                listOf(ketchupId to LinkSupplierRequest(2, supplierCode = "KT-01", referencePrice = 11.5)),
                productSupplierRepository.linkCalls,
            )
        }

    @Test
    fun `escolher fornecedor novo grava o vinculo`() =
        runTest(testDispatcher) {
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.formActions.onSupplierSelect(1)
            viewModel.onSaveClick()
            advanceUntilIdle()

            assertEquals(listOf(ketchupId to LinkSupplierRequest(1)), productSupplierRepository.linkCalls)
        }

    @Test
    fun `falha ao gravar o fornecedor mostra o erro e nao volta`() =
        runTest(testDispatcher) {
            productSupplierRepository.linkResult = Result.failure(Exception("Fornecedor não encontrado."))
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.formActions.onSupplierSelect(1)
            viewModel.onSaveClick()
            advanceUntilIdle()

            assertEquals("Fornecedor não encontrado.", viewModel.uiState.value.submitError)
            assertFalse(viewModel.uiState.value.isSaved)
        }

    @Test
    fun `salvar exige nome`() =
        runTest(testDispatcher) {
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.formActions.onNameChange("  ")
            viewModel.onSaveClick()

            assertEquals("Informe o nome do produto", viewModel.uiState.value.submitError)
            assertTrue(productRepository.updateRequests.isEmpty())
        }

    @Test
    fun `falha ao salvar mostra a mensagem e nao volta`() =
        runTest(testDispatcher) {
            productRepository.updateResult = Result.failure(Exception("Já existe um produto com esse código."))
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.onSaveClick()
            advanceUntilIdle()

            assertEquals("Já existe um produto com esse código.", viewModel.uiState.value.submitError)
            assertFalse(viewModel.uiState.value.isSaved)
        }
}
