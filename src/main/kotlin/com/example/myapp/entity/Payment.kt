package com.example.myapp.entity

//import jakarta.persistence.*
//import java.math.BigDecimal
//import java.time.LocalDateTime
//
//@Entity
//@Table(name = "t_payments")
//data class Payment(
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    @Column(name = "f_payment_id")
//    val id: Long? = null,
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "f_order_id", nullable = false)
//    val order: Order,
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "f_user_id", nullable = false)
//    val user: User,
//
//    @Enumerated(EnumType.STRING)
//    @Column(name = "f_payment_method", nullable = false)
//    val paymentMethod: PaymentMethod,
//
//    @Column(name = "f_amount", nullable = false, precision = 10, scale = 2)
//    val amount: BigDecimal,
//
//    @Enumerated(EnumType.STRING)
//    @Column(name = "f_status", nullable = false)
//    val status: PaymentStatus = PaymentStatus.PENDING,
//
//    @Column(name = "f_external_payment_id", unique = true)
//    val externalPaymentId: String? = null,
//
//    @Column(name = "f_external_charge_id")
//    val externalChargeId: String? = null,
//
//    @Column(name = "f_external_transfer_id")
//    val externalTransferId: String? = null,
//
//    @Column(name = "f_created_at", nullable = false)
//    val createdAt: LocalDateTime = LocalDateTime.now(),
//
//    @Column(name = "f_completed_at")
//    val completedAt: LocalDateTime? = null,
//
//    @Column(name = "f_failed_at")
//    val failedAt: LocalDateTime? = null,
//
//    @Column(name = "f_error_message", columnDefinition = "TEXT")
//    val errorMessage: String? = null,
//
//    @Column(name = "f_metadata", columnDefinition = "TEXT")
//    val metadata: String? = null
//)
//
//enum class PaymentMethod {
//    CREDIT_CARD,    // クレジットカード（Stripe）
//    PAYPAY,         // PayPay
//    KONBINI,        // コンビニ決済（Stripe）
//    BANK_TRANSFER   // 銀行振込
//}
//
//enum class PaymentStatus {
//    PENDING,        // 処理待ち
//    PROCESSING,     // 処理中
//    COMPLETED,      // 完了
//    FAILED,         // 失敗
//    REFUNDED,       // 返金済み
//    CANCELLED       // キャンセル
//}
