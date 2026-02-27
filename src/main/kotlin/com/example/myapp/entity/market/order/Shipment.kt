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
@Table(name = "t_shipments")
class Shipment(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "f_shipment_id")
    val shipmentId: Long = 0,

    @Column(name = "f_order_id", nullable = false)
    val orderId: Long,

    @Column(name = "f_seller_id", nullable = false)
    val sellerId: Int,

    @Column(name = "f_shipping_method_id", nullable = false)
    val shippingMethodId: Int,

    @Column(name = "f_tracking_number")
    val trackingNumber: String? = null,

    @Column(name = "f_shipping_fee")
    val shippingFee: Int = 0,

    @Column(name = "f_status")
    var status: Short = 1,

    @Column(name = "f_shipped_at")
    val shippedAt: LocalDateTime? = null,

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

