package com.inventraoficial.inventra.data.repository

import com.inventraoficial.inventra.data.remote.api.ProductApi
import com.inventraoficial.inventra.data.remote.dto.product.BarcodeLookupResponse
import com.inventraoficial.inventra.data.remote.dto.product.BarcodeSource
import com.inventraoficial.inventra.data.remote.dto.product.CategoryResponse
import com.inventraoficial.inventra.data.remote.dto.product.CreateProductRequest
import com.inventraoficial.inventra.data.remote.dto.product.KitchenParametersRequest
import com.inventraoficial.inventra.data.remote.dto.product.PageProductResponse
import com.inventraoficial.inventra.data.remote.dto.product.ProductResponse
import com.inventraoficial.inventra.data.remote.dto.product.UnitResponse
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

class NetworkProductRepositoryTest {
    private val json = Json { ignoreUnknownKeys = true }
    private val leite = FakeProductRepository.product(5013, "Leite Integral")
    private val lookup = BarcodeLookupResponse(BarcodeSource.CATALOG, "7891234567890", product = leite)

    private class FakeProductApi(
        private val lookup: BarcodeLookupResponse,
        private val barcodeError: Exception? = null,
    ) : ProductApi {
        var lastBarcode: Pair<String, Int?>? = null
            private set

        override suspend fun find(id: Int): ProductResponse = error("nao usado")

        override suspend fun search(
            name: String,
            size: Int,
            active: Boolean,
            sort: String,
        ): PageProductResponse = error("nao usado")

        override suspend fun findByBarcode(
            barcode: String,
            kitchenId: Int?,
        ): BarcodeLookupResponse {
            lastBarcode = barcode to kitchenId
            barcodeError?.let { throw it }
            return lookup
        }

        var created: CreateProductRequest? = null
            private set
        var unitsCalls = 0
            private set
        var unitsError: Exception? = null

        override suspend fun create(request: CreateProductRequest): ProductResponse {
            created = request
            return FakeProductRepository.product(77, request.name)
        }

        var kitchenParameters: Pair<Int, KitchenParametersRequest>? = null
            private set

        override suspend fun setKitchenParameters(
            id: Int,
            request: KitchenParametersRequest,
        ) {
            kitchenParameters = id to request
        }

        override suspend fun units(): List<UnitResponse> {
            unitsCalls++
            unitsError?.let { throw it }
            return FakeProductRepository.defaultUnits
        }

        override suspend fun categories(): List<CategoryResponse> = FakeProductRepository.defaultCategories
    }

    private fun httpError(code: Int) = HttpException(Response.error<Any>(code, "".toResponseBody()))

    @Test
    fun `findByBarcode envia o codigo sem espacos e a cozinha`() =
        runTest {
            val api = FakeProductApi(lookup)
            val repository = NetworkProductRepository(api, json)

            val result = repository.findByBarcode(" 7891234567890 ", kitchenId = 7)

            assertEquals(lookup, result.getOrNull())
            assertEquals("7891234567890" to 7, api.lastBarcode)
        }

    @Test
    fun `findByBarcode com 404 significa codigo desconhecido`() =
        runTest {
            val repository = NetworkProductRepository(FakeProductApi(lookup, httpError(404)), json)

            val result = repository.findByBarcode("0000000000000", kitchenId = 7)

            assertTrue(result.isSuccess)
            assertNull(result.getOrNull())
        }

    @Test
    fun `findByBarcode com outro erro devolve falha`() =
        runTest {
            val repository = NetworkProductRepository(FakeProductApi(lookup, httpError(500)), json)

            assertTrue(repository.findByBarcode("7891234567890", kitchenId = 7).isFailure)
        }

    @Test
    fun `create envia o pedido e devolve o produto criado`() =
        runTest {
            val api = FakeProductApi(lookup)
            val repository = NetworkProductRepository(api, json)
            val request = CreateProductRequest(name = "Coca-Cola 2L", unitId = 4, barcode = "7894900011517")

            val result = repository.create(request)

            assertEquals(77, result.getOrNull()?.id)
            assertEquals(request, api.created)
        }

    @Test
    fun `setKitchenParameters liga o produto a cozinha`() =
        runTest {
            val api = FakeProductApi(lookup)
            val repository = NetworkProductRepository(api, json)
            val request = KitchenParametersRequest(kitchenId = 7, minStock = 10.0)

            val result = repository.setKitchenParameters(77, request)

            assertTrue(result.isSuccess)
            assertEquals(77 to request, api.kitchenParameters)
        }

    @Test
    fun `units guarda a lista e nao busca de novo`() =
        runTest {
            val api = FakeProductApi(lookup)
            val repository = NetworkProductRepository(api, json)

            repository.units()
            val second = repository.units()

            assertEquals(FakeProductRepository.defaultUnits, second.getOrNull())
            assertEquals(1, api.unitsCalls)
        }

    @Test
    fun `units que falha tenta de novo na proxima vez`() =
        runTest {
            val api = FakeProductApi(lookup).apply { unitsError = httpError(500) }
            val repository = NetworkProductRepository(api, json)

            assertTrue(repository.units().isFailure)
            api.unitsError = null

            assertTrue(repository.units().isSuccess)
            assertEquals(2, api.unitsCalls)
        }
}
