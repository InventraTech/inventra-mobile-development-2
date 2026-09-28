package com.inventraoficial.inventra.feature.cozinha.create.ui

enum class EstablishmentType(
    val label: String,
) {
    Matriz("Matriz"),
    Filial("Filial"),
    Restaurante("Restaurante"),
    CozinhaIndustrial("Cozinha industrial"),
    Padaria("Padaria"),
}

data class CreateCozinhaUiState(
    val name: String = "",
    val address: String = "",
    val establishmentType: EstablishmentType? = null,
    val errorMessage: String? = null,
)
