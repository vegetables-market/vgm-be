package com.example.myapp.dto.market

import com.fasterxml.jackson.annotation.JsonProperty

data class CreateItemRequest(
    val name: String,
    val description: String,
    
    @JsonProperty("category_id")
    val categoryId: Long,
    
    val price: Int,
    val quantity: Int = 1,
    
    @JsonProperty("shipping_payer_type")
    val shippingPayerType: Int,
    
    @JsonProperty("shipping_origin_area")
    val shippingOriginArea: Int,
    
    @JsonProperty("shipping_days_id")
    val shippingDaysId: Int,
    
    @JsonProperty("shipping_method_id")
    val shippingMethodId: Int,
    
    @JsonProperty("item_condition")
    val itemCondition: Int,
    
    @JsonProperty("image_urls")
    val imageUrls: List<String>? = null
)

data class ItemResponse(
    val id: Long,
    val name: String?,
    val price: Int?,
    val status: Int,
    val imageUrl: String?, // 1枚目
    @JsonProperty("created_at")
    val createdAt: String
)

data class CreateDraftResponse(
    @JsonProperty("item_id")
    val itemId: Long
)

data class LinkImagesRequest(
    @JsonProperty("filenames")
    val filenames: List<String>
)
