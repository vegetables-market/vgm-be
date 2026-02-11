package com.example.myapp.dto.market.cart

/**
 * カート追加リクエストDTO
 *
 * @property itemId 商品ID
 * @property quantity 数量
 * Used in: [com.example.myapp.controller.market.cart.AddToCartController]
 */

data class AddCartRequest(
    val itemId: String,
    val quantity: Int = 1
)
