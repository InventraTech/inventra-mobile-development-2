package com.inventraoficial.inventra.data.remote.api

import com.inventraoficial.inventra.data.remote.dto.product.PageProductResponse
import com.inventraoficial.inventra.data.remote.dto.product.ProductResponse
import retrofit2.http.GET
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

    companion object {
        private const val PATH = "products"
    }
}
