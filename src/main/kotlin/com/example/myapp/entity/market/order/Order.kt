package com.example.myapp.entity.market.order

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.PreUpdate
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "t_orders")
class Order(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "f_order_id")
    val orderId: Long = 0,

    @Column(name = "f_buyer_id", nullable = false)
    val buyerId: Int,

    @Column(name = "f_total_amount", nullable = false)
    val totalAmount: Long,

    @Column(name = "f_status", nullable = false)
    var status: Short = 1,

    @Column(name = "f_shipping_name", nullable = false)
    val shippingName: String,

    @Column(name = "f_shipping_zip_code", nullable = false)
    val shippingZipCode: String,

    @Column(name = "f_shipping_prefecture", nullable = false)
    val shippingPrefecture: String,

    @Column(name = "f_shipping_city", nullable = false)
    val shippingCity: String,

    @Column(name = "f_shipping_address_line1", nullable = false)
    val shippingAddressLine1: String,

    @Column(name = "f_shipping_address_line2")
    val shippingAddressLine2: String? = null,

    @Column(name = "f_created_at", updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "f_updated_at")
    var updatedAt: LocalDateTime = LocalDateTime.now(),
) {
    @PreUpdate
    fun onPreUpdate() {
        updatedAt = LocalDateTime.now()
    }
}

