package com.example.myapp.entity.market.payment

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "t_payments")
class Payment(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "f_payment_id")
    val paymentId: Long = 0,

    @Column(name = "f_order_id", nullable = false)
    val orderId: Long,

    @Column(name = "f_method")
    var method: String? = null,

    @Column(name = "f_ext_trans_id")
    var externalTransactionId: String? = null,

    @Column(name = "f_status")
    var status: String? = null,

    @Column(name = "f_amount")
    var amount: BigDecimal? = null,

    @Column(name = "f_created_at", updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
)
