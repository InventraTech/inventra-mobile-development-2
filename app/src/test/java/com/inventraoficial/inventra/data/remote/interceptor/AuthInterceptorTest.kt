package com.inventraoficial.inventra.data.remote.interceptor

import com.inventraoficial.inventra.data.local.FakeTokenStorage
import com.inventraoficial.inventra.data.session.SessionManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
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
@OptIn(ExperimentalCoroutinesApi::class)
class AuthInterceptorTest {
    private val server = MockWebServer()
    private val sessionManager = SessionManager()

    /** Comeca a ouvir os avisos de sessao expirada e devolve a lista que vai sendo preenchida. */
    private fun TestScope.ouvirAvisos(): List<Unit> {
        val avisos = mutableListOf<Unit>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            sessionManager.sessionExpired.toList(avisos)
        }
        return avisos
    }

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
        responseCode: Int = 200,
    ): RecordedRequest {
        server.enqueue(MockResponse(code = responseCode))
        val client = OkHttpClient.Builder().addInterceptor(AuthInterceptor(tokenStorage, sessionManager)).build()
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

    @Test
    fun `401 numa requisicao com token apaga o token salvo`() {
        val tokenStorage = FakeTokenStorage("jwt-expirado")

        send(tokenStorage, "/api/kitchens", responseCode = 401)

        assertNull(tokenStorage.currentToken)
    }

    @Test
    fun `401 no login nao apaga o token porque significa senha errada`() {
        val tokenStorage = FakeTokenStorage("jwt-valido")

        send(tokenStorage, "/api/auth/login", responseCode = 401)

        assertEquals("jwt-valido", tokenStorage.currentToken)
    }

    @Test
    fun `resposta de sucesso mantem o token`() {
        val tokenStorage = FakeTokenStorage("jwt-valido")

        send(tokenStorage, "/api/kitchens", responseCode = 200)

        assertEquals("jwt-valido", tokenStorage.currentToken)
    }

    @Test
    fun `outros erros como 403 nao apagam o token`() {
        val tokenStorage = FakeTokenStorage("jwt-valido")

        send(tokenStorage, "/api/kitchens", responseCode = 403)

        assertEquals("jwt-valido", tokenStorage.currentToken)
    }

    @Test
    fun `401 numa requisicao com token avisa que a sessao expirou`() =
        runTest {
            val avisos = ouvirAvisos()

            send(FakeTokenStorage("jwt-expirado"), "/api/kitchens", responseCode = 401)

            assertEquals(1, avisos.size)
        }

    @Test
    fun `401 no login nao avisa sessao expirada`() =
        runTest {
            val avisos = ouvirAvisos()

            send(FakeTokenStorage("jwt-valido"), "/api/auth/login", responseCode = 401)

            assertEquals(0, avisos.size)
        }

    @Test
    fun `resposta de sucesso nao avisa sessao expirada`() =
        runTest {
            val avisos = ouvirAvisos()

            send(FakeTokenStorage("jwt-valido"), "/api/kitchens", responseCode = 200)

            assertEquals(0, avisos.size)
        }
}
