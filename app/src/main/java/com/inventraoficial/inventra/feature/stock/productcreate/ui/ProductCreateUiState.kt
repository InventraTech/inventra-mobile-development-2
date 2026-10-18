package com.inventraoficial.inventra.feature.stock.productcreate.ui

import com.inventraoficial.inventra.core.designsystem.organisms.InventraPickerOption

data class SavedProduct(
    val id: Int,
    val name: String,
)

data class ProductCreateUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    // O código já está no catálogo (alguém cadastrou antes): não deixa cadastrar de novo.
    val existingProduct: SavedProduct? = null,
    val isPrefilled: Boolean = false,
    val name: String = "",
    val brand: String = "",
    val barcode: String = "",
    val units: List<InventraPickerOption> = emptyList(),
    val unitId: Int? = null,
    val categories: List<InventraPickerOption> = emptyList(),
    val categoryId: Int? = null,
    val isSubmitting: Boolean = false,
    val submitError: String? = null,
    val createdProduct: SavedProduct? = null,
) {
    val unitLabel: String?
        get() = units.firstOrNull { it.id == unitId }?.label()

    val categoryName: String?
        get() = categories.firstOrNull { it.id == categoryId }?.title
}

private fun InventraPickerOption.label(): String = subtitle?.let { "$title ($it)" } ?: title

@Suppress("LongParameterList")
data class ProductCreateActions(
    val onNameChange: (String) -> Unit,
    val onBrandChange: (String) -> Unit,
    val onBarcodeChange: (String) -> Unit,
    val onUnitSelect: (Int?) -> Unit,
    val onCategorySelect: (Int?) -> Unit,
    val onSaveClick: () -> Unit,
    val onRetryClick: () -> Unit,
    val onBackClick: () -> Unit,
    val onStockEntryClick: (Int) -> Unit,
    val onSeeProductClick: (Int) -> Unit,
)
