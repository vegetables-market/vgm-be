package com.example.myapp.dto.market.item.liisting

/**
 * 商品出品/更新リクエストDTO
 *
 * @property name 商品名
 * @property description 説明
 * @property categoryId カテゴリID
 * @property price 価格
 * @property quantity 在庫数
 * @property shippingPayerType 配送料負担 (0:送料込, 1:着払い)
 * @property shippingOriginArea 発送元地域 (都道府県ID)
 * @property shippingDaysId 発送までの日数ID
 * @property shippingMethodId 配送方法ID
 * @property itemCondition 商品の状態ID
 * @property imageUrls 商品画像URLリスト (任意)
 * Used in: [com.example.myapp.controller.market.item.listing.PublishItemController]
 */

data class CreateItemRequest(
    val name: String,
    val description: String,
    val categoryId: Long,
    val price: Int,
    val quantity: Int = 1,
    val shippingPayerType: Int,
    val shippingOriginArea: Int,
    val shippingDaysId: Int,
    val shippingMethodId: Int,
    val itemCondition: Int,
    val imageUrls: List<String>? = null
)
