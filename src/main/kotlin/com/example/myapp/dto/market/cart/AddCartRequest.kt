package com.example.myapp.dto.market.cart

data class AddCartRequest(
    val itemId: Long,
    val quantity: Int = 1
)
