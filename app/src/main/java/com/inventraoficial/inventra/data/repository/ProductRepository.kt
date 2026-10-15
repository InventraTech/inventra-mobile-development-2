package com.inventraoficial.inventra.data.repository

import com.inventraoficial.inventra.data.remote.api.ProductApi
import com.inventraoficial.inventra.data.remote.apiCall
import com.inventraoficial.inventra.data.remote.dto.product.ProductResponse
import kotlinx.serialization.json.Json

interface ProductRepository {
    suspend fun product(id: Int): Result<ProductResponse>
}

class NetworkProductRepository(
    private val productApi: ProductApi,
    private val json: Json,
) : ProductRepository {
    override suspend fun product(id: Int): Result<ProductResponse> = apiCall(json) { productApi.find(id) }
}
