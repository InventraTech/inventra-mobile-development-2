package com.inventraoficial.inventra.data.local

class FakeProductDetailCache : ProductDetailCache {
    val saved = mutableMapOf<Pair<Int, Int>, CachedProductDetail>()

    override suspend fun save(
        kitchenId: Int,
        snapshot: ProductDetailSnapshot,
        savedAt: Long,
    ) {
        saved[kitchenId to snapshot.product.id] = CachedProductDetail(snapshot, savedAt)
    }

    override suspend fun load(
        kitchenId: Int,
        productId: Int,
    ): CachedProductDetail? = saved[kitchenId to productId]
}
