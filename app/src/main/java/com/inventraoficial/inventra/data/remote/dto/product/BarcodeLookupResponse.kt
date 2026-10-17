package com.inventraoficial.inventra.data.remote.dto.product

import kotlinx.serialization.Serializable

// source = CATALOG traz product (e kitchenStock quando a consulta informa a cozinha);
// source = OPEN_FOOD_FACTS traz só a suggestion da base pública.
@Serializable
data class BarcodeLookupResponse(
    val source: BarcodeSource,
    val barcode: String,
    val product: ProductResponse? = null,
    val kitchenStock: BarcodeKitchenStock? = null,
    val suggestion: OpenFoodFactsSuggestion? = null,
)

@Serializable
enum class BarcodeSource {
    CATALOG,
    OPEN_FOOD_FACTS,
}

@Serializable
data class BarcodeKitchenStock(
    val totalQuantity: Double,
    val activeBatches: Int,
    val nextExpirationDate: String? = null,
)

@Serializable
data class OpenFoodFactsSuggestion(
    val name: String? = null,
    val brand: String? = null,
    val imageUrl: String? = null,
    val quantity: String? = null,
)
