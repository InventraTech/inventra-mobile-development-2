package com.inventraoficial.inventra.data.repository

import com.inventraoficial.inventra.data.local.SessionUser
import com.inventraoficial.inventra.data.remote.dto.auth.AccessType
import com.inventraoficial.inventra.data.remote.dto.auth.ProfileSummary
import com.inventraoficial.inventra.data.remote.dto.user.UserResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeAuthRepository(
    var result: Result<UserResponse> = Result.success(fakeUser),
    var hasSession: Boolean = false,
    initialUser: SessionUser? = null,
) : AuthRepository {
    private val _currentUser = MutableStateFlow(initialUser)
    override val currentUser: Flow<SessionUser?> = _currentUser

    fun emitUser(user: SessionUser?) {
        _currentUser.value = user
    }

    var loginCalls = 0
        private set
    var lastEmail: String? = null
        private set
    var lastPassword: String? = null
        private set
    var registerCalls = 0
        private set
    var lastName: String? = null
        private set
    var lastAccessType: AccessType? = null
        private set

    override suspend fun login(
        email: String,
        password: String,
    ): Result<UserResponse> {
        loginCalls++
        lastEmail = email
        lastPassword = password
        return result
    }

    override suspend fun register(
        name: String,
        email: String,
        password: String,
        accessType: AccessType,
    ): Result<UserResponse> {
        registerCalls++
        lastName = name
        lastEmail = email
        lastPassword = password
        lastAccessType = accessType
        return result
    }

    var googleCalls = 0
        private set
    var lastIdToken: String? = null
        private set

    override suspend fun loginWithGoogle(
        idToken: String,
        accessType: AccessType?,
    ): Result<UserResponse> {
        googleCalls++
        lastIdToken = idToken
        lastAccessType = accessType
        return result
    }

    override suspend fun hasSession(): Boolean = hasSession

    var refreshCalls = 0
        private set
    var refreshResult: Result<Unit> = Result.success(Unit)

    override suspend fun refreshCurrentUser(): Result<Unit> {
        refreshCalls++
        return refreshResult
    }

    var photoResult: Result<Unit> = Result.success(Unit)
    var uploadedPhotos = mutableListOf<ByteArray>()
        private set
    var removePhotoCalls = 0
        private set

    override suspend fun updatePhoto(image: ByteArray): Result<Unit> {
        uploadedPhotos += image
        if (photoResult.isSuccess) _currentUser.value = _currentUser.value?.copy(photoUrl = UPLOADED_PHOTO_URL)
        return photoResult
    }

    override suspend fun removePhoto(): Result<Unit> {
        removePhotoCalls++
        if (photoResult.isSuccess) _currentUser.value = _currentUser.value?.copy(photoUrl = null)
        return photoResult
    }

    var leaveKitchenResult: Result<Unit> = Result.success(Unit)
    var leaveKitchenCalls = 0
        private set

    override suspend fun leaveKitchen(): Result<Unit> {
        leaveKitchenCalls++
        if (leaveKitchenResult.isSuccess) {
            _currentUser.value = _currentUser.value?.copy(kitchenId = null, kitchenName = null)
        }
        return leaveKitchenResult
    }

    var logoutCalls = 0
        private set

    override suspend fun logout() {
        logoutCalls++
        hasSession = false
        _currentUser.value = null
    }

    companion object {
        const val UPLOADED_PHOTO_URL = "https://res.cloudinary.com/inventra/image/upload/v2/inventra/users/1.jpg"

        val fakeUser =
            UserResponse(
                id = "00000000-0000-0000-0000-000000000001",
                name = "Usuario Teste",
                email = "teste@inventra.com",
                profile = ProfileSummary(id = 1, accessType = AccessType.SUPERVISOR),
                active = true,
                createdAt = "2026-10-03T00:00:00Z",
            )
    }
}
