package com.inventraoficial.inventra.data.repository

import com.inventraoficial.inventra.data.local.FakeSessionStorage
import com.inventraoficial.inventra.data.local.toSessionUser
import com.inventraoficial.inventra.data.remote.api.AuthApi
import com.inventraoficial.inventra.data.remote.api.UserApi
import com.inventraoficial.inventra.data.remote.dto.auth.AccessType
import com.inventraoficial.inventra.data.remote.dto.auth.GoogleLoginRequest
import com.inventraoficial.inventra.data.remote.dto.auth.KitchenSummary
import com.inventraoficial.inventra.data.remote.dto.auth.LoginRequest
import com.inventraoficial.inventra.data.remote.dto.auth.LoginResponse
import com.inventraoficial.inventra.data.remote.dto.auth.RefreshTokenRequest
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
            refreshToken = "refresh-jwt",
            refreshExpiresIn = 604800,
            user = FakeAuthRepository.fakeUser,
        )

    private class FakeUserApi(
        private val respond: () -> UserResponse,
    ) : UserApi {
        override suspend fun me(): UserResponse = respond()

        override suspend fun listMembers(): List<UserResponse> = emptyList()
    }

    private val userApi: UserApi = FakeUserApi { FakeAuthRepository.fakeUser }

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

        var lastGoogleRequest: GoogleLoginRequest? = null
            private set

        override suspend fun loginWithGoogle(googleLoginRequest: GoogleLoginRequest): LoginResponse {
            lastGoogleRequest = googleLoginRequest
            return respond()
        }

        override suspend fun refresh(refreshRequest: RefreshTokenRequest): LoginResponse = respond()

        var lastLogoutRequest: RefreshTokenRequest? = null
            private set

        var logoutError: Exception? = null

        override suspend fun logout(refreshRequest: RefreshTokenRequest) {
            lastLogoutRequest = refreshRequest
            logoutError?.let { throw it }
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
            assertEquals("refresh-jwt", sessionStorage.currentRefreshToken)
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
    fun `login com Google envia o idToken sem cargo e salva a sessao`() =
        runTest {
            val api = FakeAuthApi { successResponse }
            val repository = NetworkAuthRepository(api, userApi, json, sessionStorage)

            val result = repository.loginWithGoogle("firebase-id-token")

            assertEquals(FakeAuthRepository.fakeUser, result.getOrNull())
            assertEquals(GoogleLoginRequest("firebase-id-token"), api.lastGoogleRequest)
            assertEquals("jwt", sessionStorage.currentToken)
            assertEquals("refresh-jwt", sessionStorage.currentRefreshToken)
        }

    @Test
    fun `login com Google no primeiro acesso envia o cargo escolhido`() =
        runTest {
            val api = FakeAuthApi { successResponse }
            val repository = NetworkAuthRepository(api, userApi, json, sessionStorage)

            repository.loginWithGoogle("firebase-id-token", AccessType.ESTOQUISTA)

            assertEquals(GoogleLoginRequest("firebase-id-token", AccessType.ESTOQUISTA), api.lastGoogleRequest)
        }

    @Test
    fun `login com Google sem cadastro devolve o nome e o email do token`() =
        runTest {
            val body =
                """
                {"title":"Conflict","status":409,"detail":"Conta ainda não cadastrada.",
                "code":"REGISTRATION_REQUIRED","email":"felipe@gmail.com","name":"Felipe Kogake"}
                """.trimIndent()
            val repository = repositoryThatThrows(httpError(409, body))

            val error = repository.loginWithGoogle("firebase-id-token").exceptionOrNull()

            assertTrue(error is GoogleRegistrationRequiredException)
            assertEquals("Felipe Kogake", (error as GoogleRegistrationRequiredException).name)
            assertEquals("felipe@gmail.com", error.email)
            assertNull(sessionStorage.currentToken)
        }

    @Test
    fun `login com Google com outro 409 mantem o detail do backend`() =
        runTest {
            val body = """{"title":"Conflict","status":409,"detail":"Conflito qualquer."}"""
            val repository = repositoryThatThrows(httpError(409, body))

            val error = repository.loginWithGoogle("firebase-id-token").exceptionOrNull()

            assertTrue(error !is GoogleRegistrationRequiredException)
            assertEquals("Conflito qualquer.", error?.message)
        }

    @Test
    fun `GoogleLoginRequest sem cargo nao envia o campo accessType`() {
        val encoded = json.encodeToString(GoogleLoginRequest.serializer(), GoogleLoginRequest("token"))

        assertEquals("""{"idToken":"token"}""", encoded)
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
            assertNull(sessionStorage.currentRefreshToken)
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

    @Test
    fun `logout cancela o refresh token no servidor e apaga a sessao`() =
        runTest {
            val api = FakeAuthApi { successResponse }
            val repository = NetworkAuthRepository(api, userApi, json, sessionStorage)
            repository.login("teste@inventra.com", "senha123")

            repository.logout()

            assertEquals(RefreshTokenRequest("refresh-jwt"), api.lastLogoutRequest)
            assertNull(sessionStorage.currentRefreshToken)
            assertNull(sessionStorage.currentToken)
        }

    @Test
    fun `logout sem conexao apaga a sessao local mesmo assim`() =
        runTest {
            val api = FakeAuthApi { successResponse }
            val repository = NetworkAuthRepository(api, userApi, json, sessionStorage)
            repository.login("teste@inventra.com", "senha123")
            api.logoutError = IOException("sem sinal")

            repository.logout()

            assertNull(sessionStorage.currentToken)
            assertNull(sessionStorage.currentUser)
            assertNull(sessionStorage.currentRefreshToken)
        }

    @Test
    fun `logout limpa a sessao do Google depois de apagar a sessao local`() =
        runTest {
            var tokenNaHoraDeLimpar: String? = "nao chamado"
            val repository =
                NetworkAuthRepository(
                    FakeAuthApi { successResponse },
                    userApi,
                    json,
                    sessionStorage,
                    clearExternalSession = { tokenNaHoraDeLimpar = sessionStorage.currentToken },
                )
            repository.login("teste@inventra.com", "senha123")

            repository.logout()

            assertNull(tokenNaHoraDeLimpar)
        }

    @Test
    fun `logout sem conexao limpa a sessao do Google mesmo assim`() =
        runTest {
            var limpezas = 0
            val api = FakeAuthApi { successResponse }
            val repository =
                NetworkAuthRepository(api, userApi, json, sessionStorage, clearExternalSession = { limpezas++ })
            repository.login("teste@inventra.com", "senha123")
            api.logoutError = IOException("sem sinal")

            repository.logout()

            assertEquals(1, limpezas)
        }

    @Test
    fun `logout sem refresh token salvo nao chama o servidor`() =
        runTest {
            val api = FakeAuthApi { successResponse }
            val repository = NetworkAuthRepository(api, userApi, json, sessionStorage)

            repository.logout()

            assertNull(api.lastLogoutRequest)
        }
}
