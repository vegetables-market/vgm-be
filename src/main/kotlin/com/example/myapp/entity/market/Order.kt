package com.example.myapp.entity.market

//import jakarta.persistence.*
//import java.math.BigDecimal
//import java.time.LocalDateTime
//
//@Entity
//@Table(name = "t_orders")
//data class Order(
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    @Column(name = "f_order_id")
//    val id: Long? = null,
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "f_buyer_id", nullable = false)
//    val buyer: User,
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "f_seller_id", nullable = false)
//    val seller: User,
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "f_item_id", nullable = false)
//    val product: Product,
//
//    @Column(name = "f_total_amount", nullable = false, precision = 10, scale = 2)
//    val totalAmount: BigDecimal,
//
//    @Column(name = "f_platform_fee", nullable = false, precision = 10, scale = 2)
//    val platformFee: BigDecimal,
//
//    @Column(name = "f_seller_amount", nullable = false, precision = 10, scale = 2)
//    val sellerAmount: BigDecimal,
//
//    @Enumerated(EnumType.STRING)
//    @Column(name = "f_status", nullable = false)
//    val status: OrderStatus = OrderStatus.PENDING_PAYMENT,
//
//    @Column(name = "f_created_at", nullable = false)
//    val createdAt: LocalDateTime = LocalDateTime.now(),
//
//    @Column(name = "f_paid_at")
//    val paidAt: LocalDateTime? = null,
//
//    @Column(name = "f_shipped_at")
//    val shippedAt: LocalDateTime? = null,
//
//    @Column(name = "f_delivered_at")
//    val deliveredAt: LocalDateTime? = null,
//
//    @Column(name = "f_completed_at")
//    val completedAt: LocalDateTime? = null,
//
//    @Column(name = "f_cancelled_at")
//    val cancelledAt: LocalDateTime? = null,
//
//    @Column(name = "f_shipping_address")
//    val shippingAddress: String? = null,
//
//    @Column(name = "f_shipping_postal_code")
//    val shippingPostalCode: String? = null,
//
//    @Column(name = "f_shipping_recipient_name")
//    val shippingRecipientName: String? = null,
//
//    @Column(name = "f_shipping_phone_number")
//    val shippingPhoneNumber: String? = null,
//
//    @Column(name = "f_cancellation_reason", columnDefinition = "TEXT")
//    val cancellationReason: String? = null
//)
//
//enum class OrderStatus {
//    PENDING_PAYMENT,    // 支払い待ち
//    PAID,               // 支払い完了（エスクロー保留中）
//    SHIPPED,            // 発送済み
//    DELIVERED,          // 配送完了（買い手確認待ち）
//    COMPLETED,          // 取引完了（入金済み）
//    CANCELLED,          // キャンセル
//    REFUNDED            // 返金済み
//}
