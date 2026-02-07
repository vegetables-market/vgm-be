package com.example.myapp.dto.market.cart

data class CartResponse(
    val items: List<CartItemResponse>,
    val totalAmount: Long
)
