package com.inventraoficial.inventra.data.remote.dto.chat

import kotlinx.serialization.Serializable

@Serializable
data class ChatRequest(
    val session_id: String,
    val pergunta: String,
    val user_id: String
)

@Serializable
data class ChatResponse(
    val resposta: String
)
