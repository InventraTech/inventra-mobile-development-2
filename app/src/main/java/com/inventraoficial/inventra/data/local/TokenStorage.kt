package com.inventraoficial.inventra.data.local

import kotlinx.coroutines.flow.Flow

interface TokenStorage {
    val token: Flow<String?>

    suspend fun saveToken(token: String)

    suspend fun clear()
}
