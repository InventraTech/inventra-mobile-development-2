package com.inventraoficial.inventra.feature.prelist.detail.ui

data class PreListDetailUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val dateText: String = "",
    val summaryText: String = "",
    val groups: List<PreListSupplierGroup> = emptyList(),
    val isConverting: Boolean = false,
    val convertErrorMessage: String? = null,
    val openedDraftId: Int? = null,
)

data class PreListSupplierGroup(
    val supplierName: String,
    val items: List<PreListItemUi>,
)

data class PreListItemUi(
    val id: Int,
    val name: String,
    val photoUrl: String?,
    val quantityText: String,
)
