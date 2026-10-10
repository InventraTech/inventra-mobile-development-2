package com.inventraoficial.inventra.data.local

import kotlinx.coroutines.flow.Flow

interface SessionStorage {
    val token: Flow<String?>
    val user: Flow<SessionUser?>

    suspend fun saveSession(
        token: String,
        user: SessionUser,
    )

    suspend fun updateUser(user: SessionUser)

    suspend fun clear()
}
