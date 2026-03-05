package com.example.myapp.repository.market.payment

import com.example.myapp.entity.market.payment.Payment
import org.springframework.data.jpa.repository.JpaRepository

interface PaymentRepository : JpaRepository<Payment, Long> {
    fun findTopByOrderIdOrderByPaymentIdDesc(orderId: Long): Payment?
    fun findTopByOrderIdAndMethodOrderByPaymentIdDesc(orderId: Long, method: String): Payment?
}
