package com.example.myapp.dto.market.item.liisting

import com.fasterxml.jackson.annotation.JsonAlias

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
    @JsonAlias("category_id")
    val categoryId: Long,
    val price: Int,
    val quantity: Int = 1,
    @JsonAlias("shipping_payer_type")
    val shippingPayerType: Int,
    @JsonAlias("shipping_origin_area")
    val shippingOriginArea: Int,
    @JsonAlias("shipping_origin_address_id", "shippingOriginAddressId")
    val shippingOriginAddressId: Int? = null,
    @JsonAlias("shipping_days_id")
    val shippingDaysId: Int,
    @JsonAlias("shipping_method_id")
    val shippingMethodId: Int,
    @JsonAlias("item_condition")
    val itemCondition: Int,
    @JsonAlias("image_urls")
    val imageUrls: List<String>? = null
)
