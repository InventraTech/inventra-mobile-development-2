package com.inventraoficial.inventra.feature.suppliers.detail.ui

data class SupplierProduct(
    val name: String,
    val infoText: String,
)

data class DeliveryHistoryEntry(
    val date: String,
    val productName: String,
    val quantityLabel: String,
)

data class SupplierPhotoState(
    val url: String? = null,
    val canEdit: Boolean = false,
    val isSheetVisible: Boolean = false,
    val isUploading: Boolean = false,
    val errorMessage: String? = null,
)

data class SupplierPhotoActions(
    val onPhotoClick: () -> Unit = {},
    val onSheetDismiss: () -> Unit = {},
    val onPickClick: () -> Unit = {},
    val onCameraClick: () -> Unit = {},
    val onRemoveClick: () -> Unit = {},
)

data class SupplierDetailUiState(
    val photo: SupplierPhotoState = SupplierPhotoState(),
    val name: String = "",
    val rating: Int = 0,
    val badgeText: String = "",
    val phone: String? = null,
    val email: String? = null,
    val products: List<SupplierProduct> = emptyList(),
    val deliveryHistory: List<DeliveryHistoryEntry> = emptyList(),
    val showsDeliveries: Boolean = true,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)
