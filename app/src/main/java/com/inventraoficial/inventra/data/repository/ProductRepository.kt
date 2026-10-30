package com.inventraoficial.inventra.data.repository

import com.inventraoficial.inventra.data.remote.api.ProductApi
import com.inventraoficial.inventra.data.remote.api.ProductStatusApi
import com.inventraoficial.inventra.data.remote.apiCall
import com.inventraoficial.inventra.data.remote.dto.product.BarcodeLookupResponse
import com.inventraoficial.inventra.data.remote.dto.product.CategoryResponse
import com.inventraoficial.inventra.data.remote.dto.product.CreateProductRequest
import com.inventraoficial.inventra.data.remote.dto.product.KitchenParametersRequest
import com.inventraoficial.inventra.data.remote.dto.product.KitchenParametersResponse
import com.inventraoficial.inventra.data.remote.dto.product.ProductResponse
import com.inventraoficial.inventra.data.remote.dto.product.UnitResponse
import com.inventraoficial.inventra.data.remote.dto.product.UpdateProductRequest
import kotlinx.serialization.json.Json
import retrofit2.HttpException

interface ProductRepository {
    suspend fun product(id: Int): Result<ProductResponse>

    suspend fun search(name: String): Result<List<ProductResponse>>

    // null quando nem o catálogo nem o Open Food Facts conhecem o código.
    suspend fun findByBarcode(
        barcode: String,
        kitchenId: Int?,
    ): Result<BarcodeLookupResponse?>

    suspend fun create(request: CreateProductRequest): Result<ProductResponse>

    suspend fun update(
        productId: Int,
        request: UpdateProductRequest,
    ): Result<ProductResponse>

    suspend fun setKitchenParameters(
        productId: Int,
        request: KitchenParametersRequest,
    ): Result<Unit>

    suspend fun kitchenParameters(
        productId: Int,
        kitchenId: Int,
    ): Result<KitchenParametersResponse?>

    suspend fun units(): Result<List<UnitResponse>>

    suspend fun categories(): Result<List<CategoryResponse>>

    suspend fun deactivate(id: Int): Result<Unit>
}

class NetworkProductRepository(
    private val productApi: ProductApi,
    private val json: Json,
    private val productStatusApi: ProductStatusApi,
) : ProductRepository {
    // As listas são grandes e quase nunca mudam: ficam em memória depois da primeira busca que der certo.
    private var cachedUnits: List<UnitResponse>? = null
    private var cachedCategories: List<CategoryResponse>? = null

    override suspend fun product(id: Int): Result<ProductResponse> = apiCall(json) { productApi.find(id) }

    override suspend fun search(name: String): Result<List<ProductResponse>> =
        apiCall(json) { productApi.search(name = name.trim(), size = SEARCH_SIZE).content }

    override suspend fun findByBarcode(
        barcode: String,
        kitchenId: Int?,
    ): Result<BarcodeLookupResponse?> =
        apiCall(json) {
            try {
                productApi.findByBarcode(barcode.trim(), kitchenId)
            } catch (e: HttpException) {
                if (e.code() == HTTP_NOT_FOUND) null else throw e
            }
        }

    override suspend fun create(request: CreateProductRequest): Result<ProductResponse> =
        apiCall(json) {
            productApi.create(request)
        }

    override suspend fun update(
        productId: Int,
        request: UpdateProductRequest,
    ): Result<ProductResponse> = apiCall(json) { productApi.update(productId, request) }

    override suspend fun setKitchenParameters(
        productId: Int,
        request: KitchenParametersRequest,
    ): Result<Unit> = apiCall(json) { productApi.setKitchenParameters(productId, request) }

    override suspend fun kitchenParameters(
        productId: Int,
        kitchenId: Int,
    ): Result<KitchenParametersResponse?> =
        apiCall(json) {
            productApi.kitchenParameters(productId).firstOrNull { it.kitchen.id == kitchenId }
        }

    override suspend fun units(): Result<List<UnitResponse>> =
        cachedUnits?.let { Result.success(it) }
            ?: apiCall(json) { productApi.units() }.onSuccess { cachedUnits = it }

    override suspend fun categories(): Result<List<CategoryResponse>> =
        cachedCategories?.let { Result.success(it) }
            ?: apiCall(json) { productApi.categories() }.onSuccess { cachedCategories = it }

    override suspend fun deactivate(id: Int): Result<Unit> =
        apiCall(json) {
            productStatusApi.deactivate(id)
        }

    private companion object {
        const val SEARCH_SIZE = 20
        const val HTTP_NOT_FOUND = 404
    }
}
