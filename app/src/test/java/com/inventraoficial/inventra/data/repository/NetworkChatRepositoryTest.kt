package com.inventraoficial.inventra.data.repository

import com.inventraoficial.inventra.data.local.FakeSessionStorage
import com.inventraoficial.inventra.data.local.SessionUser
import com.inventraoficial.inventra.data.remote.api.ChatApi
import com.inventraoficial.inventra.data.remote.api.UserApi
import com.inventraoficial.inventra.data.remote.dto.auth.AccessType
import com.inventraoficial.inventra.data.remote.dto.auth.ProfileSummary
import com.inventraoficial.inventra.data.remote.dto.user.UserResponse
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.lang.reflect.Proxy

class NetworkChatRepositoryTest {
    private val supervisor = usuario(id = "id-supervisor", nome = "Rollback Deployment", tipo = AccessType.SUPERVISOR)
    private val estoquista = usuario(id = "id-estoquista", nome = "Estoquista", tipo = AccessType.ESTOQUISTA)

    private var usuarioDoServidor: UserResponse = supervisor
    private var consultasAoServidor = 0

    private val userApi =
        Proxy.newProxyInstance(UserApi::class.java.classLoader, arrayOf(UserApi::class.java)) { _, metodo, _ ->
            check(metodo.name == "me") { "nao usado: ${metodo.name}" }
            consultasAoServidor++
            usuarioDoServidor
        } as UserApi

    private val chatApi =
        Proxy.newProxyInstance(ChatApi::class.java.classLoader, arrayOf(ChatApi::class.java)) { _, metodo, _ ->
            error("nao usado: ${metodo.name}")
        } as ChatApi

    private fun usuario(
        id: String,
        nome: String,
        tipo: AccessType,
    ) = UserResponse(
        id = id,
        name = nome,
        email = "$id@inventra.com",
        profile = ProfileSummary(id = 1, accessType = tipo),
        active = true,
        createdAt = "2026-01-01T00:00:00",
    )

    @Test
    fun `quem entra depois com outro token nao herda o usuario de quem saiu`() =
        runTest {
            val sessao = FakeSessionStorage(initialToken = "token-supervisor")
            val repositorio = NetworkChatRepository(chatApi, userApi, sessao)

            assertEquals("id-supervisor", repositorio.obterUsuarioId())
            assertEquals("Rollback Deployment", repositorio.obterNomeUsuario())

            // Logout e login do estoquista: outro token.
            usuarioDoServidor = estoquista
            sessao.saveSession(
                token = "token-estoquista",
                user = SessionUser(estoquista.id, estoquista.name, estoquista.email, AccessType.ESTOQUISTA),
                refreshToken = "refresh",
            )

            assertEquals("id-estoquista", repositorio.obterUsuarioId())
            assertEquals("Estoquista", repositorio.obterNomeUsuario())
        }

    @Test
    fun `com o mesmo token o usuario nao e buscado de novo`() =
        runTest {
            val sessao = FakeSessionStorage(initialToken = "token-supervisor")
            val repositorio = NetworkChatRepository(chatApi, userApi, sessao)

            repositorio.obterUsuarioId()
            repositorio.obterNomeUsuario()
            repositorio.obterUsuarioId()

            assertEquals(1, consultasAoServidor)
        }
}
