package com.inventraoficial.inventra.data.local

import com.inventraoficial.inventra.data.remote.dto.stock.KitchenStockItemResponse

class FakeStockCache : StockCache {
    val saved = mutableMapOf<Int, CachedKitchenStock>()

    override suspend fun save(
        kitchenId: Int,
        items: List<KitchenStockItemResponse>,
        savedAt: Long,
    ) {
        saved[kitchenId] = CachedKitchenStock(items, savedAt)
    }

    override suspend fun load(kitchenId: Int): CachedKitchenStock? = saved[kitchenId]
}
