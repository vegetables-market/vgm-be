package com.example.myapp.entity.market

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "t_cart_items")
data class CartItem(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "f_cart_item_id")
    val cartItemId: Long = 0,

    @Column(name = "f_user_id")
    val userId: Int? = null,

    @Column(name = "f_guest_id")
    val guestId: String? = null,

    @Column(name = "f_item_id", nullable = false)
    val itemId: Long,

    @Column(name = "f_quantity", nullable = false)
    var quantity: Int = 1,

    @Column(name = "f_created_at", updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "f_updated_at")
    var updatedAt: LocalDateTime = LocalDateTime.now()
)
