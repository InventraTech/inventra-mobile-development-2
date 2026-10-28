package com.inventraoficial.inventra.data.repository

import com.inventraoficial.inventra.data.local.ImageCompressor
import com.inventraoficial.inventra.data.remote.api.ProductPhotoApi
import com.inventraoficial.inventra.data.remote.apiCall
import com.inventraoficial.inventra.data.remote.dto.product.ProductResponse
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

interface ProductPhotoRepository {
    suspend fun update(
        productId: Int,
        imageUri: String,
    ): Result<ProductResponse>

    suspend fun remove(productId: Int): Result<ProductResponse>
}

class NetworkProductPhotoRepository(
    private val productPhotoApi: ProductPhotoApi,
    private val imageCompressor: ImageCompressor,
    private val json: Json,
) : ProductPhotoRepository {
    override suspend fun update(
        productId: Int,
        imageUri: String,
    ): Result<ProductResponse> {
        val image = imageCompressor.compress(imageUri).getOrElse { return Result.failure(it) }
        return apiCall(json) {
            val body = image.toRequestBody(JPEG.toMediaType())
            val part = MultipartBody.Part.createFormData(PHOTO_FIELD, "produto-$productId.jpg", body)
            productPhotoApi.update(productId, part)
        }
    }

    override suspend fun remove(productId: Int): Result<ProductResponse> =
        apiCall(json) {
            productPhotoApi.remove(productId)
        }

    private companion object {
        const val JPEG = "image/jpeg"
        const val PHOTO_FIELD = "file"
    }
}
