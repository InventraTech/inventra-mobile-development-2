package com.inventraoficial.inventra.feature.cozinha.info.ui

import com.inventraoficial.inventra.feature.cozinha.create.ui.EstablishmentType

data class CozinhaInfoUiState(
    val name: String = "",
    val address: String = "",
    val establishmentType: EstablishmentType? = null,
    val isEditing: Boolean = false,
)
