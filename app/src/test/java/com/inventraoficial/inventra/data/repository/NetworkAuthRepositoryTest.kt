package com.inventraoficial.inventra.data.repository

import com.inventraoficial.inventra.data.local.FakeSessionStorage
import com.inventraoficial.inventra.data.local.toSessionUser
import com.inventraoficial.inventra.data.remote.api.AuthApi
import com.inventraoficial.inventra.data.remote.api.UserApi
import com.inventraoficial.inventra.data.remote.dto.auth.AccessType
import com.inventraoficial.inventra.data.remote.dto.auth.KitchenSummary
import com.inventraoficial.inventra.data.remote.dto.auth.LoginRequest
import com.inventraoficial.inventra.data.remote.dto.auth.LoginResponse
import com.inventraoficial.inventra.data.remote.dto.auth.RegisterRequest
import com.inventraoficial.inventra.data.remote.dto.user.UserResponse
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class NetworkAuthRepositoryTest {
    private val json = Json { ignoreUnknownKeys = true }
    private val sessionStorage = FakeSessionStorage()

    private val successResponse =
        LoginResponse(
            token = "jwt",
            tokenType = "Bearer",
            expiresIn = 3600,
            user = FakeAuthRepository.fakeUser,
        )

    /** UserApi falsa: executa [respond] no lugar do GET /users/me. */
    private class FakeUserApi(
        private val respond: () -> UserResponse,
    ) : UserApi {
        override suspend fun me(): UserResponse = respond()
    }

    private val userApi: UserApi = FakeUserApi { FakeAuthRepository.fakeUser }

    /** AuthApi falsa: executa [respond] no lugar da chamada HTTP, tanto no login quanto no cadastro. */
    private class FakeAuthApi(
        private val respond: () -> LoginResponse,
    ) : AuthApi {
        var lastRequest: LoginRequest? = null
            private set
        var lastRegisterRequest: RegisterRequest? = null
            private set

        override suspend fun login(loginRequest: LoginRequest): LoginResponse {
            lastRequest = loginRequest
            return respond()
        }

        override suspend fun register(registerRequest: RegisterRequest): LoginResponse {
            lastRegisterRequest = registerRequest
            return respond()
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
        return NetworkAuthRepository(api, userApi, json, sessionStorage)
    }

    @Test
    fun `login com sucesso devolve o usuario, monta o LoginRequest e salva o token`() =
        runTest {
            val api = FakeAuthApi { successResponse }
            val repository = NetworkAuthRepository(api, userApi, json, sessionStorage)

            val result = repository.login("teste@inventra.com", "senha123")

            assertEquals(FakeAuthRepository.fakeUser, result.getOrNull())
            assertEquals(LoginRequest("teste@inventra.com", "senha123"), api.lastRequest)
            assertEquals("jwt", sessionStorage.currentToken)
            assertEquals(FakeAuthRepository.fakeUser.toSessionUser(), sessionStorage.currentUser)
        }

    @Test
    fun `login com falha nao salva token`() =
        runTest {
            val body = """{"title":"Unauthorized","status":401,"detail":"E-mail ou senha inválidos."}"""
            val repository = repositoryThatThrows(httpError(401, body))

            repository.login("teste@inventra.com", "errada")

            assertNull(sessionStorage.currentToken)
            assertNull(sessionStorage.currentUser)
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

    @Test
    fun `register com sucesso monta o RegisterRequest e salva o token`() =
        runTest {
            val api = FakeAuthApi { successResponse }
            val repository = NetworkAuthRepository(api, userApi, json, sessionStorage)

            val result = repository.register("Felipe", "teste@inventra.com", "senha1234", AccessType.ESTOQUISTA)

            assertEquals(FakeAuthRepository.fakeUser, result.getOrNull())
            assertEquals(
                RegisterRequest("Felipe", "teste@inventra.com", "senha1234", AccessType.ESTOQUISTA),
                api.lastRegisterRequest,
            )
            assertEquals("jwt", sessionStorage.currentToken)
        }

    @Test
    fun `register com email ja cadastrado usa o detail do backend`() =
        runTest {
            val body = """{"title":"Conflict","status":409,"detail":"E-mail já cadastrado."}"""
            val repository = repositoryThatThrows(httpError(409, body))

            val result = repository.register("Felipe", "teste@inventra.com", "senha1234", AccessType.SUPERVISOR)

            assertEquals("E-mail já cadastrado.", result.exceptionOrNull()?.message)
            assertNull(sessionStorage.currentToken)
        }

    @Test
    fun `RegisterRequest envia o accessType em maiusculas como o backend espera`() {
        val request = RegisterRequest("Felipe", "teste@inventra.com", "senha1234", AccessType.SUPERVISOR)

        val encoded = json.encodeToString(RegisterRequest.serializer(), request)

        assertTrue(encoded.contains("\"accessType\":\"SUPERVISOR\""))
    }

    @Test
    fun `hasSession e false sem token e true depois do login`() =
        runTest {
            val repository = NetworkAuthRepository(FakeAuthApi { successResponse }, userApi, json, sessionStorage)

            assertEquals(false, repository.hasSession())

            repository.login("teste@inventra.com", "senha123")

            assertEquals(true, repository.hasSession())
        }

    @Test
    fun `logout apaga o token e encerra a sessao`() =
        runTest {
            val repository = NetworkAuthRepository(FakeAuthApi { successResponse }, userApi, json, sessionStorage)
            repository.login("teste@inventra.com", "senha123")

            repository.logout()

            assertNull(sessionStorage.currentToken)
            assertNull(sessionStorage.currentUser)
            assertEquals(false, repository.hasSession())
        }

    @Test
    fun `currentUser comeca nulo, recebe o usuario no login e volta a nulo no logout`() =
        runTest {
            val repository = NetworkAuthRepository(FakeAuthApi { successResponse }, userApi, json, sessionStorage)

            assertNull(repository.currentUser.first())

            repository.login("teste@inventra.com", "senha123")
            assertEquals(FakeAuthRepository.fakeUser.toSessionUser(), repository.currentUser.first())

            repository.logout()
            assertNull(repository.currentUser.first())
        }

    /** Repositorio ja logado (token "jwt" e o fakeUser salvos), usando a [api] de usuario informada. */
    private suspend fun loggedRepository(api: UserApi): NetworkAuthRepository {
        val repository = NetworkAuthRepository(FakeAuthApi { successResponse }, api, json, sessionStorage)
        repository.login("teste@inventra.com", "senha123")
        return repository
    }

    @Test
    fun `refreshCurrentUser troca o usuario salvo pelo do servidor e mantem o token`() =
        runTest {
            val cozinha = KitchenSummary(id = 7, name = "Sabor Filial Sul")
            val repository = loggedRepository(FakeUserApi { FakeAuthRepository.fakeUser.copy(kitchen = cozinha) })

            val result = repository.refreshCurrentUser()

            assertTrue(result.isSuccess)
            assertEquals(7, sessionStorage.currentUser?.kitchenId)
            assertEquals("jwt", sessionStorage.currentToken)
        }

    @Test
    fun `refreshCurrentUser sem conexao devolve falha e mantem o usuario antigo`() =
        runTest {
            val repository = loggedRepository(FakeUserApi { throw IOException("timeout") })

            val result = repository.refreshCurrentUser()

            assertTrue(result.isFailure)
            assertEquals(FakeAuthRepository.fakeUser.toSessionUser(), sessionStorage.currentUser)
        }
}
