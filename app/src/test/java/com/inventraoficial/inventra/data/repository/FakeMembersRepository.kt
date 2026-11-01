package com.inventraoficial.inventra.data.repository

import com.inventraoficial.inventra.data.remote.dto.auth.AccessType
import com.inventraoficial.inventra.data.remote.dto.auth.KitchenSummary
import com.inventraoficial.inventra.data.remote.dto.auth.ProfileSummary
import com.inventraoficial.inventra.data.remote.dto.kitchenaccess.AccessRequestUser
import com.inventraoficial.inventra.data.remote.dto.kitchenaccess.KitchenAccessRequestResponse
import com.inventraoficial.inventra.data.remote.dto.kitchenaccess.KitchenAccessRequestStatus
import com.inventraoficial.inventra.data.remote.dto.user.UserResponse

class FakeMembersRepository(
    var pending: MutableList<KitchenAccessRequestResponse> = mutableListOf(pendingRequest),
    var members: MutableList<UserResponse> = mutableListOf(supervisor),
) : MembersRepository {
    var pendingError: Exception? = null
    var approveError: Exception? = null
    var approvedIds = mutableListOf<String>()
        private set
    var rejected = mutableListOf<Pair<String, String?>>()
        private set
    var deactivateError: Exception? = null
    var deactivatedIds = mutableListOf<String>()
        private set

    override suspend fun pendingRequests(): Result<List<KitchenAccessRequestResponse>> =
        pendingError?.let { Result.failure(it) } ?: Result.success(pending.toList())

    var membersError: Exception? = null

    override suspend fun members(): Result<List<UserResponse>> {
        membersError?.let { return Result.failure(it) }
        return Result.success(members.toList())
    }

    override suspend fun approve(requestId: String): Result<KitchenAccessRequestResponse> {
        approveError?.let { return Result.failure(it) }
        approvedIds += requestId
        val request = pending.first { it.id == requestId }
        pending.remove(request)
        members.add(estoquista(request.user))
        return Result.success(request.copy(status = KitchenAccessRequestStatus.APPROVED))
    }

    override suspend fun reject(
        requestId: String,
        reason: String?,
    ): Result<KitchenAccessRequestResponse> {
        rejected += requestId to reason
        val request = pending.first { it.id == requestId }
        pending.remove(request)
        return Result.success(request.copy(status = KitchenAccessRequestStatus.REJECTED, reason = reason))
    }

    override suspend fun deactivate(userId: String): Result<Unit> {
        deactivateError?.let { return Result.failure(it) }
        deactivatedIds += userId
        members.replaceAll { if (it.id == userId) it.copy(active = false) else it }
        return Result.success(Unit)
    }

    companion object {
        val supervisor =
            UserResponse(
                id = "sup-1",
                name = "Jones Silva",
                email = "jones@inventra.com",
                profile = ProfileSummary(id = 1, accessType = AccessType.SUPERVISOR),
                active = true,
                createdAt = "2026-10-10T00:00:00Z",
            )

        val pendingRequest =
            KitchenAccessRequestResponse(
                id = "req-1",
                status = KitchenAccessRequestStatus.PENDING,
                kitchen = KitchenSummary(id = 7, name = "Sabor Filial Sul"),
                user = AccessRequestUser(id = "est-1", name = "Mauro Rosa", email = "mauro@inventra.com"),
                createdAt = "2026-10-10T00:00:00Z",
            )

        fun estoquista(user: AccessRequestUser) =
            UserResponse(
                id = user.id,
                name = user.name,
                email = user.email,
                profile = ProfileSummary(id = 2, accessType = AccessType.ESTOQUISTA),
                active = true,
                createdAt = "2026-10-10T00:00:00Z",
            )
    }
}
