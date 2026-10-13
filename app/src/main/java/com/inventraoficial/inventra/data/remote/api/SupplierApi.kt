package com.inventraoficial.inventra.data.remote.api

import com.inventraoficial.inventra.data.remote.dto.supplier.SupplierResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface SupplierApi {
    @GET(PATH)
    suspend fun list(
        @Query("kitchenId") kitchenId: Int,
    ): List<SupplierResponse>

    @GET("$PATH/{id}")
    suspend fun find(
        @Path("id") id: Int,
    ): SupplierResponse

    companion object {
        private const val PATH = "suppliers"
    }
}
