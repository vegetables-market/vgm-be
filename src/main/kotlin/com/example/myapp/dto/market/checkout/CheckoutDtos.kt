package com.example.myapp.dto.market.checkout

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

data class CheckoutRequest(
    val items: List<CheckoutItemRequest>,
    val shipping: CheckoutShippingRequest,
    @JsonAlias("payment_method")
    val paymentMethod: String? = null,
    @JsonAlias("idempotency_key")
    val idempotencyKey: String? = null,
)

data class CheckoutItemRequest(
    @JsonAlias("item_id")
    val itemId: String,
    val quantity: Int,
)

data class CheckoutShippingRequest(
    val name: String,
    @JsonAlias("zip_code")
    val zipCode: String,
    val prefecture: String,
    val city: String,
    @JsonAlias("address_line1")
    val addressLine1: String,
    @JsonAlias("address_line2")
    val addressLine2: String? = null,
)

@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy::class)
data class CheckoutResponse(
    val orderId: Long,
    val status: String,
    val items: List<CheckoutItemResult>,
    val totalAmount: Long,
    val paymentIntentId: String? = null,
    val clientSecret: String? = null,
)

@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy::class)
data class CheckoutItemResult(
    val itemId: String,
    val quantity: Int,
    val unitPrice: Long,
    val subtotal: Long,
)

data class CheckoutPayRequest(
    @JsonAlias("payment_method")
    val paymentMethod: String? = null,
    @JsonAlias("payment_intent_id")
    val paymentIntentId: String? = null,
)

@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy::class)
data class CheckoutPayResponse(
    val orderId: Long,
    val status: String,
)
