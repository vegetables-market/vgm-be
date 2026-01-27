package com.example.myapp.dto.market

//import com.example.myapp.entity.OrderStatus
//import java.math.BigDecimal
//
//// 注文作成リクエスト
//data class CreateOrderRequest(
//    val buyerId: Long,
//    val productId: Long,
//    val shippingAddress: String?,
//    val shippingPostalCode: String?,
//    val shippingRecipientName: String?,
//    val shippingPhoneNumber: String?
//)
//
//data class CreateOrderResponse(
//    val success: Boolean,
//    val message: String,
//    val orderId: Long? = null,
//    val totalAmount: Double? = null,
//    val platformFee: Double? = null,
//    val sellerAmount: Double? = null
//)
//
//// 注文詳細レスポンス
//data class OrderDetailResponse(
//    val success: Boolean,
//    val orderId: Long,
//    val productName: String,
//    val productPrice: BigDecimal,
//    val buyerName: String,
//    val sellerName: String,
//    val totalAmount: BigDecimal,
//    val platformFee: BigDecimal,
//    val sellerAmount: BigDecimal,
//    val status: OrderStatus,
//    val createdAt: String
//)
//
//// 注文ステータス更新リクエスト
//data class UpdateOrderStatusRequest(
//    val orderId: Long,
//    val status: OrderStatus
//)
//
//data class UpdateOrderStatusResponse(
//    val success: Boolean,
//    val message: String
//)
