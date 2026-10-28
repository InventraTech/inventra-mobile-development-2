package com.inventraoficial.inventra.feature.stock.edit.ui

import com.inventraoficial.inventra.feature.stock.productform.ProductFormActions
import com.inventraoficial.inventra.feature.stock.productform.ProductFormState

data class ProductEditUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val form: ProductFormState = ProductFormState(),
    val isSubmitting: Boolean = false,
    val submitError: String? = null,
    val isSaved: Boolean = false,
    val photo: ProductPhotoState = ProductPhotoState(),
)

data class ProductPhotoState(
    val url: String? = null,
    val isSheetVisible: Boolean = false,
    val isUploading: Boolean = false,
    val errorMessage: String? = null,
)

data class ProductEditActions(
    val form: ProductFormActions,
    val photo: ProductPhotoActions = ProductPhotoActions(),
    val onSaveClick: () -> Unit,
    val onRetryClick: () -> Unit,
    val onBackClick: () -> Unit,
)

data class ProductPhotoActions(
    val onPhotoClick: () -> Unit = {},
    val onSheetDismiss: () -> Unit = {},
    val onPickClick: () -> Unit = {},
    val onRemoveClick: () -> Unit = {},
)
