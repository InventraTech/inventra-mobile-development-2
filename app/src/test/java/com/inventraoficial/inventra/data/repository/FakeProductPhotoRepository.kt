package com.inventraoficial.inventra.data.repository

import com.inventraoficial.inventra.data.remote.dto.product.ProductResponse

class FakeProductPhotoRepository : ProductPhotoRepository {
    var error: Exception? = null
    var uploads = mutableListOf<Pair<Int, String>>()
        private set
    var removals = mutableListOf<Int>()
        private set

    override suspend fun update(
        productId: Int,
        imageUri: String,
    ): Result<ProductResponse> {
        uploads += productId to imageUri
        error?.let { return Result.failure(it) }
        return Result.success(
            FakeProductRepository.product(productId, "Produto").copy(photoUrl = "https://cdn/produto-$productId.jpg"),
        )
    }

    override suspend fun remove(productId: Int): Result<ProductResponse> {
        removals += productId
        error?.let { return Result.failure(it) }
        return Result.success(FakeProductRepository.product(productId, "Produto"))
    }
}
