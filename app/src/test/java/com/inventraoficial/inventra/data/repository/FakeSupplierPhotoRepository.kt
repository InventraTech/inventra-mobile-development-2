package com.inventraoficial.inventra.data.repository

import com.inventraoficial.inventra.data.remote.dto.supplier.SupplierResponse

class FakeSupplierPhotoRepository : SupplierPhotoRepository {
    var error: Exception? = null
    var uploads = mutableListOf<Pair<Int, String>>()
        private set
    var removals = mutableListOf<Int>()
        private set

    override suspend fun update(
        supplierId: Int,
        imageUri: String,
    ): Result<SupplierResponse> {
        uploads += supplierId to imageUri
        error?.let { return Result.failure(it) }
        return Result.success(
            FakeSupplierRepository.supplier(supplierId, "Fornecedor").copy(photoUrl = uploadedUrl(supplierId)),
        )
    }

    override suspend fun remove(supplierId: Int): Result<SupplierResponse> {
        removals += supplierId
        error?.let { return Result.failure(it) }
        return Result.success(FakeSupplierRepository.supplier(supplierId, "Fornecedor"))
    }

    companion object {
        fun uploadedUrl(supplierId: Int) = "https://cdn/fornecedor-$supplierId.jpg"
    }
}
