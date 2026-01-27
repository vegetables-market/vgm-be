package com.example.myapp.entity.market

//import jakarta.persistence.*
//import java.math.BigDecimal
//import java.time.LocalDateTime
//
//@Entity
//@Table(name = "t_transactions")
//data class Transaction(
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    @Column(name = "f_transaction_id")
//    val id: Long? = null,
//
//    @OneToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "f_order_id", nullable = false, unique = true)
//    val order: Order,
//
//    @Enumerated(EnumType.STRING)
//    @Column(name = "f_type", nullable = false)
//    val type: TransactionType,
//
//    @Column(name = "f_amount", nullable = false, precision = 10, scale = 2)
//    val amount: BigDecimal,
//
//    @Enumerated(EnumType.STRING)
//    @Column(name = "f_status", nullable = false)
//    val status: TransactionStatus = TransactionStatus.PENDING,
//
//    @Column(name = "f_escrow_started_at")
//    val escrowStartedAt: LocalDateTime? = null,
//
//    @Column(name = "f_escrow_released_at")
//    val escrowReleasedAt: LocalDateTime? = null,
//
//    @Column(name = "f_refunded_at")
//    val refundedAt: LocalDateTime? = null,
//
//    @Column(name = "f_created_at", nullable = false)
//    val createdAt: LocalDateTime = LocalDateTime.now(),
//
//    @Column(name = "f_updated_at")
//    val updatedAt: LocalDateTime? = null,
//
//    @Column(name = "f_notes", columnDefinition = "TEXT")
//    val notes: String? = null
//)
//
//enum class TransactionType {
//    PAYMENT,        // 買い手からの支払い
//    PAYOUT,         // 出品者への入金
//    REFUND          // 返金
//}
//
//enum class TransactionStatus {
//    PENDING,        // 処理待ち
//    PROCESSING,     // 処理中
//    ESCROWED,       // エスクロー中（資金保留中）
//    COMPLETED,      // 完了
//    FAILED,         // 失敗
//    REFUNDED        // 返金済み
//}
