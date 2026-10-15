package com.inventraoficial.inventra.data.local

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import androidx.core.net.toUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.IOException

fun interface ImageCompressor {
    suspend fun compress(uri: String): Result<ByteArray>
}

class AndroidImageCompressor(
    private val context: Context,
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
        val source = ImageDecoder.createSource(context.contentResolver, uri.toUri())
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

    private companion object {
        const val MAX_SIDE = 1024
        const val JPEG_QUALITY = 85
        const val READ_ERROR = "Não foi possível ler a imagem escolhida."
    }
}
