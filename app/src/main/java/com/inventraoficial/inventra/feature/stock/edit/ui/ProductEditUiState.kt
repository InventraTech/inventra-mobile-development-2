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
)

data class ProductEditActions(
    val form: ProductFormActions,
    val onSaveClick: () -> Unit,
    val onRetryClick: () -> Unit,
    val onBackClick: () -> Unit,
)
