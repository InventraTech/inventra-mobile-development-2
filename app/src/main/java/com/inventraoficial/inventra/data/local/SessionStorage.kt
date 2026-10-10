package com.inventraoficial.inventra.data.local

import kotlinx.coroutines.flow.Flow

interface SessionStorage {
    val token: Flow<String?>
    val user: Flow<SessionUser?>
    val refreshToken: Flow<String?>

    suspend fun saveSession(
        token: String,
        user: SessionUser,
        refreshToken: String,
    )

    suspend fun updateUser(user: SessionUser)

    suspend fun clear()
}
