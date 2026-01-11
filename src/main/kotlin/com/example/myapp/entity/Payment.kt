package com.example.myapp.entity

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "payments")
data class Payment(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    val order: Order,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val paymentMethod: PaymentMethod,

    @Column(nullable = false, precision = 10, scale = 2)
    val amount: BigDecimal,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val status: PaymentStatus = PaymentStatus.PENDING,

    // Stripe Payment Intent ID または PayPay取引ID
    @Column(unique = true)
    val externalPaymentId: String? = null,

    // Stripe Charge ID（エスクロー管理用）
    @Column
    val externalChargeId: String? = null,

    // Stripe Transfer ID（出品者への入金用）
    @Column
    val externalTransferId: String? = null,

    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column
    val completedAt: LocalDateTime? = null,

    @Column
    val failedAt: LocalDateTime? = null,

    // エラーメッセージ（決済失敗時）
    @Column(columnDefinition = "TEXT")
    val errorMessage: String? = null,

    // メタデータ（JSON形式で決済プロバイダからの追加情報を保存）
    @Column(columnDefinition = "TEXT")
    val metadata: String? = null
)

enum class PaymentMethod {
    CREDIT_CARD,    // クレジットカード（Stripe）
    PAYPAY,         // PayPay
    KONBINI,        // コンビニ決済（Stripe）
    BANK_TRANSFER   // 銀行振込
}

enum class PaymentStatus {
    PENDING,        // 処理待ち
    PROCESSING,     // 処理中
    COMPLETED,      // 完了
    FAILED,         // 失敗
    REFUNDED,       // 返金済み
    CANCELLED       // キャンセル
}
