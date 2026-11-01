package com.inventraoficial.inventra.data.remote.api

import com.inventraoficial.inventra.data.remote.dto.product.LinkSupplierRequest
import com.inventraoficial.inventra.data.remote.dto.product.ProductSupplierResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ProductSupplierApi {
    @POST(PATH)
    suspend fun link(
        @Path("id") productId: Int,
        @Body request: LinkSupplierRequest,
    )

    @GET(PATH)
    suspend fun list(
        @Path("id") productId: Int,
    ): List<ProductSupplierResponse>

    companion object {
        private const val PATH = "products/{id}/suppliers"
    }
}
