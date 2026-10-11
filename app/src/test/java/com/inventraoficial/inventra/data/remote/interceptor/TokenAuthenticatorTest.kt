package com.inventraoficial.inventra.data.remote.interceptor

import com.inventraoficial.inventra.data.local.FakeSessionStorage
import com.inventraoficial.inventra.data.local.toSessionUser
import com.inventraoficial.inventra.data.remote.api.AuthApi
import com.inventraoficial.inventra.data.remote.dto.auth.LoginRequest
import com.inventraoficial.inventra.data.remote.dto.auth.LoginResponse
import com.inventraoficial.inventra.data.remote.dto.auth.RefreshTokenRequest
import com.inventraoficial.inventra.data.remote.dto.auth.RegisterRequest
import com.inventraoficial.inventra.data.repository.FakeAuthRepository
import com.inventraoficial.inventra.data.session.SessionManager
import mockwebserver3.Dispatcher
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import mockwebserver3.RecordedRequest
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class TokenAuthenticatorTest {
    private val server = MockWebServer()
    private val sessionManager = SessionManager()

    private class FakeRefreshApi(
        private val respond: () -> LoginResponse,
    ) : AuthApi {
        var refreshCalls = 0
            private set
        var lastRefreshRequest: RefreshTokenRequest? = null
            private set

        override suspend fun refresh(refreshRequest: RefreshTokenRequest): LoginResponse {
            refreshCalls++
            lastRefreshRequest = refreshRequest
            return respond()
        }

        override suspend fun login(loginRequest: LoginRequest): LoginResponse = error("nao usado")

        override suspend fun register(registerRequest: RegisterRequest): LoginResponse = error("nao usado")

        override suspend fun logout(refreshRequest: RefreshTokenRequest) = Unit
    }

    private val renewed =
        LoginResponse(
            token = "jwt-novo",
            tokenType = "Bearer",
            expiresIn = 3600,
            refreshToken = "refresh-novo",
            refreshExpiresIn = 604800,
            user = FakeAuthRepository.fakeUser,
        )

    private fun loggedStorage() =
        FakeSessionStorage(
            initialToken = "jwt-vencido",
            initialUser = FakeAuthRepository.fakeUser.toSessionUser(),
            initialRefreshToken = "refresh-antigo",
        )

    private fun call(
        storage: FakeSessionStorage,
        refreshApi: AuthApi,
        path: String = "/api/kitchens",
    ): Int {
        val client =
            OkHttpClient
                .Builder()
                .addInterceptor(AuthInterceptor(storage, sessionManager))
                .authenticator(TokenAuthenticator(storage, refreshApi))
                .build()
        val request = Request.Builder().url(server.url(path)).build()
        return client.newCall(request).execute().use { it.code }
    }

    @Before
    fun setUp() {
        server.start()
    }

    @After
    fun tearDown() {
        server.close()
    }

    @Test
    fun `401 renova o token, repete a requisicao e a chamada termina com sucesso`() {
        server.enqueue(MockResponse(code = 401))
        server.enqueue(MockResponse(code = 200))
        val storage = loggedStorage()
        val refreshApi = FakeRefreshApi { renewed }

        val code = call(storage, refreshApi)

        assertEquals(200, code)
        assertEquals("Bearer jwt-vencido", server.takeRequest().headers["Authorization"])
        assertEquals("Bearer jwt-novo", server.takeRequest().headers["Authorization"])
        assertEquals(RefreshTokenRequest("refresh-antigo"), refreshApi.lastRefreshRequest)
    }

    @Test
    fun `depois de renovar, salva o token e o refresh token novos`() {
        server.enqueue(MockResponse(code = 401))
        server.enqueue(MockResponse(code = 200))
        val storage = loggedStorage()

        call(storage, FakeRefreshApi { renewed })

        assertEquals("jwt-novo", storage.currentToken)
        assertEquals("refresh-novo", storage.currentRefreshToken)
    }

    @Test
    fun `se o refresh falhar, desiste e a sessao e apagada pelo AuthInterceptor`() {
        server.enqueue(MockResponse(code = 401))
        val storage = loggedStorage()
        val refreshRecusado = FakeRefreshApi { throw HttpException(Response.error<Any>(401, "".toResponseBody())) }

        val code = call(storage, refreshRecusado)

        assertEquals(401, code)
        assertEquals(1, server.requestCount)
        assertNull(storage.currentToken)
        assertNull(storage.currentRefreshToken)
    }

    @Test
    fun `sem refresh token salvo nem tenta renovar`() {
        server.enqueue(MockResponse(code = 401))
        val storage = FakeSessionStorage(initialToken = "jwt-vencido")
        val refreshApi = FakeRefreshApi { renewed }

        val code = call(storage, refreshApi)

        assertEquals(401, code)
        assertEquals(0, refreshApi.refreshCalls)
    }

    @Test
    fun `401 numa rota de autenticacao nao tenta renovar`() {
        server.enqueue(MockResponse(code = 401))
        val refreshApi = FakeRefreshApi { renewed }

        val code = call(loggedStorage(), refreshApi, path = "/api/auth/login")

        assertEquals(401, code)
        assertEquals(0, refreshApi.refreshCalls)
    }

    @Test
    fun `se o 401 continuar mesmo com o token novo, desiste depois de uma tentativa`() {
        server.enqueue(MockResponse(code = 401))
        server.enqueue(MockResponse(code = 401))
        val refreshApi = FakeRefreshApi { renewed }

        val code = call(loggedStorage(), refreshApi)

        assertEquals(401, code)
        assertEquals(1, refreshApi.refreshCalls)
        assertEquals(2, server.requestCount)
    }

    @Test
    fun `varias requisicoes com 401 ao mesmo tempo renovam o token uma vez so`() {
        server.dispatcher =
            object : Dispatcher() {
                override fun dispatch(request: RecordedRequest): MockResponse {
                    val tokenNovo = request.headers["Authorization"] == "Bearer jwt-novo"
                    return MockResponse(code = if (tokenNovo) 200 else 401)
                }
            }
        val storage = loggedStorage()
        val refreshApi =
            FakeRefreshApi {
                Thread.sleep(REFRESH_DELAY_MS)
                renewed
            }
        val client =
            OkHttpClient
                .Builder()
                .addInterceptor(AuthInterceptor(storage, sessionManager))
                .authenticator(TokenAuthenticator(storage, refreshApi))
                .build()
        val start = CountDownLatch(1)
        val pool = Executors.newFixedThreadPool(PARALLEL_REQUESTS)

        val results =
            (1..PARALLEL_REQUESTS).map {
                pool.submit<Int> {
                    start.await()
                    client.newCall(Request.Builder().url(server.url("/api/kitchens")).build()).execute().use { it.code }
                }
            }
        start.countDown()
        val codes = results.map { it.get(TIMEOUT_SECONDS, TimeUnit.SECONDS) }
        pool.shutdown()

        assertEquals(List(PARALLEL_REQUESTS) { 200 }, codes)
        assertEquals(1, refreshApi.refreshCalls)
        assertEquals("refresh-novo", storage.currentRefreshToken)
    }

    private companion object {
        const val PARALLEL_REQUESTS = 3
        const val REFRESH_DELAY_MS = 200L
        const val TIMEOUT_SECONDS = 10L
    }
}
