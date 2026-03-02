package com.example.myapp.dto.market.shipping

import com.fasterxml.jackson.annotation.JsonAlias

data class ShippingEstimateRequest(
    @JsonAlias("item_id")
    val itemId: String,
    @JsonAlias("address_id")
    val addressId: Int,
)

data class ShippingEstimateResponse(
    val itemId: String,
    val addressId: Int,
    val distanceKm: Int,
    val estimatedDays: Int,
    val distanceBand: String,
    val estimatedLabel: String,
    val note: String = "配送業者・天候・交通状況により前後します",
)
