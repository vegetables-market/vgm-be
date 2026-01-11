package com.example.myapp.entity

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "transactions")
data class Transaction(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    val order: Order,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val type: TransactionType,

    @Column(nullable = false, precision = 10, scale = 2)
    val amount: BigDecimal,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val status: TransactionStatus = TransactionStatus.PENDING,

    // エスクロー（資金保留）開始時刻
    @Column
    val escrowStartedAt: LocalDateTime? = null,

    // エスクロー解除（出品者への入金）時刻
    @Column
    val escrowReleasedAt: LocalDateTime? = null,

    // 返金時刻
    @Column
    val refundedAt: LocalDateTime? = null,

    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column
    val updatedAt: LocalDateTime? = null,

    // トランザクションメモ（エラーメッセージや追加情報）
    @Column(columnDefinition = "TEXT")
    val notes: String? = null
)

enum class TransactionType {
    PAYMENT,        // 買い手からの支払い
    PAYOUT,         // 出品者への入金
    REFUND          // 返金
}

enum class TransactionStatus {
    PENDING,        // 処理待ち
    PROCESSING,     // 処理中
    ESCROWED,       // エスクロー中（資金保留中）
    COMPLETED,      // 完了
    FAILED,         // 失敗
    REFUNDED        // 返金済み
}
