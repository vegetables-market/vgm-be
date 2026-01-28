package com.example.myapp.entity.market

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "t_items_images")
data class ItemImage(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "f_image_id")
    val imageId: Long? = null,

    @Column(name = "f_item_id", nullable = false)
    val itemId: Long, // ItemとのRelationは一旦ID参照にするか、@ManyToOneにするか。シンプルにIDで。

    @Column(name = "f_image_url", nullable = false, columnDefinition = "TEXT")
    val imageUrl: String,

    @Column(name = "f_display_order")
    val displayOrder: Int = 1,

    @Column(name = "f_status")
    val status: Int = 1, // 1:有効, 0:無効

    @Column(name = "f_created_at", updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)
