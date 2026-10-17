package com.inventraoficial.inventra.data.repository

import com.inventraoficial.inventra.data.remote.api.ProductApi
import com.inventraoficial.inventra.data.remote.apiCall
import com.inventraoficial.inventra.data.remote.dto.product.BarcodeLookupResponse
import com.inventraoficial.inventra.data.remote.dto.product.ProductResponse
import kotlinx.serialization.json.Json
import retrofit2.HttpException

interface ProductRepository {
    suspend fun product(id: Int): Result<ProductResponse>

    suspend fun search(name: String): Result<List<ProductResponse>>

    // null quando nem o catálogo nem o Open Food Facts conhecem o código.
    suspend fun findByBarcode(
        barcode: String,
        kitchenId: Int?,
    ): Result<BarcodeLookupResponse?>
}

class NetworkProductRepository(
    private val productApi: ProductApi,
    private val json: Json,
) : ProductRepository {
    override suspend fun product(id: Int): Result<ProductResponse> = apiCall(json) { productApi.find(id) }

    override suspend fun search(name: String): Result<List<ProductResponse>> =
        apiCall(json) { productApi.search(name = name.trim(), size = SEARCH_SIZE).content }

    override suspend fun findByBarcode(
        barcode: String,
        kitchenId: Int?,
    ): Result<BarcodeLookupResponse?> =
        apiCall(json) {
            try {
                productApi.findByBarcode(barcode.trim(), kitchenId)
            } catch (e: HttpException) {
                if (e.code() == HTTP_NOT_FOUND) null else throw e
            }
        }

    private companion object {
        const val SEARCH_SIZE = 20
        const val HTTP_NOT_FOUND = 404
    }
}
