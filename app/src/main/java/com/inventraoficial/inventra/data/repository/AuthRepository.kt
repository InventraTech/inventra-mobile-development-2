package com.inventraoficial.inventra.data.repository

import com.inventraoficial.inventra.data.local.SessionStorage
import com.inventraoficial.inventra.data.local.SessionUser
import com.inventraoficial.inventra.data.local.toSessionUser
import com.inventraoficial.inventra.data.remote.ApiException
import com.inventraoficial.inventra.data.remote.api.AuthApi
import com.inventraoficial.inventra.data.remote.api.UserApi
import com.inventraoficial.inventra.data.remote.apiCall
import com.inventraoficial.inventra.data.remote.dto.auth.AccessType
import com.inventraoficial.inventra.data.remote.dto.auth.GoogleLoginRequest
import com.inventraoficial.inventra.data.remote.dto.auth.LoginRequest
import com.inventraoficial.inventra.data.remote.dto.auth.LoginResponse
import com.inventraoficial.inventra.data.remote.dto.auth.RefreshTokenRequest
import com.inventraoficial.inventra.data.remote.dto.auth.RegisterRequest
import com.inventraoficial.inventra.data.remote.dto.user.UserResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

interface AuthRepository {
    val currentUser: Flow<SessionUser?>

    suspend fun login(
        email: String,
        password: String,
    ): Result<UserResponse>

    suspend fun register(
        name: String,
        email: String,
        password: String,
        accessType: AccessType,
    ): Result<UserResponse>

    suspend fun loginWithGoogle(
        idToken: String,
        accessType: AccessType? = null,
    ): Result<UserResponse>

    suspend fun hasSession(): Boolean

    suspend fun logout()

    suspend fun refreshCurrentUser(): Result<Unit>

    suspend fun updatePhoto(image: ByteArray): Result<Unit>

    suspend fun removePhoto(): Result<Unit>

    suspend fun leaveKitchen(): Result<Unit>
}

class NetworkAuthRepository(
    private val authApi: AuthApi,
    private val userApi: UserApi,
    private val json: Json,
    private val sessionStorage: SessionStorage,
    private val clearExternalSession: suspend () -> Unit = {},
) : AuthRepository {
    override val currentUser: Flow<SessionUser?> = sessionStorage.user

    private suspend fun authenticate(call: suspend () -> LoginResponse): Result<UserResponse> =
        apiCall(json) {
            val result = call()
            sessionStorage.saveSession(result.token, result.user.toSessionUser(), result.refreshToken)
            result.user
        }

    override suspend fun login(
        email: String,
        password: String,
    ): Result<UserResponse> =
        authenticate {
            authApi.login(LoginRequest(email, password))
        }

    override suspend fun register(
        name: String,
        email: String,
        password: String,
        accessType: AccessType,
    ): Result<UserResponse> =
        authenticate {
            authApi.register(RegisterRequest(name, email, password, accessType))
        }

    override suspend fun loginWithGoogle(
        idToken: String,
        accessType: AccessType?,
    ): Result<UserResponse> {
        val result = authenticate { authApi.loginWithGoogle(GoogleLoginRequest(idToken, accessType)) }
        val error = (result.exceptionOrNull() as? ApiException)?.error
        return if (error?.code == REGISTRATION_REQUIRED) {
            Result.failure(GoogleRegistrationRequiredException(error.name, error.email.orEmpty()))
        } else {
            result
        }
    }

    override suspend fun hasSession(): Boolean = sessionStorage.token.first() != null

    override suspend fun logout() {
        val refreshToken = sessionStorage.refreshToken.first()
        if (refreshToken != null) {
            apiCall(json) { authApi.logout(RefreshTokenRequest(refreshToken)) }
        }
        sessionStorage.clear()
        clearExternalSession()
    }

    override suspend fun refreshCurrentUser(): Result<Unit> =
        apiCall(json) {
            sessionStorage.updateUser(userApi.me().toSessionUser())
        }

    override suspend fun updatePhoto(image: ByteArray): Result<Unit> =
        apiCall(json) {
            val body = image.toRequestBody(JPEG.toMediaType())
            val part = MultipartBody.Part.createFormData(PHOTO_FIELD, PHOTO_FILE_NAME, body)
            sessionStorage.updateUser(userApi.updatePhoto(part).toSessionUser())
        }

    override suspend fun removePhoto(): Result<Unit> =
        apiCall(json) {
            sessionStorage.updateUser(userApi.removePhoto().toSessionUser())
        }

    override suspend fun leaveKitchen(): Result<Unit> =
        apiCall(json) {
            sessionStorage.updateUser(userApi.leaveKitchen().toSessionUser())
        }

    private companion object {
        const val REGISTRATION_REQUIRED = "REGISTRATION_REQUIRED"
        const val JPEG = "image/jpeg"
        const val PHOTO_FIELD = "file"
        const val PHOTO_FILE_NAME = "perfil.jpg"
    }
}

class GoogleRegistrationRequiredException(
    val name: String?,
    val email: String,
) : Exception("Escolha seu cargo para concluir o cadastro.")
