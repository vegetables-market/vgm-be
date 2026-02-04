package com.example.myapp.dto.market

data class ItemDetailResponse(
    val item: ItemDetail,
    val relatedItems: List<ItemResponse>
)

data class ItemDetail(
    val itemId: Long,
    val title: String,
    val description: String?,
    val price: Int,
    val quantity: Int,
    val categoryId: Long?,
    val categoryName: String?,
    val condition: Int,
    val status: Short,
    val likesCount: Int,
    val isLiked: Boolean,
    val brand: String?,
    val weight: Int?,
    val shippingPayerType: Int,
    val images: List<ItemImageInfo>,
    val seller: SellerDetailInfo,
    val createdAt: String,
    val updatedAt: String
)

data class ItemImageInfo(
    val imageId: Long,
    val imageUrl: String,
    val displayOrder: Int
)

data class SellerDetailInfo(
    val userId: Int,
    val username: String,
    val displayName: String,
    val avatarUrl: String?,
    val ratingAverage: Double?,
    val ratingCount: Int
)
