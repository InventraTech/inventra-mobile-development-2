package com.inventraoficial.inventra.data.repository

import com.inventraoficial.inventra.data.local.ImageCompressor
import com.inventraoficial.inventra.data.remote.api.ProductPhotoApi
import com.inventraoficial.inventra.data.remote.dto.product.ProductResponse
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.MultipartBody
import okio.Buffer
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NetworkProductPhotoRepositoryTest {
    private val json = Json { ignoreUnknownKeys = true }
    private val image = byteArrayOf(1, 2, 3)

    private class FakeProductPhotoApi : ProductPhotoApi {
        var uploaded: Pair<Int, MultipartBody.Part>? = null
            private set
        var removed: Int? = null
            private set

        override suspend fun update(
            productId: Int,
            file: MultipartBody.Part,
        ): ProductResponse {
            uploaded = productId to file
            return FakeProductRepository.product(productId, "Ketchup").copy(photoUrl = "https://cdn/ketchup.jpg")
        }

        override suspend fun remove(productId: Int): ProductResponse {
            removed = productId
            return FakeProductRepository.product(productId, "Ketchup")
        }
    }

    @Test
    fun `update comprime a imagem e envia no campo file`() =
        runTest {
            val api = FakeProductPhotoApi()
            val repository = NetworkProductPhotoRepository(api, ImageCompressor { Result.success(image) }, json)

            val result = repository.update(42, "content://galeria/1")

            assertEquals("https://cdn/ketchup.jpg", result.getOrNull()?.photoUrl)
            val (productId, part) = api.uploaded!!
            assertEquals(42, productId)
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
            val api = FakeProductPhotoApi()
            val compressor = ImageCompressor { Result.failure(Exception("Não foi possível ler a imagem.")) }
            val repository = NetworkProductPhotoRepository(api, compressor, json)

            val result = repository.update(42, "content://galeria/1")

            assertEquals("Não foi possível ler a imagem.", result.exceptionOrNull()?.message)
            assertNull(api.uploaded)
        }

    @Test
    fun `remove apaga a foto do produto`() =
        runTest {
            val api = FakeProductPhotoApi()
            val repository = NetworkProductPhotoRepository(api, ImageCompressor { Result.success(image) }, json)

            val result = repository.remove(42)

            assertNull(result.getOrNull()?.photoUrl)
            assertEquals(42, api.removed)
        }
}
