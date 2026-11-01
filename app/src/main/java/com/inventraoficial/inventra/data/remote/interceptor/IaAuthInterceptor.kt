package com.inventraoficial.inventra.data.remote.interceptor

import com.inventraoficial.inventra.data.local.SessionStorage
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Anexa o mesmo JWT do login do app às chamadas da IA (ela valida o token no Spring).
 * Diferente do AuthInterceptor principal, um 401 da IA não derruba a sessão do app.
 */
class IaAuthInterceptor(
    private val sessionStorage: SessionStorage,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = runBlocking { sessionStorage.token.first() }
        val request = chain.request()
        if (token == null) return chain.proceed(request)
        return chain.proceed(request.newBuilder().header("Authorization", "Bearer $token").build())
    }
}
