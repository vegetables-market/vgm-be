package com.example.myapp.dto.market.item

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
