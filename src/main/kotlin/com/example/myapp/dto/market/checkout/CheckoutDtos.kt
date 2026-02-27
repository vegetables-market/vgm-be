package com.example.myapp.dto.market.checkout

import com.fasterxml.jackson.annotation.JsonAlias

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

data class CheckoutResponse(
    val orderId: Long,
    val status: String,
    val items: List<CheckoutItemResult>,
    val totalAmount: Long,
)

data class CheckoutItemResult(
    val itemId: String,
    val quantity: Int,
    val unitPrice: Long,
    val subtotal: Long,
)

data class CheckoutPayRequest(
    @JsonAlias("payment_method")
    val paymentMethod: String? = null,
)

data class CheckoutPayResponse(
    val orderId: Long,
    val status: String,
)
