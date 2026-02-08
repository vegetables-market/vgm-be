package com.example.myapp.entity.market.item

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "t_items_likes")
class ItemFavorite(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "f_item_favorite_id")
    val favoriteId: Long? = null,

    @Column(name = "f_user_id")
    val userId: Int? = null,

    @Column(name = "f_guest_id")
    val guestId: String? = null,

    @Column(name = "f_item_id", nullable = false)
    val itemId: Long,

    @Column(name = "f_created_at", updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)
