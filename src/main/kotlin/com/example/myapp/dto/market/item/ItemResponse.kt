package com.example.myapp.dto.market.item

data class ItemResponse(
    val itemId: Long,
    val title: String,
    val description: String?,
    val price: Int,
    val categoryId: Long?,
    val categoryName: String?,
    val condition: Int,
    val status: Short,
    val likesCount: Int,
    val thumbnailUrl: String?,
    val seller: SellerInfo,
    val createdAt: String
)
