package com.inventraoficial.inventra.feature.stock.productcreate.ui

import com.inventraoficial.inventra.data.local.SessionUser
import com.inventraoficial.inventra.data.remote.dto.auth.AccessType
import com.inventraoficial.inventra.data.remote.dto.product.BarcodeLookupResponse
import com.inventraoficial.inventra.data.remote.dto.product.BarcodeSource
import com.inventraoficial.inventra.data.remote.dto.product.CreateProductRequest
import com.inventraoficial.inventra.data.remote.dto.product.KitchenParametersRequest
import com.inventraoficial.inventra.data.remote.dto.product.LinkSupplierRequest
import com.inventraoficial.inventra.data.remote.dto.product.OpenFoodFactsSuggestion
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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProductCreateViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private val barcode = "7894900011517"
    private lateinit var productRepository: FakeProductRepository
    private lateinit var supplierRepository: FakeSupplierRepository
    private lateinit var productSupplierRepository: FakeProductSupplierRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        productRepository = FakeProductRepository()
        supplierRepository =
            FakeSupplierRepository(
                suppliers =
                    listOf(
                        FakeSupplierRepository.supplier(2, "Seara Alimentos"),
                        FakeSupplierRepository.supplier(1, "Atacadão"),
                    ),
            )
        productSupplierRepository = FakeProductSupplierRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(
        barcode: String? = this.barcode,
        accessType: AccessType = AccessType.SUPERVISOR,
    ): ProductCreateViewModel {
        val user = SessionUser("1", "Ana", "ana@inventra.com", accessType, kitchenId = 7)
        return ProductCreateViewModel(
            barcode = barcode,
            productRepository = productRepository,
            authRepository = FakeAuthRepository(initialUser = user),
            supplierRepository = supplierRepository,
            productSupplierRepository = productSupplierRepository,
        )
    }

    private fun suggestion(
        name: String? = "Coca-Cola",
        brand: String? = "Coca-Cola",
        quantity: String? = "2 L",
    ) = BarcodeLookupResponse(
        source = BarcodeSource.OPEN_FOOD_FACTS,
        barcode = barcode,
        suggestion = OpenFoodFactsSuggestion(name = name, brand = brand, quantity = quantity),
    )

    @Test
    fun `comeca carregando com o codigo escaneado`() {
        val viewModel = createViewModel()

        assertTrue(viewModel.uiState.value.isLoading)
        assertEquals(barcode, viewModel.uiState.value.form.barcode)
    }

    @Test
    fun `quem nao e supervisor nao cadastra`() =
        runTest(testDispatcher) {
            val viewModel = createViewModel(accessType = AccessType.ESTOQUISTA)
            advanceUntilIdle()

            assertEquals("Só o supervisor da cozinha pode cadastrar produtos.", viewModel.uiState.value.errorMessage)
            assertTrue(productRepository.barcodeCalls.isEmpty())
        }

    @Test
    fun `unidades simples vem antes das embalagens e categorias em ordem de nome`() =
        runTest(testDispatcher) {
            productRepository.unitsResult = Result.success(FakeProductRepository.defaultUnits.reversed())
            val viewModel = createViewModel()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertFalse(state.isLoading)
            assertEquals(listOf(6, 5, 4, 2, 1, 10), state.form.units.map { it.id })
            assertEquals("Litro", state.form.units[2].title)
            assertEquals("L", state.form.units[2].subtitle)
            assertEquals(listOf("Laticínios", "Mercearia"), state.form.categories.map { it.title })
        }

    @Test
    fun `codigo do Open Food Facts preenche nome, marca e unidade`() =
        runTest(testDispatcher) {
            productRepository.barcodeResult = Result.success(suggestion())
            val viewModel = createViewModel()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertTrue(state.isPrefilled)
            assertEquals("Coca-Cola", state.form.name)
            assertEquals("Coca-Cola", state.form.brand)
            assertEquals(4, state.form.unitId)
            assertEquals("Litro (L)", state.form.unitLabel)
            assertEquals(listOf(barcode to 7), productRepository.barcodeCalls)
        }

    @Test
    fun `adivinha a unidade pela ultima medida da quantidade`() =
        runTest(testDispatcher) {
            productRepository.barcodeResult = Result.success(suggestion(quantity = "12 x 350ml"))
            val viewModel = createViewModel()
            advanceUntilIdle()

            assertEquals(5, viewModel.uiState.value.form.unitId)
        }

    @Test
    fun `quantidade sem medida conhecida deixa a unidade em branco`() =
        runTest(testDispatcher) {
            productRepository.barcodeResult = Result.success(suggestion(quantity = "6 unidades"))
            val viewModel = createViewModel()
            advanceUntilIdle()

            assertNull(viewModel.uiState.value.form.unitId)
        }

    @Test
    fun `codigo que ja esta no catalogo mostra o produto e nao deixa cadastrar`() =
        runTest(testDispatcher) {
            val leite = FakeProductRepository.product(13, "Leite Integral")
            productRepository.barcodeResult =
                Result.success(BarcodeLookupResponse(BarcodeSource.CATALOG, barcode, product = leite))
            val viewModel = createViewModel()
            advanceUntilIdle()

            assertEquals(SavedProduct(13, "Leite Integral"), viewModel.uiState.value.existingProduct)
        }

    @Test
    fun `sem codigo nao consulta e falha na consulta nao atrapalha`() =
        runTest(testDispatcher) {
            val semCodigo = createViewModel(barcode = null)
            advanceUntilIdle()
            assertTrue(productRepository.barcodeCalls.isEmpty())
            assertFalse(semCodigo.uiState.value.isLoading)

            productRepository.barcodeResult = Result.failure(Exception("Sem conexão"))
            val comFalha = createViewModel()
            advanceUntilIdle()
            assertNull(comFalha.uiState.value.errorMessage)
            assertEquals("", comFalha.uiState.value.form.name)
        }

    @Test
    fun `sem unidades mostra o erro e tentar de novo carrega`() =
        runTest(testDispatcher) {
            productRepository.unitsResult = Result.failure(Exception("Sem conexão"))
            val viewModel = createViewModel()
            advanceUntilIdle()
            assertEquals("Sem conexão", viewModel.uiState.value.errorMessage)

            productRepository.unitsResult = Result.success(FakeProductRepository.defaultUnits)
            viewModel.onRetryClick()
            advanceUntilIdle()

            assertNull(viewModel.uiState.value.errorMessage)
            assertEquals(6, viewModel.uiState.value.form.units.size)
        }

    @Test
    fun `sem categorias o formulario segue sem elas`() =
        runTest(testDispatcher) {
            productRepository.categoriesResult = Result.failure(Exception("Sem conexão"))
            val viewModel = createViewModel()
            advanceUntilIdle()

            assertNull(viewModel.uiState.value.errorMessage)
            assertTrue(
                viewModel.uiState.value.form.categories
                    .isEmpty(),
            )
        }

    @Test
    fun `codigo de barras so aceita numeros`() =
        runTest(testDispatcher) {
            val viewModel = createViewModel(barcode = null)
            advanceUntilIdle()

            viewModel.formActions.onBarcodeChange("789a")
            assertEquals("", viewModel.uiState.value.form.barcode)
            viewModel.formActions.onBarcodeChange("789")
            assertEquals("789", viewModel.uiState.value.form.barcode)
        }

    @Test
    fun `salvar exige nome e unidade`() =
        runTest(testDispatcher) {
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.onSaveClick()
            assertEquals("Informe o nome do produto", viewModel.uiState.value.submitError)

            viewModel.formActions.onNameChange("Coca-Cola 2L")
            viewModel.onSaveClick()
            assertEquals("Escolha a unidade de medida", viewModel.uiState.value.submitError)
            assertTrue(productRepository.createRequests.isEmpty())
        }

    @Test
    fun `salvar envia o produto sem espacos e mostra o cadastrado`() =
        runTest(testDispatcher) {
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.formActions.onNameChange("  Coca-Cola 2L ")
            viewModel.formActions.onBrandChange("   ")
            viewModel.formActions.onUnitSelect(4)
            viewModel.formActions.onCategorySelect(5015)
            viewModel.onSaveClick()
            assertTrue(viewModel.uiState.value.isSubmitting)
            advanceUntilIdle()

            assertEquals(
                listOf(CreateProductRequest("Coca-Cola 2L", null, 5015, 4, barcode)),
                productRepository.createRequests,
            )
            assertEquals(SavedProduct(99, "Coca-Cola 2L"), viewModel.uiState.value.createdProduct)
            assertFalse(viewModel.uiState.value.isSubmitting)
        }

    @Test
    fun `produto cadastrado fica ligado a cozinha com o estoque minimo`() =
        runTest(testDispatcher) {
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.formActions.onNameChange("Ketchup")
            viewModel.formActions.onUnitSelect(6)
            viewModel.formActions.onMinStockChange("2,5")
            viewModel.onSaveClick()
            advanceUntilIdle()

            assertEquals(listOf(99 to KitchenParametersRequest(7, 2.5)), productRepository.kitchenParametersCalls)
            assertFalse(viewModel.uiState.value.kitchenLinkFailed)
        }

    @Test
    fun `sem estoque minimo liga a cozinha mesmo assim`() =
        runTest(testDispatcher) {
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.formActions.onNameChange("Ketchup")
            viewModel.formActions.onUnitSelect(6)
            viewModel.onSaveClick()
            advanceUntilIdle()

            assertEquals(listOf(99 to KitchenParametersRequest(7, null)), productRepository.kitchenParametersCalls)
        }

    @Test
    fun `estoque minimo so aceita numero com ate tres casas`() =
        runTest(testDispatcher) {
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.formActions.onMinStockChange("10a")
            viewModel.formActions.onMinStockChange("1,2345")
            assertEquals("", viewModel.uiState.value.form.minStock)

            viewModel.formActions.onMinStockChange("1,234")
            assertEquals("1,234", viewModel.uiState.value.form.minStock)
        }

    @Test
    fun `estoque minimo so com a virgula nao deixa salvar`() =
        runTest(testDispatcher) {
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.formActions.onNameChange("Ketchup")
            viewModel.formActions.onUnitSelect(6)
            viewModel.formActions.onMinStockChange(",")
            viewModel.onSaveClick()

            assertEquals("Informe o estoque mínimo só com números", viewModel.uiState.value.submitError)
            assertTrue(productRepository.createRequests.isEmpty())
        }

    @Test
    fun `fornecedores vem todos os ativos em ordem de nome`() =
        runTest(testDispatcher) {
            val viewModel = createViewModel()
            advanceUntilIdle()

            assertEquals(
                listOf("Atacadão", "Seara Alimentos"),
                viewModel.uiState.value.form.suppliers
                    .map { it.title },
            )
            assertEquals(1, supplierRepository.allCalls)
            assertTrue(supplierRepository.kitchenCalls.isEmpty())
        }

    @Test
    fun `produto com fornecedor grava o vinculo com o preco de referencia`() =
        runTest(testDispatcher) {
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.formActions.onNameChange("Ketchup")
            viewModel.formActions.onUnitSelect(6)
            viewModel.formActions.onSupplierSelect(2)
            viewModel.formActions.onReferencePriceChange("12,90")
            viewModel.onSaveClick()
            advanceUntilIdle()

            assertEquals(
                listOf(99 to LinkSupplierRequest(supplierId = 2, referencePrice = 12.9)),
                productSupplierRepository.linkCalls,
            )
            assertFalse(viewModel.uiState.value.supplierLinkFailed)
        }

    @Test
    fun `sem fornecedor nao grava vinculo`() =
        runTest(testDispatcher) {
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.formActions.onNameChange("Ketchup")
            viewModel.formActions.onUnitSelect(6)
            viewModel.onSaveClick()
            advanceUntilIdle()

            assertTrue(productSupplierRepository.linkCalls.isEmpty())
        }

    @Test
    fun `tirar o fornecedor limpa o preco e preco so aceita duas casas`() =
        runTest(testDispatcher) {
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.formActions.onSupplierSelect(2)
            viewModel.formActions.onReferencePriceChange("12,999")
            viewModel.formActions.onReferencePriceChange("12,99")
            assertEquals("12,99", viewModel.uiState.value.form.referencePrice)

            viewModel.formActions.onSupplierSelect(null)
            assertEquals("", viewModel.uiState.value.form.referencePrice)
        }

    @Test
    fun `falha ao ligar o fornecedor ainda mostra o produto cadastrado`() =
        runTest(testDispatcher) {
            productSupplierRepository.linkResult = Result.failure(Exception("Erro"))
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.formActions.onNameChange("Ketchup")
            viewModel.formActions.onUnitSelect(6)
            viewModel.formActions.onSupplierSelect(2)
            viewModel.onSaveClick()
            advanceUntilIdle()

            assertEquals(SavedProduct(99, "Ketchup"), viewModel.uiState.value.createdProduct)
            assertTrue(viewModel.uiState.value.supplierLinkFailed)
        }

    @Test
    fun `falha ao ligar a cozinha ainda mostra o produto cadastrado`() =
        runTest(testDispatcher) {
            productRepository.kitchenParametersResult = Result.failure(Exception("Erro"))
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.formActions.onNameChange("Ketchup")
            viewModel.formActions.onUnitSelect(6)
            viewModel.onSaveClick()
            advanceUntilIdle()

            assertEquals(SavedProduct(99, "Ketchup"), viewModel.uiState.value.createdProduct)
            assertTrue(viewModel.uiState.value.kitchenLinkFailed)
        }

    @Test
    fun `falha ao salvar mostra a mensagem do backend`() =
        runTest(testDispatcher) {
            productRepository.createResult =
                Result.failure(Exception("Já existe um produto com esse código de barras."))
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.formActions.onNameChange("Coca-Cola 2L")
            viewModel.formActions.onUnitSelect(4)
            viewModel.onSaveClick()
            advanceUntilIdle()

            assertEquals("Já existe um produto com esse código de barras.", viewModel.uiState.value.submitError)
            assertNull(viewModel.uiState.value.createdProduct)
        }
}
