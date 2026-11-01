package com.inventraoficial.inventra.data.repository

import com.inventraoficial.inventra.data.remote.dto.supplier.CreateSupplierRequest
import com.inventraoficial.inventra.data.remote.dto.supplier.SupplierResponse

class FakeSupplierRepository(
    var suppliers: List<SupplierResponse> = emptyList(),
) : SupplierRepository {
    var error: Exception? = null
    var requestedIds = mutableListOf<Int>()
        private set
    var kitchenCalls = mutableListOf<Int>()
        private set

    override suspend fun suppliers(kitchenId: Int): Result<List<SupplierResponse>> {
        kitchenCalls += kitchenId
        error?.let { return Result.failure(it) }
        return Result.success(suppliers)
    }

    var allCalls = 0
        private set
    var otherSuppliers: List<SupplierResponse> = emptyList()

    override suspend fun allSuppliers(): Result<List<SupplierResponse>> {
        allCalls++
        error?.let { return Result.failure(it) }
        return Result.success(suppliers + otherSuppliers)
    }

    override suspend fun supplier(id: Int): Result<SupplierResponse> {
        requestedIds += id
        error?.let { return Result.failure(it) }
        return suppliers.firstOrNull { it.id == id }?.let { Result.success(it) }
            ?: Result.failure(NoSuchElementException("Fornecedor não encontrado."))
    }

    var createResult: Result<SupplierResponse>? = null
    var created = mutableListOf<CreateSupplierRequest>()
        private set

    override suspend fun create(request: CreateSupplierRequest): Result<SupplierResponse> {
        created += request
        val supplier = SupplierResponse(id = NEW_ID, legalName = request.legalName, cnpj = request.cnpj)
        return createResult ?: Result.success(supplier.copy(rating = request.rating))
    }

    companion object {
        const val NEW_ID = 77

        fun supplier(
            id: Int,
            name: String,
            rating: Int? = null,
            email: String? = null,
            whatsapp: String? = null,
        ) = SupplierResponse(
            id = id,
            legalName = name,
            cnpj = "00.000.000/0001-0$id",
            email = email,
            whatsapp = whatsapp,
            rating = rating,
        )
    }
}
