package com.inventraoficial.inventra.data.repository

import com.inventraoficial.inventra.data.remote.dto.prelist.PreListItemResponse
import com.inventraoficial.inventra.data.remote.dto.prelist.PreListProduct
import com.inventraoficial.inventra.data.remote.dto.prelist.PreListResponse
import com.inventraoficial.inventra.data.remote.dto.stock.SupplierSummary

class FakePreListRepository(
    var preLists: List<PreListResponse> = emptyList(),
) : PreListRepository {
    var error: Exception? = null
    var listCalls = 0
        private set
    var detailCalls = mutableListOf<Int>()
        private set

    override suspend fun preLists(): Result<List<PreListResponse>> {
        listCalls++
        error?.let { return Result.failure(it) }
        return Result.success(preLists.map { it.copy(items = emptyList()) })
    }

    override suspend fun preList(id: Int): Result<PreListResponse> {
        detailCalls += id
        error?.let { return Result.failure(it) }
        return preLists.firstOrNull { it.id == id }?.let { Result.success(it) }
            ?: Result.failure(NoSuchElementException("Pré-lista não encontrada."))
    }

    companion object {
        fun preList(
            id: Int,
            listDate: String,
            items: List<PreListItemResponse>,
        ) = PreListResponse(id = id, listDate = listDate, items = items)

        fun preListItem(
            id: Int,
            productName: String,
            quantity: Double,
            supplierId: Int? = 1,
            photoUrl: String? = null,
        ) = PreListItemResponse(
            id = id,
            product = PreListProduct(id = id, name = productName, photoUrl = photoUrl),
            supplier = supplierId?.let { SupplierSummary(it, "Fornecedor $it") },
            quantity = quantity,
        )
    }
}
