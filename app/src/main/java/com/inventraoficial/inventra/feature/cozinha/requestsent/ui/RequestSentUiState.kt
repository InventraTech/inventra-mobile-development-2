package com.inventraoficial.inventra.feature.cozinha.requestsent.ui

enum class RequestSentStatus { Pending, Rejected }

enum class RequestSentDestination { Home, SelectCozinha, Login }

data class RequestSentUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val cozinhaName: String = "",
    val status: RequestSentStatus = RequestSentStatus.Pending,
    val expiresAt: String? = null,
    val rejectionReason: String? = null,
    val isRefreshing: Boolean = false,
    val destination: RequestSentDestination? = null,
)
