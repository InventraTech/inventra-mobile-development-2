package com.inventraoficial.inventra.data.remote.api

import com.inventraoficial.inventra.data.remote.dto.product.ProductResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface ProductApi {
    @GET("$PATH/{id}")
    suspend fun find(
        @Path("id") id: Int,
    ): ProductResponse

    companion object {
        private const val PATH = "products"
    }
}
