package com.example.myapp.dto.market.cart

data class CartItemResponse(
    val cartItemId: Long,
    val itemId: Long,
    val name: String,
    val price: Long,
    val quantity: Int,
    val subtotal: Long,
    val thumbnailUrl: String?
)
