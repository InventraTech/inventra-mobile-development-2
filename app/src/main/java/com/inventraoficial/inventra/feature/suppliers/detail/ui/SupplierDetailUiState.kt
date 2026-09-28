package com.inventraoficial.inventra.feature.suppliers.detail.ui

import com.inventraoficial.inventra.R

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
    val imageRes: Int = R.drawable.ic_inventra_logo,
    val name: String = "",
    val rating: Int = 0,
    val badgeText: String = "",
    val phone: String = "",
    val email: String = "",
    val address: String = "",
    val products: List<SupplierProduct> = emptyList(),
    val deliveryHistory: List<DeliveryHistoryEntry> = emptyList(),
)
