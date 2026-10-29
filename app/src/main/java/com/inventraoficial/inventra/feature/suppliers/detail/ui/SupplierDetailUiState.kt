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

data class SupplierDetailUiState(
    val photoUrl: String? = null,
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
