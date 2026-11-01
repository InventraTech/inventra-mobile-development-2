package com.inventraoficial.inventra.data.local

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import com.inventraoficial.inventra.data.local.InventraDatabase.Companion.KITCHEN_ID
import com.inventraoficial.inventra.data.local.InventraDatabase.Companion.PAYLOAD
import com.inventraoficial.inventra.data.local.InventraDatabase.Companion.PRODUCT_DETAIL_TABLE
import com.inventraoficial.inventra.data.local.InventraDatabase.Companion.PRODUCT_ID
import com.inventraoficial.inventra.data.local.InventraDatabase.Companion.SAVED_AT
import com.inventraoficial.inventra.data.remote.dto.product.ProductResponse
import com.inventraoficial.inventra.data.remote.dto.product.ProductSupplierResponse
import com.inventraoficial.inventra.data.remote.dto.stock.StockBatchResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class ProductDetailSnapshot(
    val product: ProductResponse,
    val batches: List<StockBatchResponse>,
    val suppliers: List<ProductSupplierResponse> = emptyList(),
)

data class CachedProductDetail(
    val snapshot: ProductDetailSnapshot,
    val savedAt: Long,
)

interface ProductDetailCache {
    suspend fun save(
        kitchenId: Int,
        snapshot: ProductDetailSnapshot,
        savedAt: Long,
    )

    suspend fun load(
        kitchenId: Int,
        productId: Int,
    ): CachedProductDetail?
}

class SqliteProductDetailCache(
    private val database: InventraDatabase,
    private val json: Json,
) : ProductDetailCache {
    override suspend fun save(
        kitchenId: Int,
        snapshot: ProductDetailSnapshot,
        savedAt: Long,
    ) {
        withContext(Dispatchers.IO) {
            val row =
                ContentValues().apply {
                    put(KITCHEN_ID, kitchenId)
                    put(PRODUCT_ID, snapshot.product.id)
                    put(PAYLOAD, json.encodeToString(ProductDetailSnapshot.serializer(), snapshot))
                    put(SAVED_AT, savedAt)
                }
            database.writableDatabase.insertWithOnConflict(
                PRODUCT_DETAIL_TABLE,
                null,
                row,
                SQLiteDatabase.CONFLICT_REPLACE,
            )
        }
    }

    override suspend fun load(
        kitchenId: Int,
        productId: Int,
    ): CachedProductDetail? =
        withContext(Dispatchers.IO) {
            database.readableDatabase
                .query(
                    PRODUCT_DETAIL_TABLE,
                    arrayOf(PAYLOAD, SAVED_AT),
                    "$KITCHEN_ID = ? AND $PRODUCT_ID = ?",
                    arrayOf(kitchenId.toString(), productId.toString()),
                    null,
                    null,
                    null,
                ).use { cursor ->
                    if (!cursor.moveToFirst()) return@use null
                    runCatching {
                        val snapshot = json.decodeFromString(ProductDetailSnapshot.serializer(), cursor.getString(0))
                        CachedProductDetail(snapshot, cursor.getLong(1))
                    }.getOrNull()
                }
        }
}
