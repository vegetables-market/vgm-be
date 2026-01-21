package com.example.myapp.entity

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "orders")
data class Order(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_id", nullable = false)
    val buyer: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    val seller: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    val product: Product,

    @Column(nullable = false, precision = 10, scale = 2)
    val totalAmount: BigDecimal,

    // 手数料（プラットフォーム手数料、例: 10%）
    @Column(nullable = false, precision = 10, scale = 2)
    val platformFee: BigDecimal,

    // 出品者への入金額（totalAmount - platformFee）
    @Column(nullable = false, precision = 10, scale = 2)
    val sellerAmount: BigDecimal,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val status: OrderStatus = OrderStatus.PENDING_PAYMENT,

    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column
    val paidAt: LocalDateTime? = null,

    @Column
    val shippedAt: LocalDateTime? = null,

    @Column
    val deliveredAt: LocalDateTime? = null,

    @Column
    val completedAt: LocalDateTime? = null,

    @Column
    val cancelledAt: LocalDateTime? = null,

    // 配送先情報
    @Column
    val shippingAddress: String? = null,

    @Column
    val shippingPostalCode: String? = null,

    @Column
    val shippingRecipientName: String? = null,

    @Column
    val shippingPhoneNumber: String? = null,

    // キャンセル理由
    @Column(columnDefinition = "TEXT")
    val cancellationReason: String? = null
)

enum class OrderStatus {
    PENDING_PAYMENT,    // 支払い待ち
    PAID,               // 支払い完了（エスクロー保留中）
    SHIPPED,            // 発送済み
    DELIVERED,          // 配送完了（買い手確認待ち）
    COMPLETED,          // 取引完了（入金済み）
    CANCELLED,          // キャンセル
    REFUNDED            // 返金済み
}
