package com.inventraoficial.inventra.data.repository

import com.inventraoficial.inventra.data.local.ImageCompressor
import com.inventraoficial.inventra.data.remote.api.SupplierPhotoApi
import com.inventraoficial.inventra.data.remote.dto.supplier.SupplierResponse
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.MultipartBody
import okio.Buffer
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NetworkSupplierPhotoRepositoryTest {
    private val json = Json { ignoreUnknownKeys = true }
    private val image = byteArrayOf(1, 2, 3)

    private class FakeSupplierPhotoApi : SupplierPhotoApi {
        var uploaded: Pair<Int, MultipartBody.Part>? = null
            private set
        var removed: Int? = null
            private set

        override suspend fun update(
            supplierId: Int,
            file: MultipartBody.Part,
        ): SupplierResponse {
            uploaded = supplierId to file
            return FakeSupplierRepository.supplier(supplierId, "Seara").copy(photoUrl = "https://cdn/seara.jpg")
        }

        override suspend fun remove(supplierId: Int): SupplierResponse {
            removed = supplierId
            return FakeSupplierRepository.supplier(supplierId, "Seara")
        }
    }

    @Test
    fun `update comprime a imagem e envia no campo file`() =
        runTest {
            val api = FakeSupplierPhotoApi()
            val repository = NetworkSupplierPhotoRepository(api, ImageCompressor { Result.success(image) }, json)

            val result = repository.update(5, "content://galeria/1")

            assertEquals("https://cdn/seara.jpg", result.getOrNull()?.photoUrl)
            val (supplierId, part) = api.uploaded!!
            assertEquals(5, supplierId)
            assertTrue(
                part.headers
                    ?.get("Content-Disposition")
                    .orEmpty()
                    .contains("name=\"file\""),
            )
            val sent = Buffer().also { part.body.writeTo(it) }.readByteArray()
            assertArrayEquals(image, sent)
        }

    @Test
    fun `imagem que nao abre nao chega a enviar`() =
        runTest {
            val api = FakeSupplierPhotoApi()
            val compressor = ImageCompressor { Result.failure(Exception("Não foi possível ler a imagem.")) }
            val repository = NetworkSupplierPhotoRepository(api, compressor, json)

            val result = repository.update(5, "content://galeria/1")

            assertEquals("Não foi possível ler a imagem.", result.exceptionOrNull()?.message)
            assertNull(api.uploaded)
        }

    @Test
    fun `remove apaga a foto do fornecedor`() =
        runTest {
            val api = FakeSupplierPhotoApi()
            val repository = NetworkSupplierPhotoRepository(api, ImageCompressor { Result.success(image) }, json)

            val result = repository.remove(5)

            assertNull(result.getOrNull()?.photoUrl)
            assertEquals(5, api.removed)
        }
}
