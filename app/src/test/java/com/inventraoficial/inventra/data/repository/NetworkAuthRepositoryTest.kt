package com.inventraoficial.inventra.data.repository

import com.inventraoficial.inventra.data.remote.api.AuthApi
import com.inventraoficial.inventra.data.remote.dto.auth.LoginRequest
import com.inventraoficial.inventra.data.remote.dto.auth.LoginResponse
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class NetworkAuthRepositoryTest {
    private val json = Json { ignoreUnknownKeys = true }

    /** AuthApi falsa: executa [onLogin] no lugar da chamada HTTP. */
    private class FakeAuthApi(
        private val onLogin: (LoginRequest) -> LoginResponse,
    ) : AuthApi {
        var lastRequest: LoginRequest? = null
            private set

        override suspend fun login(loginRequest: LoginRequest): LoginResponse {
            lastRequest = loginRequest
            return onLogin(loginRequest)
        }
    }

    private fun httpError(
        code: Int,
        body: String,
    ): HttpException {
        val responseBody = body.toResponseBody("application/json".toMediaType())
        return HttpException(Response.error<Any>(code, responseBody))
    }

    private fun repositoryThatThrows(exception: Exception): NetworkAuthRepository {
        val api = FakeAuthApi { throw exception }
        return NetworkAuthRepository(api, json)
    }

    @Test
    fun `login com sucesso devolve o usuario e monta o LoginRequest`() =
        runTest {
            val api =
                FakeAuthApi {
                    LoginResponse(
                        token = "jwt",
                        tokenType = "Bearer",
                        expiresIn = 3600,
                        user = FakeAuthRepository.fakeUser,
                    )
                }
            val repository = NetworkAuthRepository(api, json)

            val result = repository.login("teste@inventra.com", "senha123")

            assertEquals(FakeAuthRepository.fakeUser, result.getOrNull())
            assertEquals(LoginRequest("teste@inventra.com", "senha123"), api.lastRequest)
        }

    @Test
    fun `login com 401 usa o detail do ProblemDetail como mensagem`() =
        runTest {
            val body = """{"title":"Unauthorized","status":401,"detail":"E-mail ou senha inválidos."}"""
            val repository = repositoryThatThrows(httpError(401, body))

            val result = repository.login("teste@inventra.com", "errada")

            assertEquals("E-mail ou senha inválidos.", result.exceptionOrNull()?.message)
        }

    @Test
    fun `login com erro HTTP sem JSON no corpo usa a mensagem generica`() =
        runTest {
            val repository = repositoryThatThrows(httpError(502, "<html>Bad Gateway</html>"))

            val result = repository.login("teste@inventra.com", "senha123")

            assertEquals("Erro inesperado. Tente novamente.", result.exceptionOrNull()?.message)
        }

    @Test
    fun `login sem conexao devolve a mensagem de rede e preserva a causa`() =
        runTest {
            val cause = IOException("timeout")
            val repository = repositoryThatThrows(cause)

            val result = repository.login("teste@inventra.com", "senha123")

            val error = result.exceptionOrNull()
            assertEquals("Não foi possível conectar ao servidor. Verifique sua conexão.", error?.message)
            assertEquals(cause, error?.cause)
        }

    @Test
    fun `login com resposta em formato inesperado devolve falha em vez de quebrar o app`() =
        runTest {
            val cause = SerializationException("AccessType does not contain element with name 'supervisor'")
            val repository = repositoryThatThrows(cause)

            val result = repository.login("teste@inventra.com", "senha123")

            assertTrue(result.isFailure)
            assertEquals("Resposta inesperada do servidor. Tente novamente.", result.exceptionOrNull()?.message)
            assertEquals(cause, result.exceptionOrNull()?.cause)
        }
}
