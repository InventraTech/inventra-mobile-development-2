package com.inventraoficial.inventra.feature.stock.productcreate.ui

import com.inventraoficial.inventra.feature.stock.productform.ProductFormActions
import com.inventraoficial.inventra.feature.stock.productform.ProductFormState
import com.inventraoficial.inventra.feature.stock.productform.ProductPhotoActions
import com.inventraoficial.inventra.feature.stock.productform.ProductPhotoState

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
    val form: ProductFormState = ProductFormState(),
    val photo: ProductPhotoState = ProductPhotoState(),
    val isSubmitting: Boolean = false,
    val submitError: String? = null,
    val createdProduct: SavedProduct? = null,
    val kitchenLinkFailed: Boolean = false,
    val photoUploadFailed: Boolean = false,
)

data class ProductCreateActions(
    val form: ProductFormActions,
    val photo: ProductPhotoActions = ProductPhotoActions(),
    val onSaveClick: () -> Unit,
    val onRetryClick: () -> Unit,
    val onBackClick: () -> Unit,
    val onStockEntryClick: (Int) -> Unit,
    val onSeeProductClick: (Int) -> Unit,
)
