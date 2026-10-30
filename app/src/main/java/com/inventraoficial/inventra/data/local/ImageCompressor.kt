package com.inventraoficial.inventra.data.local

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import androidx.core.net.toUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.nio.ByteBuffer

fun interface ImageCompressor {
    suspend fun compress(uri: String): Result<ByteArray>
}

class AndroidImageCompressor(
    private val context: Context,
    private val httpClient: OkHttpClient,
) : ImageCompressor {
    override suspend fun compress(uri: String): Result<ByteArray> =
        withContext(Dispatchers.IO) {
            try {
                Result.success(decodeAndCompress(uri))
            } catch (e: IOException) {
                Result.failure(Exception(READ_ERROR, e))
            } catch (e: IllegalArgumentException) {
                Result.failure(Exception(READ_ERROR, e))
            }
        }

    private fun decodeAndCompress(uri: String): ByteArray {
        val source =
            if (uri.startsWith(HTTPS)) {
                ImageDecoder.createSource(ByteBuffer.wrap(download(uri)))
            } else {
                ImageDecoder.createSource(context.contentResolver, uri.toUri())
            }
        val bitmap =
            ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
                val largestSide = maxOf(info.size.width, info.size.height)
                if (largestSide > MAX_SIDE) {
                    val scale = MAX_SIDE.toFloat() / largestSide
                    decoder.setTargetSize((info.size.width * scale).toInt(), (info.size.height * scale).toInt())
                }
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
            }
        return ByteArrayOutputStream().use { output ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, output)
            bitmap.recycle()
            output.toByteArray()
        }
    }

    private fun download(url: String): ByteArray =
        httpClient.newCall(Request.Builder().url(url).build()).execute().use { response ->
            val body = response.body
            if (!response.isSuccessful || body.contentLength() > MAX_DOWNLOAD_BYTES) {
                throw IOException("Download da imagem falhou: HTTP ${response.code}")
            }
            body.bytes()
        }

    private companion object {
        const val HTTPS = "https://"
        const val MAX_DOWNLOAD_BYTES = 10L * 1024 * 1024
        const val MAX_SIDE = 1024
        const val JPEG_QUALITY = 85
        const val READ_ERROR = "Não foi possível ler a imagem escolhida."
    }
}
