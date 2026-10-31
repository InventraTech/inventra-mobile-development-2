package com.inventraoficial.inventra.data.local

import android.content.ContentValues
import com.inventraoficial.inventra.data.local.InventraDatabase.Companion.KITCHEN_ID
import com.inventraoficial.inventra.data.local.InventraDatabase.Companion.PAYLOAD
import com.inventraoficial.inventra.data.local.InventraDatabase.Companion.POSITION
import com.inventraoficial.inventra.data.local.InventraDatabase.Companion.PRODUCT_ID
import com.inventraoficial.inventra.data.local.InventraDatabase.Companion.SAVED_AT
import com.inventraoficial.inventra.data.local.InventraDatabase.Companion.STOCK_TABLE
import com.inventraoficial.inventra.data.remote.dto.stock.KitchenStockItemResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

data class CachedKitchenStock(
    val items: List<KitchenStockItemResponse>,
    val savedAt: Long,
)

interface StockCache {
    suspend fun save(
        kitchenId: Int,
        items: List<KitchenStockItemResponse>,
        savedAt: Long,
    )

    suspend fun load(kitchenId: Int): CachedKitchenStock?
}

class SqliteStockCache(
    private val database: InventraDatabase,
    private val json: Json,
) : StockCache {
    override suspend fun save(
        kitchenId: Int,
        items: List<KitchenStockItemResponse>,
        savedAt: Long,
    ) = withContext(Dispatchers.IO) {
        val db = database.writableDatabase
        db.beginTransaction()
        try {
            db.delete(STOCK_TABLE, "$KITCHEN_ID = ?", arrayOf(kitchenId.toString()))
            items.forEachIndexed { index, item ->
                val row =
                    ContentValues().apply {
                        put(KITCHEN_ID, kitchenId)
                        put(PRODUCT_ID, item.product.id)
                        put(POSITION, index)
                        put(PAYLOAD, json.encodeToString(KitchenStockItemResponse.serializer(), item))
                        put(SAVED_AT, savedAt)
                    }
                db.insertOrThrow(STOCK_TABLE, null, row)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    override suspend fun load(kitchenId: Int): CachedKitchenStock? =
        withContext(Dispatchers.IO) {
            database.readableDatabase
                .query(
                    STOCK_TABLE,
                    arrayOf(PAYLOAD, SAVED_AT),
                    "$KITCHEN_ID = ?",
                    arrayOf(kitchenId.toString()),
                    null,
                    null,
                    POSITION,
                ).use { cursor ->
                    var savedAt: Long? = null
                    val items = mutableListOf<KitchenStockItemResponse>()
                    runCatching {
                        while (cursor.moveToNext()) {
                            items += json.decodeFromString(KitchenStockItemResponse.serializer(), cursor.getString(0))
                            savedAt = cursor.getLong(1)
                        }
                    }.getOrNull()?.let { savedAt }?.let { CachedKitchenStock(items, it) }
                }
        }
}
