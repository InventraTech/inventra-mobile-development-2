package com.inventraoficial.inventra.data.repository

import com.inventraoficial.inventra.data.remote.dto.product.LinkSupplierRequest
import com.inventraoficial.inventra.data.remote.dto.product.ProductSupplierResponse
import com.inventraoficial.inventra.data.remote.dto.product.ProductSupplierSummary

class FakeProductSupplierRepository(
    var links: List<ProductSupplierResponse> = emptyList(),
) : ProductSupplierRepository {
    var linkResult: Result<Unit> = Result.success(Unit)
    var linkCalls = mutableListOf<Pair<Int, LinkSupplierRequest>>()
        private set

    override suspend fun link(
        productId: Int,
        request: LinkSupplierRequest,
    ): Result<Unit> {
        linkCalls += productId to request
        return linkResult
    }

    override suspend fun suppliersOf(productId: Int): Result<List<ProductSupplierResponse>> = Result.success(links)

    companion object {
        fun link(
            supplierId: Int,
            name: String,
            referencePrice: Double? = null,
            supplierCode: String? = null,
        ) = ProductSupplierResponse(
            supplier = ProductSupplierSummary(supplierId, name),
            supplierCode = supplierCode,
            referencePrice = referencePrice,
        )
    }
}
