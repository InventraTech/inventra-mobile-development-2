package com.inventraoficial.inventra.feature.suppliers.detail.ui

import com.inventraoficial.inventra.data.local.SessionUser
import com.inventraoficial.inventra.data.remote.dto.auth.AccessType
import com.inventraoficial.inventra.data.remote.dto.stock.SupplierSummary
import com.inventraoficial.inventra.data.repository.FakeAuthRepository
import com.inventraoficial.inventra.data.repository.FakeStockRepository
import com.inventraoficial.inventra.data.repository.FakeStockRepository.Companion.batch
import com.inventraoficial.inventra.data.repository.FakeSupplierPhotoRepository
import com.inventraoficial.inventra.data.repository.FakeSupplierRepository
import com.inventraoficial.inventra.data.repository.FakeSupplierRepository.Companion.supplier
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

@OptIn(ExperimentalCoroutinesApi::class)
class SupplierDetailViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private val user =
        SessionUser(
            id = "sup-1",
            name = "Jones",
            email = "jones@inventra.com",
            accessType = AccessType.SUPERVISOR,
            kitchenId = 7,
            kitchenName = "Sabor Filial Sul",
        )
    private val seara = SupplierSummary(id = 1, legalName = "Seara Alimentos")
    private val italac = SupplierSummary(id = 2, legalName = "Italac")
    private lateinit var supplierRepository: FakeSupplierRepository
    private lateinit var stockRepository: FakeStockRepository
    private lateinit var photoRepository: FakeSupplierPhotoRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        photoRepository = FakeSupplierPhotoRepository()
        supplierRepository =
            FakeSupplierRepository(
                suppliers =
                    listOf(
                        supplier(
                            id = 1,
                            name = "Seara Alimentos",
                            rating = 5,
                            email = "contato@seara.com",
                            whatsapp = "11999990000",
                        ),
                        supplier(2, "Italac"),
                    ),
            )
        stockRepository =
            FakeStockRepository(
                batches =
                    listOf(
                        delivery(10, "Peito de Frango", productId = 1, entryDate = "2026-10-01", quantity = 4.0)
                            .copy(unitPrice = 18.9),
                        delivery(11, "Peito de Frango", productId = 1, entryDate = "2026-10-08", quantity = 6.0)
                            .copy(unitPrice = 18.5),
                        delivery(12, "Carne Moída", productId = 2, entryDate = "2026-10-05", quantity = 3.0),
                        batch(13, "Leite Integral", "2026-10-12", productId = 3).copy(supplier = italac),
                    ),
            )
    }

    private fun delivery(
        id: Int,
        productName: String,
        productId: Int,
        entryDate: String,
        quantity: Double,
    ) = batch(id, productName, "2026-12-31", currentQuantity = quantity, productId = productId)
        .copy(supplier = seara, entryDate = entryDate, initialQuantity = quantity)

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(
        supplierId: Int = 1,
        sessionUser: SessionUser? = user,
    ) = SupplierDetailViewModel(
        supplierId = supplierId,
        supplierRepository = supplierRepository,
        authRepository = FakeAuthRepository(initialUser = sessionUser),
        stockRepository = stockRepository,
        supplierPhotoRepository = photoRepository,
    )

    @Test
    fun `mostra os dados do fornecedor com o cnpj no selo`() =
        runTest(testDispatcher) {
            val viewModel = createViewModel()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertFalse(state.isLoading)
            assertEquals("Seara Alimentos", state.name)
            assertEquals(5, state.rating)
            assertEquals("00.000.000/0001-01", state.badgeText)
            assertEquals("11999990000", state.phone)
            assertEquals("contato@seara.com", state.email)
        }

    @Test
    fun `fornecedor sem contato fica com telefone e email nulos`() =
        runTest(testDispatcher) {
            val viewModel = createViewModel(supplierId = 2)
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertNull(state.phone)
            assertNull(state.email)
            assertEquals(0, state.rating)
        }

    @Test
    fun `historico mostra so as entregas desse fornecedor da mais recente para a mais antiga`() =
        runTest(testDispatcher) {
            val viewModel = createViewModel()
            advanceUntilIdle()

            val history = viewModel.uiState.value.deliveryHistory
            assertEquals(listOf("08/10/2026", "05/10/2026", "01/10/2026"), history.map { it.date })
            assertEquals(listOf("Peito de Frango", "Carne Moída", "Peito de Frango"), history.map { it.productName })
            assertEquals("Qtd. 6", history.first().quantityLabel)
        }

    @Test
    fun `produtos fornecidos sao distintos e mostram o ultimo preco`() =
        runTest(testDispatcher) {
            val viewModel = createViewModel()
            advanceUntilIdle()

            val products = viewModel.uiState.value.products
            assertEquals(listOf("Carne Moída", "Peito de Frango"), products.map { it.name })
            assertEquals("Sem preço informado", products[0].infoText)
            assertEquals("Último preço R$ 18,50", products[1].infoText)
        }

    @Test
    fun `usuario sem cozinha mostra o fornecedor sem buscar lotes`() =
        runTest(testDispatcher) {
            val viewModel = createViewModel(sessionUser = user.copy(kitchenId = null, kitchenName = null))
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertEquals("Seara Alimentos", state.name)
            assertTrue(state.deliveryHistory.isEmpty())
            assertTrue(stockRepository.batchesCalls.isEmpty())
        }

    @Test
    fun `falha na api mostra o erro`() =
        runTest(testDispatcher) {
            supplierRepository.error = IOException("Sem conexão")
            val viewModel = createViewModel()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertFalse(state.isLoading)
            assertEquals("Sem conexão", state.errorMessage)
        }

    @Test
    fun `sem acesso aos lotes mostra o fornecedor sem produtos e historico`() =
        runTest(testDispatcher) {
            stockRepository.batchesError = IOException("Acesso negado")
            val viewModel = createViewModel()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertNull(state.errorMessage)
            assertEquals("Seara Alimentos", state.name)
            assertFalse(state.showsDeliveries)
            assertTrue(state.products.isEmpty())
            assertTrue(state.deliveryHistory.isEmpty())
        }

    @Test
    fun `com acesso aos lotes mostra produtos e historico`() =
        runTest(testDispatcher) {
            val viewModel = createViewModel()
            advanceUntilIdle()

            assertTrue(viewModel.uiState.value.showsDeliveries)
        }

    @Test
    fun `tentar novamente limpa o erro e recarrega`() =
        runTest(testDispatcher) {
            supplierRepository.error = IOException("Sem conexão")
            val viewModel = createViewModel()
            advanceUntilIdle()

            supplierRepository.error = null
            viewModel.onRetryClick()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertNull(state.errorMessage)
            assertEquals(3, state.deliveryHistory.size)
        }

    @Test
    fun `supervisor pode trocar a foto e abre as opcoes ao tocar`() =
        runTest(testDispatcher) {
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.onPhotoClick()

            assertTrue(viewModel.uiState.value.photo.canEdit)
            assertTrue(viewModel.uiState.value.photo.isSheetVisible)
        }

    @Test
    fun `quem nao e supervisor nao abre as opcoes de foto`() =
        runTest(testDispatcher) {
            val viewModel = createViewModel(sessionUser = user.copy(accessType = AccessType.COMPRADOR))
            advanceUntilIdle()

            viewModel.onPhotoClick()

            assertFalse(viewModel.uiState.value.photo.canEdit)
            assertFalse(viewModel.uiState.value.photo.isSheetVisible)
        }

    @Test
    fun `foto escolhida e enviada e fica com a url da api`() =
        runTest(testDispatcher) {
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.onPhotoPicked("content://galeria/9")
            assertTrue(viewModel.uiState.value.photo.isUploading)
            advanceUntilIdle()

            val photo = viewModel.uiState.value.photo
            assertEquals(listOf(1 to "content://galeria/9"), photoRepository.uploads)
            assertEquals(FakeSupplierPhotoRepository.uploadedUrl(1), photo.url)
            assertFalse(photo.isUploading)
        }

    @Test
    fun `falha no envio volta a foto anterior e mostra o erro`() =
        runTest(testDispatcher) {
            photoRepository.error = IOException("Sem conexão")
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.onPhotoPicked("content://galeria/9")
            advanceUntilIdle()

            val photo = viewModel.uiState.value.photo
            assertNull(photo.url)
            assertEquals("Sem conexão", photo.errorMessage)
        }

    @Test
    fun `remover foto chama a api e limpa a url`() =
        runTest(testDispatcher) {
            val viewModel = createViewModel()
            advanceUntilIdle()
            viewModel.onPhotoPicked("content://galeria/9")
            advanceUntilIdle()

            viewModel.onRemovePhotoClick()
            advanceUntilIdle()

            assertEquals(listOf(1), photoRepository.removals)
            assertNull(viewModel.uiState.value.photo.url)
        }

    @Test
    fun `camera negada fecha as opcoes e explica`() =
        runTest(testDispatcher) {
            val viewModel = createViewModel()
            advanceUntilIdle()
            viewModel.onPhotoClick()

            viewModel.onCameraDenied()

            val photo = viewModel.uiState.value.photo
            assertFalse(photo.isSheetVisible)
            assertTrue(photo.errorMessage.orEmpty().contains("câmera"))
        }
}
