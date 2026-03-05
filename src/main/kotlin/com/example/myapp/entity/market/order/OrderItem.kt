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
@Table(name = "t_orders_items")
class OrderItem(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "f_order_item_id")
    val orderItemId: Long = 0,

    @Column(name = "f_order_id", nullable = false)
    val orderId: Long,

    @Column(name = "f_shipment_id", nullable = false)
    val shipmentId: Long,

    @Column(name = "f_item_id", nullable = false)
    val itemId: Long,

    @Column(name = "f_seller_id", nullable = false)
    val sellerId: Int,

    @Column(name = "f_unit_price", nullable = false)
    val unitPrice: Int,

    @Column(name = "f_quantity", nullable = false)
    val quantity: Int,

    @Column(name = "f_platform_fee")
    val platformFee: Int = 0,

    @Column(name = "f_seller_amount")
    val sellerAmount: Int = 0,

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

