package com.inventraoficial.inventra.data.remote

import kotlinx.coroutines.test.runTest
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class ApiCallTest {
    private val json = Json { ignoreUnknownKeys = true }

    private fun httpError(
        code: Int,
        body: String,
    ): HttpException {
        val responseBody = body.toResponseBody("application/json".toMediaType())
        return HttpException(Response.error<Any>(code, responseBody))
    }

    @Test
    fun `sucesso devolve o valor da chamada`() =
        runTest {
            val result = apiCall(json) { 42 }

            assertEquals(42, result.getOrNull())
        }

    @Test
    fun `erro HTTP usa o detail do ProblemDetail`() =
        runTest {
            val body = """{"title":"Bad Request","status":422,"detail":"Você já está vinculado a uma cozinha."}"""

            val result = apiCall<Int>(json) { throw httpError(422, body) }

            assertEquals("Você já está vinculado a uma cozinha.", result.exceptionOrNull()?.message)
        }

    @Test
    fun `erro HTTP sem JSON usa a mensagem generica`() =
        runTest {
            val result = apiCall<Int>(json) { throw httpError(502, "<html>Bad Gateway</html>") }

            assertEquals("Erro inesperado. Tente novamente.", result.exceptionOrNull()?.message)
        }

    @Test
    fun `sem conexao usa a mensagem de rede e preserva a causa`() =
        runTest {
            val cause = IOException("timeout")

            val result = apiCall<Int>(json) { throw cause }

            val expected = "Não foi possível conectar ao servidor. Verifique sua conexão."
            assertEquals(expected, result.exceptionOrNull()?.message)
            assertEquals(cause, result.exceptionOrNull()?.cause)
        }

    @Test
    fun `resposta em formato inesperado vira falha em vez de quebrar o app`() =
        runTest {
            val result = apiCall<Int>(json) { throw SerializationException("campo faltando") }

            assertEquals("Resposta inesperada do servidor. Tente novamente.", result.exceptionOrNull()?.message)
        }
}
