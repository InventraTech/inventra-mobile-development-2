package com.inventraoficial.inventra.data.remote.interceptor

import com.inventraoficial.inventra.data.local.SessionStorage
import com.inventraoficial.inventra.data.local.toSessionUser
import com.inventraoficial.inventra.data.remote.api.AuthApi
import com.inventraoficial.inventra.data.remote.dto.auth.RefreshTokenRequest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.SerializationException
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import retrofit2.HttpException
import java.io.IOException

class TokenAuthenticator(
    private val sessionStorage: SessionStorage,
    private val refreshApi: AuthApi,
) : Authenticator {
    private val lock = Any()

    override fun authenticate(
        route: Route?,
        response: Response,
    ): Request? {
        if (!canTryRefresh(response)) return null
        val newToken = synchronized(lock) { runBlocking { validTokenFor(response.request) } }
        return newToken?.let { token ->
            response.request
                .newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        }
    }

    private fun canTryRefresh(response: Response): Boolean {
        val isAuthRoute =
            response.request.url.encodedPath
                .startsWith("/api/auth/")
        val alreadyRetried = response.priorResponse != null
        return !isAuthRoute && !alreadyRetried
    }

    private suspend fun refreshTokens(): String? {
        val refreshToken = sessionStorage.refreshToken.first() ?: return null
        return try {
            val renewed = refreshApi.refresh(RefreshTokenRequest(refreshToken))
            sessionStorage.saveSession(renewed.token, renewed.user.toSessionUser(), renewed.refreshToken)
            renewed.token
        } catch (ignored: HttpException) {
            null
        } catch (ignored: IOException) {
            null
        } catch (ignored: SerializationException) {
            null
        }
    }

    private suspend fun validTokenFor(failedRequest: Request): String? {
        val currentToken = sessionStorage.token.first()
        val usedToken = failedRequest.header("Authorization")?.removePrefix("Bearer ")
        return if (currentToken != null && currentToken != usedToken) currentToken else refreshTokens()
    }
}
