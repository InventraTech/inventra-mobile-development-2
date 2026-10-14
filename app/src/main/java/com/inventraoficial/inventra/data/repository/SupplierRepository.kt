package com.inventraoficial.inventra.data.repository

import com.inventraoficial.inventra.data.remote.api.SupplierApi
import com.inventraoficial.inventra.data.remote.apiCall
import com.inventraoficial.inventra.data.remote.dto.supplier.SupplierResponse
import kotlinx.serialization.json.Json

interface SupplierRepository {
    suspend fun suppliers(kitchenId: Int): Result<List<SupplierResponse>>

    suspend fun supplier(id: Int): Result<SupplierResponse>
}

class NetworkSupplierRepository(
    private val supplierApi: SupplierApi,
    private val json: Json,
) : SupplierRepository {
    override suspend fun suppliers(kitchenId: Int) = apiCall(json) { supplierApi.list(kitchenId) }

    override suspend fun supplier(id: Int): Result<SupplierResponse> = apiCall(json) { supplierApi.find(id) }
}
