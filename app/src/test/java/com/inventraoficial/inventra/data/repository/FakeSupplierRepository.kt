package com.inventraoficial.inventra.data.repository

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

    override suspend fun supplier(id: Int): Result<SupplierResponse> {
        requestedIds += id
        error?.let { return Result.failure(it) }
        return suppliers.firstOrNull { it.id == id }?.let { Result.success(it) }
            ?: Result.failure(NoSuchElementException("Fornecedor não encontrado."))
    }

    companion object {
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
