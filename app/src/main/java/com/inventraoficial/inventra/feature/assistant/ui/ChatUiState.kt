package com.inventraoficial.inventra.feature.assistant.ui

data class ChatUiState(
    val greeting: String = "Olá! 👋",
    val suggestions: List<String> =
        listOf(
            "Quais produtos vencem essa semana?",
            "Qual fornecedor tem a melhor nota?",
            "Quanto eu economizei esse mês?",
        ),
    val inputValue: String = "",
)
