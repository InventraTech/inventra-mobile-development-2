package com.inventraoficial.inventra.data.repository

import com.inventraoficial.inventra.data.remote.ApiException
import com.inventraoficial.inventra.data.remote.api.AuthApi
import com.inventraoficial.inventra.data.remote.dto.auth.ForgotPasswordRequest
import com.inventraoficial.inventra.data.remote.dto.auth.GoogleLoginRequest
import com.inventraoficial.inventra.data.remote.dto.auth.LoginRequest
import com.inventraoficial.inventra.data.remote.dto.auth.LoginResponse
import com.inventraoficial.inventra.data.remote.dto.auth.RefreshTokenRequest
import com.inventraoficial.inventra.data.remote.dto.auth.RegisterRequest
import com.inventraoficial.inventra.data.remote.dto.auth.ResetPasswordRequest
import com.inventraoficial.inventra.data.remote.dto.auth.ResetTokenResponse
import com.inventraoficial.inventra.data.remote.dto.auth.VerifyResetCodeRequest
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

class NetworkPasswordResetRepositoryTest {
    private val json = Json { ignoreUnknownKeys = true }

    private class FakePasswordResetApi : AuthApi {
        var lastForgotRequest: ForgotPasswordRequest? = null
            private set
        var lastVerifyRequest: VerifyResetCodeRequest? = null
            private set
        var lastResetRequest: ResetPasswordRequest? = null
            private set
        var error: Exception? = null

        override suspend fun forgotPassword(forgotPasswordRequest: ForgotPasswordRequest) {
            lastForgotRequest = forgotPasswordRequest
            error?.let { throw it }
        }

        override suspend fun verifyResetCode(verifyResetCodeRequest: VerifyResetCodeRequest): ResetTokenResponse {
            lastVerifyRequest = verifyResetCodeRequest
            error?.let { throw it }
            return ResetTokenResponse(resetToken = "reset-token", expiresInSeconds = 900)
        }

        override suspend fun resetPassword(resetPasswordRequest: ResetPasswordRequest) {
            lastResetRequest = resetPasswordRequest
            error?.let { throw it }
        }

        override suspend fun login(loginRequest: LoginRequest): LoginResponse = error("nao usado")

        override suspend fun register(registerRequest: RegisterRequest): LoginResponse = error("nao usado")

        override suspend fun loginWithGoogle(googleLoginRequest: GoogleLoginRequest): LoginResponse = error("nao usado")

        override suspend fun refresh(refreshRequest: RefreshTokenRequest): LoginResponse = error("nao usado")

        override suspend fun logout(refreshRequest: RefreshTokenRequest) = Unit
    }

    private val api = FakePasswordResetApi()
    private val repository = NetworkPasswordResetRepository(api, json)

    private fun httpError(body: String): HttpException =
        HttpException(Response.error<Any>(400, body.toResponseBody("application/json".toMediaType())))

    @Test
    fun `pedir o codigo manda o e-mail`() =
        runTest {
            val result = repository.requestCode("maria@inventra.com")

            assertTrue(result.isSuccess)
            assertEquals(ForgotPasswordRequest("maria@inventra.com"), api.lastForgotRequest)
        }

    @Test
    fun `confirmar o codigo devolve o token de redefinicao`() =
        runTest {
            val result = repository.verifyCode("maria@inventra.com", "482915")

            assertEquals("reset-token", result.getOrNull())
            assertEquals(VerifyResetCodeRequest("maria@inventra.com", "482915"), api.lastVerifyRequest)
        }

    @Test
    fun `codigo errado usa a mensagem do backend`() =
        runTest {
            api.error = httpError("""{"status":400,"detail":"Código inválido.","code":"INVALID_RESET_CODE"}""")

            val result = repository.verifyCode("maria@inventra.com", "000000")

            assertEquals("Código inválido.", result.exceptionOrNull()?.message)
        }

    @Test
    fun `salvar a nova senha manda o token e a senha`() =
        runTest {
            val result = repository.resetPassword("reset-token", "NovaSenha@456")

            assertTrue(result.isSuccess)
            assertEquals(ResetPasswordRequest("reset-token", "NovaSenha@456"), api.lastResetRequest)
        }

    @Test
    fun `token vencido vira ResetTokenExpiredException com a mensagem do backend`() =
        runTest {
            api.error = httpError("""{"status":400,"detail":"Peça um novo.","code":"INVALID_RESET_TOKEN"}""")

            val error = repository.resetPassword("vencido", "NovaSenha@456").exceptionOrNull()

            assertTrue(error is ResetTokenExpiredException)
            assertEquals("Peça um novo.", error?.message)
        }

    @Test
    fun `outro erro ao salvar a senha continua como erro da API`() =
        runTest {
            api.error = httpError("""{"status":400,"detail":"Dados inválidos."}""")

            val error = repository.resetPassword("reset-token", "curta").exceptionOrNull()

            assertTrue(error is ApiException)
            assertEquals("Dados inválidos.", error?.message)
        }
}
