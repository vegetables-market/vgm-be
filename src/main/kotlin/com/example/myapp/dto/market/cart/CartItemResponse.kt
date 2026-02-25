package com.example.myapp.dto.market.cart

/**
 * カート内商品情報レスポンスDTO
 *
 * @property cartItemId カート商品ID
 * @property itemId 商品ID
 * @property name 商品名
 * @property price 価格
 * @property quantity 数量
 * @property subtotal 小計
 * @property thumbnailUrl サムネイル画像URL
 * Used in: [com.example.myapp.dto.market.cart.CartResponse]
 */

data class CartItemResponse(
    val cartItemId: Long,
    val itemId: String,
    val name: String,
    val price: Long,
    val quantity: Int,
    val availableQuantity: Int,
    val subtotal: Long,
    val thumbnailUrl: String?
)
