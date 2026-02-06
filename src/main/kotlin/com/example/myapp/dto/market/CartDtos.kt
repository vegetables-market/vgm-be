package com.example.myapp.dto.market

import com.fasterxml.jackson.annotation.JsonProperty

data class CartResponse(
    val items: List<CartItemResponse>,
    @JsonProperty("total_amount")
    val totalAmount: Long
)

data class CartItemResponse(
    @JsonProperty("cart_item_id")
    val cartItemId: Long,
    
    @JsonProperty("item_id")
    val itemId: Long,
    
    val name: String,
    val price: Long,
    val quantity: Int,
    val subtotal: Long,
    
    @JsonProperty("thumbnail_url")
    val thumbnailUrl: String?
)

data class AddCartRequest(
    @JsonProperty("item_id")
    val itemId: Long,
    
    val quantity: Int = 1
)

data class UpdateCartRequest(
    val quantity: Int
)
