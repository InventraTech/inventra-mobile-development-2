package com.inventraoficial.inventra.data.remote

import com.inventraoficial.inventra.data.remote.dto.auth.ErrorResponse
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import retrofit2.HttpException
import java.io.IOException

private const val GENERIC_ERROR = "Erro inesperado. Tente novamente."
private const val NETWORK_ERROR = "Não foi possível conectar ao servidor. Verifique sua conexão."
private const val PARSE_ERROR = "Resposta inesperada do servidor. Tente novamente."

suspend fun <T> apiCall(
    json: Json,
    call: suspend () -> T,
): Result<T> =
    try {
        Result.success(call())
    } catch (e: HttpException) {
        val body = e.response()?.errorBody()?.string()
        val error = body?.let { runCatching { json.decodeFromString<ErrorResponse>(it) }.getOrNull() }
        Result.failure(Exception(error?.detail ?: GENERIC_ERROR, e))
    } catch (e: IOException) {
        Result.failure(Exception(NETWORK_ERROR, e))
    } catch (e: SerializationException) {
        Result.failure(Exception(PARSE_ERROR, e))
    }
