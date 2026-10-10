package com.inventraoficial.inventra.data.remote.interceptor

import com.inventraoficial.inventra.data.local.SessionStorage
import com.inventraoficial.inventra.data.session.SessionManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import java.net.HttpURLConnection

class AuthInterceptor(
    private val sessionStorage: SessionStorage,
    private val sessionManager: SessionManager,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = runBlocking { sessionStorage.token.first() }
        val request = chain.request()

        if (token == null || request.url.encodedPath.startsWith("/api/auth/")) return chain.proceed(request)

        val authenticationRequest =
            request
                .newBuilder()
                .header("Authorization", "Bearer $token")
                .build()

        val response = chain.proceed(authenticationRequest)
        if (response.code == HttpURLConnection.HTTP_UNAUTHORIZED) {
            runBlocking { sessionStorage.clear() }
            sessionManager.notifySessionExpired()
        }
        return response
    }
}
