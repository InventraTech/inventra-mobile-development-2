package com.inventraoficial.inventra.data.remote.api

import com.inventraoficial.inventra.data.remote.dto.product.ProductResponse
import okhttp3.MultipartBody
import retrofit2.http.DELETE
import retrofit2.http.Multipart
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path

interface ProductPhotoApi {
    @Multipart
    @PUT(PATH)
    suspend fun update(
        @Path("id") productId: Int,
        @Part file: MultipartBody.Part,
    ): ProductResponse

    @DELETE(PATH)
    suspend fun remove(
        @Path("id") productId: Int,
    ): ProductResponse

    companion object {
        private const val PATH = "products/{id}/photo"
    }
}
