package com.inventraoficial.inventra.data.repository

import com.inventraoficial.inventra.data.remote.api.ProductApi
import com.inventraoficial.inventra.data.remote.dto.product.BarcodeLookupResponse
import com.inventraoficial.inventra.data.remote.dto.product.BarcodeSource
import com.inventraoficial.inventra.data.remote.dto.product.PageProductResponse
import com.inventraoficial.inventra.data.remote.dto.product.ProductResponse
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
}
