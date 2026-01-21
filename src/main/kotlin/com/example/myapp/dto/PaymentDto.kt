package com.example.myapp.dto

import com.example.myapp.entity.PaymentMethod
import com.example.myapp.entity.PaymentStatus
import java.math.BigDecimal

// 決済リクエスト
data class CreatePaymentRequest(
    val orderId: Long,
    val userId: Int,
    val paymentMethod: PaymentMethod,
    val amount: Double
)

data class CreatePaymentResponse(
    val success: Boolean,
    val message: String,
    val paymentId: Long? = null,
    val clientSecret: String? = null, // Stripe用
    val paypayUrl: String? = null, // PayPay用
    val paypayDeeplink: String? = null // PayPay用
)

// 決済確認リクエスト
data class ConfirmPaymentRequest(
    val paymentIntentId: String
)

data class ConfirmPaymentResponse(
    val success: Boolean,
    val message: String,
    val paymentId: Long? = null,
    val status: PaymentStatus? = null
)

// エスクロー解除リクエスト
data class ReleaseEscrowRequest(
    val orderId: Long
)

data class ReleaseEscrowResponse(
    val success: Boolean,
    val message: String,
    val transactionId: Long? = null
)

// 返金リクエスト
data class RefundPaymentRequest(
    val orderId: Long,
    val reason: String? = null
)

data class RefundPaymentResponse(
    val success: Boolean,
    val message: String,
    val refundId: String? = null
)

// 決済ステータス取得レスポンス
data class PaymentStatusResponse(
    val success: Boolean,
    val paymentId: Long,
    val status: PaymentStatus,
    val amount: BigDecimal,
    val paymentMethod: PaymentMethod,
    val createdAt: String,
    val completedAt: String?
)
