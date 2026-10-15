package com.inventraoficial.inventra.data.repository

import com.inventraoficial.inventra.data.remote.dto.product.ProductResponse
import com.inventraoficial.inventra.data.remote.dto.stock.StockCategory
import com.inventraoficial.inventra.data.remote.dto.stock.StockUnit

class FakeProductRepository(
    var products: List<ProductResponse> = emptyList(),
) : ProductRepository {
    var error: Exception? = null
    var calls = mutableListOf<Int>()
        private set

    override suspend fun product(id: Int): Result<ProductResponse> {
        calls += id
        error?.let { return Result.failure(it) }
        return products.firstOrNull { it.id == id }?.let { Result.success(it) }
            ?: Result.failure(NoSuchElementException("Produto não encontrado."))
    }

    companion object {
        fun product(
            id: Int,
            name: String,
            unitSymbol: String = "L",
            brand: String? = null,
            categoryName: String? = null,
        ) = ProductResponse(
            id = id,
            name = name,
            brand = brand,
            category = categoryName?.let { StockCategory(id = 1, name = it) },
            unit = StockUnit(id = 1, symbol = unitSymbol),
        )
    }
}
