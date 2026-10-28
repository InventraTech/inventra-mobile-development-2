package com.inventraoficial.inventra.data.repository

import com.inventraoficial.inventra.data.remote.api.ProductSupplierApi
import com.inventraoficial.inventra.data.remote.apiCall
import com.inventraoficial.inventra.data.remote.dto.product.LinkSupplierRequest
import com.inventraoficial.inventra.data.remote.dto.product.ProductSupplierResponse
import kotlinx.serialization.json.Json

interface ProductSupplierRepository {
    suspend fun link(
        productId: Int,
        request: LinkSupplierRequest,
    ): Result<Unit>

    suspend fun suppliersOf(productId: Int): Result<List<ProductSupplierResponse>>
}

class NetworkProductSupplierRepository(
    private val productSupplierApi: ProductSupplierApi,
    private val json: Json,
) : ProductSupplierRepository {
    override suspend fun link(
        productId: Int,
        request: LinkSupplierRequest,
    ): Result<Unit> = apiCall(json) { productSupplierApi.link(productId, request) }

    override suspend fun suppliersOf(productId: Int): Result<List<ProductSupplierResponse>> =
        apiCall(json) { productSupplierApi.list(productId) }
}
