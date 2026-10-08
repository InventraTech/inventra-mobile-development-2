package com.inventraoficial.inventra.data.remote.interceptor

import com.inventraoficial.inventra.data.local.FakeTokenStorage
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import mockwebserver3.RecordedRequest
import okhttp3.OkHttpClient
import okhttp3.Request
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

/**
 * Sobe um servidor HTTP falso local, faz a requisicao passar pelo
 * [AuthInterceptor] e confere quais headers chegaram no servidor.
 */
class AuthInterceptorTest {
    private val server = MockWebServer()

    @Before
    fun setUp() {
        server.start()
    }

    @After
    fun tearDown() {
        server.close()
    }

    private fun send(
        tokenStorage: FakeTokenStorage,
        path: String,
        existingAuthorization: String? = null,
    ): RecordedRequest {
        server.enqueue(MockResponse(code = 200))
        val client = OkHttpClient.Builder().addInterceptor(AuthInterceptor(tokenStorage)).build()
        val request =
            Request
                .Builder()
                .url(server.url(path))
                .apply { existingAuthorization?.let { header("Authorization", it) } }
                .build()

        client.newCall(request).execute().close()
        return server.takeRequest()
    }

    @Test
    fun `com token adiciona o header Authorization Bearer`() {
        val recorded = send(FakeTokenStorage("jwt-123"), "/api/kitchens")

        assertEquals("Bearer jwt-123", recorded.headers["Authorization"])
    }

    @Test
    fun `sem token nao adiciona o header`() {
        val recorded = send(FakeTokenStorage(null), "/api/kitchens")

        assertNull(recorded.headers["Authorization"])
    }

    @Test
    fun `rota de autenticacao nunca recebe o token`() {
        val recorded = send(FakeTokenStorage("token-velho"), "/api/auth/login")

        assertNull(recorded.headers["Authorization"])
    }

    @Test
    fun `substitui um Authorization que ja existia em vez de duplicar`() {
        val recorded = send(FakeTokenStorage("jwt-novo"), "/api/kitchens", existingAuthorization = "Bearer antigo")

        assertEquals(listOf("Bearer jwt-novo"), recorded.headers.values("Authorization"))
    }
}
