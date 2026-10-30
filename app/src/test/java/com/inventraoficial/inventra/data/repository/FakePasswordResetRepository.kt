package com.inventraoficial.inventra.data.repository

class FakePasswordResetRepository : PasswordResetRepository {
    var requestCodeResult: Result<Unit> = Result.success(Unit)
    var codeRequests = mutableListOf<String>()
        private set

    override suspend fun requestCode(email: String): Result<Unit> {
        codeRequests += email
        return requestCodeResult
    }

    var verifyResult: Result<String> = Result.success("reset-token")
    var lastEmail: String? = null
        private set
    var lastCode: String? = null
        private set

    override suspend fun verifyCode(
        email: String,
        code: String,
    ): Result<String> {
        lastEmail = email
        lastCode = code
        return verifyResult
    }

    var resetPasswordResult: Result<Unit> = Result.success(Unit)
    var lastResetToken: String? = null
        private set
    var lastNewPassword: String? = null
        private set

    override suspend fun resetPassword(
        resetToken: String,
        newPassword: String,
    ): Result<Unit> {
        lastResetToken = resetToken
        lastNewPassword = newPassword
        return resetPasswordResult
    }
}
