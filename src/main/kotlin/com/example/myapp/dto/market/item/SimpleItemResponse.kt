package com.example.myapp.dto.market.item

data class SimpleItemResponse(
    val id: Long,
    val name: String?,
    val price: Int?,
    val status: Int,
    val imageUrl: String?, // 1枚目
    val createdAt: String
)
