package com.example.myapp.entity

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "t_items")
data class Product(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "f_item_id")
    val id: Long? = null,

    @Column(name = "f_item_name", nullable = false)
    val name: String,

    @Column(name = "f_description", columnDefinition = "TEXT")
    val description: String? = null,

    @Column(name = "f_price", nullable = false, precision = 10, scale = 2)
    val price: BigDecimal,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "f_user_id", nullable = false)
    val seller: User,

    @Enumerated(EnumType.STRING)
    @Column(name = "f_status", nullable = false)
    val status: ProductStatus = ProductStatus.AVAILABLE,

    @Column(name = "f_created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "f_updated_at")
    val updatedAt: LocalDateTime? = null,

    @Column(name = "f_sold_at")
    val soldAt: LocalDateTime? = null,

    @Column(name = "f_image_urls", columnDefinition = "TEXT")
    val imageUrls: String? = null,

    @Column(name = "f_category")
    val category: String? = null,

    @Column(name = "f_stock", nullable = false)
    val stock: Int = 1
)

enum class ProductStatus {
    AVAILABLE,      // 販売中
    RESERVED,       // 予約済み
    SOLD,           // 売却済み
    REMOVED         // 削除済み
}
