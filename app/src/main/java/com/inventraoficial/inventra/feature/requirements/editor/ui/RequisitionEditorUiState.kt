package com.inventraoficial.inventra.feature.requirements.editor.ui

import com.inventraoficial.inventra.data.remote.dto.requisition.RequisitionType

data class RequisitionEditorUiState(
    val requisitionId: Int? = null,
    val type: RequisitionType = RequisitionType.PURCHASE,
    val origin: String = "",
    val items: List<EditorItemUi> = emptyList(),
    val totalText: String? = null,
    val itemForm: ItemForm? = null,
    val suppliers: List<SupplierOption> = emptyList(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val result: EditorResult? = null,
) {
    val isDraftCreated: Boolean
        get() = requisitionId != null

    val canSubmit: Boolean
        get() = items.isNotEmpty() && !isSaving
}

data class EditorItemUi(
    val id: Int,
    val name: String,
    val quantityText: String,
    val details: String?,
)

data class ItemForm(
    val query: String = "",
    val results: List<ProductOption> = emptyList(),
    val isSearching: Boolean = false,
    val product: ProductOption? = null,
    val quantity: String = "",
    val estimatedPrice: String = "",
    val supplierId: Int? = null,
    val note: String = "",
    val errorMessage: String? = null,
)

data class ProductOption(
    val id: Int,
    val name: String,
    val unitSymbol: String,
)

data class SupplierOption(
    val id: Int,
    val name: String,
)

enum class EditorResult(
    val message: String,
) {
    Submitted("Requisição enviada para análise."),
    Discarded("Rascunho descartado."),
}

data class RequisitionEditorActions(
    val onTypeSelect: (RequisitionType) -> Unit = {},
    val onOriginChange: (String) -> Unit = {},
    val onAddItemClick: () -> Unit = {},
    val onRemoveItemClick: (Int) -> Unit = {},
    val onSubmitClick: () -> Unit = {},
    val onDiscardClick: () -> Unit = {},
    val onRetryClick: () -> Unit = {},
    val onBackClick: () -> Unit = {},
)

data class ItemFormActions(
    val onQueryChange: (String) -> Unit = {},
    val onProductSelect: (ProductOption) -> Unit = {},
    val onQuantityChange: (String) -> Unit = {},
    val onPriceChange: (String) -> Unit = {},
    val onSupplierSelect: (Int?) -> Unit = {},
    val onNoteChange: (String) -> Unit = {},
    val onConfirm: () -> Unit = {},
    val onDismiss: () -> Unit = {},
)
