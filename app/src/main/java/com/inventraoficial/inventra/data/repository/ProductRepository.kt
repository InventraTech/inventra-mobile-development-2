package com.inventraoficial.inventra.data.repository

import com.inventraoficial.inventra.data.remote.api.ProductApi
import com.inventraoficial.inventra.data.remote.apiCall
import com.inventraoficial.inventra.data.remote.dto.product.ProductResponse
import kotlinx.serialization.json.Json

interface ProductRepository {
    suspend fun product(id: Int): Result<ProductResponse>

    suspend fun search(name: String): Result<List<ProductResponse>>
}

class NetworkProductRepository(
    private val productApi: ProductApi,
    private val json: Json,
) : ProductRepository {
    override suspend fun product(id: Int): Result<ProductResponse> = apiCall(json) { productApi.find(id) }

    override suspend fun search(name: String): Result<List<ProductResponse>> =
        apiCall(json) { productApi.search(name = name.trim(), size = SEARCH_SIZE).content }

    private companion object {
        const val SEARCH_SIZE = 20
    }
}
