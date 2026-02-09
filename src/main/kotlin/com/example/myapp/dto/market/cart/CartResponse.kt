package com.example.myapp.dto.market.cart

/**
 * カート情報レスポンスDTO
 *
 * @property items カート内商品リスト
 * @property totalAmount 合計金額
 * Used in: [com.example.myapp.controller.market.cart.GetCartController]
 */

data class CartResponse(
    val items: List<CartItemResponse>,
    val totalAmount: Long
)
