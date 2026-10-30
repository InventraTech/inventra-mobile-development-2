package com.inventraoficial.inventra.feature.stock.edit.ui

import com.inventraoficial.inventra.feature.stock.productform.ProductFormActions
import com.inventraoficial.inventra.feature.stock.productform.ProductFormState
import com.inventraoficial.inventra.feature.stock.productform.ProductPhotoActions
import com.inventraoficial.inventra.feature.stock.productform.ProductPhotoState

data class ProductEditUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val form: ProductFormState = ProductFormState(),
    val isSubmitting: Boolean = false,
    val submitError: String? = null,
    val isSaved: Boolean = false,
    val photo: ProductPhotoState = ProductPhotoState(),
)

data class ProductEditActions(
    val form: ProductFormActions,
    val photo: ProductPhotoActions = ProductPhotoActions(),
    val onSaveClick: () -> Unit,
    val onRetryClick: () -> Unit,
    val onBackClick: () -> Unit,
)
