package com.inventraoficial.inventra.feature.scan.ui

import com.inventraoficial.inventra.data.local.SessionUser
import com.inventraoficial.inventra.data.remote.dto.auth.AccessType
import com.inventraoficial.inventra.data.remote.dto.product.BarcodeKitchenStock
import com.inventraoficial.inventra.data.remote.dto.product.BarcodeLookupResponse
import com.inventraoficial.inventra.data.remote.dto.product.BarcodeSource
import com.inventraoficial.inventra.data.remote.dto.product.OpenFoodFactsSuggestion
import com.inventraoficial.inventra.data.repository.FakeAlertRepository
import com.inventraoficial.inventra.data.repository.FakeAuthRepository
import com.inventraoficial.inventra.data.repository.FakeProductRepository
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
class ScanViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private val barcode = "7891234567890"
    private lateinit var productRepository: FakeProductRepository
    private lateinit var alertRepository: FakeAlertRepository

    private val leite = FakeProductRepository.product(5013, "Leite Integral", "L", "Italac", "Laticínios")
    private val catalog =
        BarcodeLookupResponse(
            source = BarcodeSource.CATALOG,
            barcode = barcode,
            product = leite,
            kitchenStock = BarcodeKitchenStock(12.5, activeBatches = 3, nextExpirationDate = "2026-10-20"),
        )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        productRepository = FakeProductRepository()
        alertRepository = FakeAlertRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(
        accessType: AccessType = AccessType.ESTOQUISTA,
        kitchenId: Int? = 7,
    ): ScanViewModel {
        val user = SessionUser("1", "Mauro", "mauro@inventra.com", accessType, kitchenId)
        return ScanViewModel(productRepository, alertRepository, FakeAuthRepository(initialUser = user))
    }

    @Test
    fun `permissao negada mostra o aviso`() {
        val viewModel = createViewModel()

        viewModel.onPermissionResult(false)

        assertFalse(viewModel.uiState.value.hasCameraPermission)
        assertEquals("Permita o acesso à câmera para escanear", viewModel.uiState.value.permissionMessage)
    }

    @Test
    fun `lanterna liga e desliga`() {
        val viewModel = createViewModel()

        viewModel.onFlashClick()
        assertTrue(viewModel.uiState.value.isFlashOn)

        viewModel.onFlashClick()
        assertFalse(viewModel.uiState.value.isFlashOn)
    }

    @Test
    fun `estoquista pode dar baixa e avisar, supervisor dar baixa e cadastrar e comprador nenhum`() =
        runTest(testDispatcher) {
            val estoquista = createViewModel(AccessType.ESTOQUISTA)
            val supervisor = createViewModel(AccessType.SUPERVISOR)
            val comprador = createViewModel(AccessType.COMPRADOR)
            advanceUntilIdle()

            assertTrue(estoquista.uiState.value.canManageStock && estoquista.uiState.value.canReport)
            assertFalse(estoquista.uiState.value.canCreateProduct)
            assertTrue(supervisor.uiState.value.canManageStock && supervisor.uiState.value.canCreateProduct)
            assertFalse(supervisor.uiState.value.canReport)
            assertFalse(comprador.uiState.value.canManageStock || comprador.uiState.value.canReport)
            assertFalse(comprador.uiState.value.canCreateProduct)
        }

    @Test
    fun `produto do catalogo mostra nome, saldo e validade`() =
        runTest(testDispatcher) {
            productRepository.barcodeResult = Result.success(catalog)
            val viewModel = createViewModel()

            viewModel.onBarcodeDetected(barcode)
            assertEquals(barcode, viewModel.uiState.value.lookingUpBarcode)
            advanceUntilIdle()

            val result = viewModel.uiState.value.result as ScanResult.Found
            assertNull(viewModel.uiState.value.lookingUpBarcode)
            assertEquals(5013, result.productId)
            assertEquals("Leite Integral", result.name)
            assertEquals("Italac · Laticínios", result.details)
            assertEquals("12,5 L em 3 lotes", result.stock)
            assertEquals("20/10/2026", result.nextExpiration)
            assertTrue(result.hasStock)
            assertEquals(listOf(barcode to 7), productRepository.barcodeCalls)
        }

    @Test
    fun `produto sem saldo na cozinha nao deixa dar baixa`() =
        runTest(testDispatcher) {
            productRepository.barcodeResult = Result.success(catalog.copy(kitchenStock = null))
            val viewModel = createViewModel()

            viewModel.onBarcodeDetected(barcode)
            advanceUntilIdle()

            val result = viewModel.uiState.value.result as ScanResult.Found
            assertEquals("Sem saldo na cozinha", result.stock)
            assertFalse(result.hasStock)
        }

    @Test
    fun `codigo so do Open Food Facts vira nao cadastrado com sugestao`() =
        runTest(testDispatcher) {
            val suggestion =
                OpenFoodFactsSuggestion(
                    name = "Coca-Cola",
                    brand = "Coca-Cola",
                    imageUrl = "https://images.openfoodfacts.org/coca.jpg",
                    quantity = "2 L",
                )
            productRepository.barcodeResult =
                Result.success(BarcodeLookupResponse(BarcodeSource.OPEN_FOOD_FACTS, barcode, suggestion = suggestion))
            val viewModel = createViewModel()

            viewModel.onBarcodeDetected(barcode)
            advanceUntilIdle()

            val result = viewModel.uiState.value.result as ScanResult.NotRegistered
            assertEquals("Coca-Cola · Coca-Cola · 2 L", result.suggestion)
            assertEquals("https://images.openfoodfacts.org/coca.jpg", result.photoUrl)
        }

    @Test
    fun `codigo desconhecido vira nao cadastrado sem sugestao`() =
        runTest(testDispatcher) {
            val viewModel = createViewModel()

            viewModel.onBarcodeDetected(barcode)
            advanceUntilIdle()

            val result = viewModel.uiState.value.result as ScanResult.NotRegistered
            assertEquals(barcode, result.barcode)
            assertNull(result.suggestion)
            assertNull(result.photoUrl)
        }

    @Test
    fun `com resultado na tela novas leituras sao ignoradas ate escanear outro`() =
        runTest(testDispatcher) {
            productRepository.barcodeResult = Result.success(catalog)
            val viewModel = createViewModel()
            viewModel.onBarcodeDetected(barcode)
            advanceUntilIdle()

            viewModel.onBarcodeDetected("7894900011517")
            advanceUntilIdle()
            assertEquals(1, productRepository.barcodeCalls.size)

            viewModel.onScanAgainClick()
            viewModel.onBarcodeDetected("7894900011517")
            advanceUntilIdle()
            assertEquals(2, productRepository.barcodeCalls.size)
        }

    @Test
    fun `falha na consulta mostra o erro e pausa a leitura`() =
        runTest(testDispatcher) {
            productRepository.barcodeResult = Result.failure(Exception("Sem conexão"))
            val viewModel = createViewModel()

            viewModel.onBarcodeDetected(barcode)
            advanceUntilIdle()
            viewModel.onBarcodeDetected(barcode)
            advanceUntilIdle()

            assertEquals("Sem conexão", viewModel.uiState.value.errorMessage)
            assertEquals(1, productRepository.barcodeCalls.size)
        }

    @Test
    fun `codigo digitado busca do mesmo jeito`() =
        runTest(testDispatcher) {
            productRepository.barcodeResult = Result.success(catalog)
            val viewModel = createViewModel()

            viewModel.onManualEntryClick()
            viewModel.onManualCodeChange("789abc")
            viewModel.onManualCodeChange(barcode)
            viewModel.onManualCodeSubmit()
            advanceUntilIdle()

            assertFalse(viewModel.uiState.value.isManualEntryOpen)
            assertEquals(listOf(barcode to 7), productRepository.barcodeCalls)
            assertTrue(viewModel.uiState.value.result is ScanResult.Found)
        }

    @Test
    fun `avisar o supervisor manda o codigo da cozinha`() =
        runTest(testDispatcher) {
            val viewModel = createViewModel()
            viewModel.onBarcodeDetected(barcode)
            advanceUntilIdle()

            viewModel.onReportClick()
            assertTrue((viewModel.uiState.value.result as ScanResult.NotRegistered).isReporting)
            advanceUntilIdle()

            val result = viewModel.uiState.value.result as ScanResult.NotRegistered
            assertTrue(result.isReported)
            assertFalse(result.isReporting)
            assertEquals(listOf(7 to barcode), alertRepository.reported)
        }

    @Test
    fun `falha ao avisar mostra o erro e deixa tentar de novo`() =
        runTest(testDispatcher) {
            alertRepository.reportError = Exception("Sem conexão")
            val viewModel = createViewModel()
            viewModel.onBarcodeDetected(barcode)
            advanceUntilIdle()

            viewModel.onReportClick()
            advanceUntilIdle()

            val result = viewModel.uiState.value.result as ScanResult.NotRegistered
            assertEquals("Sem conexão", result.reportError)
            assertFalse(result.isReported)
        }

    @Test
    fun `sem cozinha nao consegue avisar`() =
        runTest(testDispatcher) {
            val viewModel = createViewModel(kitchenId = null)
            viewModel.onBarcodeDetected(barcode)
            advanceUntilIdle()

            viewModel.onReportClick()
            advanceUntilIdle()

            val result = viewModel.uiState.value.result as ScanResult.NotRegistered
            assertEquals("Você ainda não faz parte de uma cozinha.", result.reportError)
            assertTrue(alertRepository.reported.isEmpty())
        }
}
