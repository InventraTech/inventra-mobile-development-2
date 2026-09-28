package com.inventraoficial.inventra.feature.cozinha.select.ui

import com.inventraoficial.inventra.core.designsystem.molecules.InventraCozinhaRequestStatus

enum class CozinhaViewMode { List, Map }

data class CozinhaListItem(
    val name: String,
    val subtitle: String,
    val status: InventraCozinhaRequestStatus,
)

data class SelectCozinhaUiState(
    val searchQuery: String = "",
    val viewMode: CozinhaViewMode = CozinhaViewMode.List,
    val cozinhas: List<CozinhaListItem> = emptyList(),
)
