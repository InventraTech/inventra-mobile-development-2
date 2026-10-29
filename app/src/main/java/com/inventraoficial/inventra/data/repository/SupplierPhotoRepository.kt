package com.inventraoficial.inventra.data.repository

import com.inventraoficial.inventra.data.local.ImageCompressor
import com.inventraoficial.inventra.data.remote.api.SupplierPhotoApi
import com.inventraoficial.inventra.data.remote.apiCall
import com.inventraoficial.inventra.data.remote.dto.supplier.SupplierResponse
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

interface SupplierPhotoRepository {
    suspend fun update(
        supplierId: Int,
        imageUri: String,
    ): Result<SupplierResponse>

    suspend fun remove(supplierId: Int): Result<SupplierResponse>
}

class NetworkSupplierPhotoRepository(
    private val supplierPhotoApi: SupplierPhotoApi,
    private val imageCompressor: ImageCompressor,
    private val json: Json,
) : SupplierPhotoRepository {
    override suspend fun update(
        supplierId: Int,
        imageUri: String,
    ): Result<SupplierResponse> {
        val image = imageCompressor.compress(imageUri).getOrElse { return Result.failure(it) }
        return apiCall(json) {
            val body = image.toRequestBody(JPEG.toMediaType())
            val part = MultipartBody.Part.createFormData(PHOTO_FIELD, "fornecedor-$supplierId.jpg", body)
            supplierPhotoApi.update(supplierId, part)
        }
    }

    override suspend fun remove(supplierId: Int): Result<SupplierResponse> =
        apiCall(json) {
            supplierPhotoApi.remove(supplierId)
        }

    private companion object {
        const val JPEG = "image/jpeg"
        const val PHOTO_FIELD = "file"
    }
}
