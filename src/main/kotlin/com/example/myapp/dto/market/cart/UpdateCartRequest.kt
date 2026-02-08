package com.example.myapp.dto.market.cart

/**
 * カート商品数量更新リクエストDTO
 *
 * @property quantity 新しい数量
 * Used in: [com.example.myapp.controller.market.cart.UpdateCartItemController]
 */

data class UpdateCartRequest(
    val quantity: Int
)
