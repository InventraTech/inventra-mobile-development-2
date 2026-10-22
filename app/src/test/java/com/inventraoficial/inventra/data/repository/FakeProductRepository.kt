package com.inventraoficial.inventra.data.repository

import com.inventraoficial.inventra.data.remote.dto.product.BarcodeLookupResponse
import com.inventraoficial.inventra.data.remote.dto.product.CategoryResponse
import com.inventraoficial.inventra.data.remote.dto.product.CreateProductRequest
import com.inventraoficial.inventra.data.remote.dto.product.ProductResponse
import com.inventraoficial.inventra.data.remote.dto.product.UnitResponse
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

    var searches = mutableListOf<String>()
        private set

    override suspend fun search(name: String): Result<List<ProductResponse>> {
        searches += name
        error?.let { return Result.failure(it) }
        return Result.success(products.filter { it.name.contains(name, ignoreCase = true) })
    }

    var barcodeResult: Result<BarcodeLookupResponse?> = Result.success(null)
    var barcodeCalls = mutableListOf<Pair<String, Int?>>()
        private set

    override suspend fun findByBarcode(
        barcode: String,
        kitchenId: Int?,
    ): Result<BarcodeLookupResponse?> {
        barcodeCalls += barcode to kitchenId
        return barcodeResult
    }

    var createResult: Result<ProductResponse>? = null
    var createRequests = mutableListOf<CreateProductRequest>()
        private set

    override suspend fun create(request: CreateProductRequest): Result<ProductResponse> {
        createRequests += request
        return createResult ?: Result.success(product(id = 99, name = request.name))
    }

    var unitsResult: Result<List<UnitResponse>> = Result.success(defaultUnits)
    var categoriesResult: Result<List<CategoryResponse>> = Result.success(defaultCategories)

    override suspend fun units(): Result<List<UnitResponse>> = unitsResult

    override suspend fun categories(): Result<List<CategoryResponse>> = categoriesResult

    companion object {
        val defaultUnits =
            listOf(
                UnitResponse(1, "KG", "Quilograma"),
                UnitResponse(2, "G", "Grama"),
                UnitResponse(4, "L", "Litro"),
                UnitResponse(5, "ML", "Mililitro"),
                UnitResponse(6, "UN", "Unidade"),
                UnitResponse(10, "CX50G", "Caixa de 50g"),
            )

        val defaultCategories =
            listOf(
                CategoryResponse(5012, "Laticínios"),
                CategoryResponse(5015, "Mercearia"),
            )

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
