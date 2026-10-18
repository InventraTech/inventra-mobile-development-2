package com.inventraoficial.inventra.feature.cozinha.requestsent.ui

import com.inventraoficial.inventra.feature.cozinha.select.ui.CozinhaListItem

enum class RequestSentStatus { Pending, Rejected }

enum class RequestSentDestination { Home, SelectCozinha, Login }

data class RequestSentUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val cozinhaName: String = "",
    val status: RequestSentStatus = RequestSentStatus.Pending,
    val sentAt: String? = null,
    val expiresAt: String? = null,
    // "vence hoje", "vence amanhã", "vence em 5 dias"
    val expiresIn: String? = null,
    val rejectionReason: String? = null,
    // Outras cozinhas em que o usuário já pediu entrada, vindas da busca de cozinhas.
    val otherRequests: List<CozinhaListItem> = emptyList(),
    val isRefreshing: Boolean = false,
    val destination: RequestSentDestination? = null,
) {
    // Com um pedido pendente, o backend não aceita outro: dá para ver as cozinhas, mas não pedir.
    val canRequestAnother: Boolean
        get() = status == RequestSentStatus.Rejected
}
