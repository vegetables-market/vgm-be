package com.example.myapp.entity

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "products")
data class Product(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    val name: String,

    @Column(columnDefinition = "TEXT")
    val description: String? = null,

    @Column(nullable = false, precision = 10, scale = 2)
    val price: BigDecimal,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "seller_id", nullable = false)
    val seller: User,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val status: ProductStatus = ProductStatus.AVAILABLE,

    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column
    val updatedAt: LocalDateTime? = null,

    @Column
    val soldAt: LocalDateTime? = null,

    // 商品画像URL（カンマ区切り、または後でProductImageテーブルに分離可能）
    @Column(columnDefinition = "TEXT")
    val imageUrls: String? = null,

    // カテゴリー（後でCategoryテーブルに分離可能）
    @Column
    val category: String? = null,

    // 在庫数（1つの商品として扱う場合は1固定）
    @Column(nullable = false)
    val stock: Int = 1
)

enum class ProductStatus {
    AVAILABLE,      // 販売中
    RESERVED,       // 予約済み
    SOLD,           // 売却済み
    REMOVED         // 削除済み
}
