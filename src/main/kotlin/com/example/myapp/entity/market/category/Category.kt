package com.example.myapp.entity.market.category

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "m_categories")
class Category(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "f_category_id")
    val categoryId: Long? = null,

    @Column(name = "f_name", nullable = false)
    val name: String,

    @Column(name = "f_parent_id")
    val parentId: Long? = null,

    @Column(name = "f_level")
    val level: Int = 1,

    @Column(name = "f_icon_url")
    val iconUrl: String? = null,

    @Column(name = "f_sort_order")
    val sortOrder: Int = 0,

    @Column(name = "f_created_at", updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "f_updated_at")
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    @PreUpdate
    fun onPreUpdate() {
        updatedAt = LocalDateTime.now()
    }
}
