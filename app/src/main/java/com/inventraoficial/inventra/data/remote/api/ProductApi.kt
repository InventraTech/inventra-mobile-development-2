package com.inventraoficial.inventra.data.remote.api

import com.inventraoficial.inventra.data.remote.dto.product.BarcodeLookupResponse
import com.inventraoficial.inventra.data.remote.dto.product.CategoryResponse
import com.inventraoficial.inventra.data.remote.dto.product.CreateProductRequest
import com.inventraoficial.inventra.data.remote.dto.product.PageProductResponse
import com.inventraoficial.inventra.data.remote.dto.product.ProductResponse
import com.inventraoficial.inventra.data.remote.dto.product.UnitResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ProductApi {
    @GET("$PATH/{id}")
    suspend fun find(
        @Path("id") id: Int,
    ): ProductResponse

    @GET(PATH)
    suspend fun search(
        @Query("name") name: String,
        @Query("size") size: Int,
        @Query("active") active: Boolean = true,
        @Query("sort") sort: String = "name",
    ): PageProductResponse

    @GET("$PATH/by-barcode/{barcode}")
    suspend fun findByBarcode(
        @Path("barcode") barcode: String,
        @Query("kitchenId") kitchenId: Int?,
    ): BarcodeLookupResponse

    @POST(PATH)
    suspend fun create(
        @Body request: CreateProductRequest,
    ): ProductResponse

    @GET("units")
    suspend fun units(): List<UnitResponse>

    @GET("categories")
    suspend fun categories(): List<CategoryResponse>

    companion object {
        private const val PATH = "products"
    }
}
