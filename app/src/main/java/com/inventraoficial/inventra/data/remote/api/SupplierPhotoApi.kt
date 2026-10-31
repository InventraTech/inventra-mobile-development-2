package com.inventraoficial.inventra.data.remote.api

import com.inventraoficial.inventra.data.remote.dto.supplier.SupplierResponse
import okhttp3.MultipartBody
import retrofit2.http.DELETE
import retrofit2.http.Multipart
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path

interface SupplierPhotoApi {
    @Multipart
    @PUT(PATH)
    suspend fun update(
        @Path("id") supplierId: Int,
        @Part file: MultipartBody.Part,
    ): SupplierResponse

    @DELETE(PATH)
    suspend fun remove(
        @Path("id") supplierId: Int,
    ): SupplierResponse

    companion object {
        private const val PATH = "suppliers/{id}/photo"
    }
}
