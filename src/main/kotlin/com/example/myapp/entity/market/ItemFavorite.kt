package com.example.myapp.entity.market

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "t_items_likes")
class ItemFavorite(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "f_item_favorite_id")
    val favoriteId: Long? = null,

    @Column(name = "f_user_id", nullable = false)
    val userId: Int,

    @Column(name = "f_item_id", nullable = false)
    val itemId: Long,

    @Column(name = "f_created_at", updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)
