package com.example.myapp.entity.market.item

import com.example.myapp.entity.user.User
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "t_items")
class Item(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "f_item_id")
    val itemId: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "f_user_id", nullable = false)
    val user: User,

    @Column(name = "f_spot_id")
    val spotId: Long? = null,

    @Column(name = "f_name")
    var name: String? = null,

    @Column(name = "f_description", columnDefinition = "TEXT")
    var description: String? = null,

    @Column(name = "f_categories_id")
    var categoryId: Long? = null,

    @Column(name = "f_price")
    var price: Int? = null,

    @Column(name = "f_quantity", nullable = false)
    var quantity: Int = 1,

    // 0:出品作業中, 1:下書き, 2:出品中, 3:取引中, 4:売切, 5:停止
    @Column(name = "f_status", nullable = false)
    var status: Short = 0,

    // 0:送料込(出品者負担), 1:着払い
    @Column(name = "f_shipping_payer_type")
    var shippingPayerType: Int = 0,

    // 都道府県ID
    @Column(name = "f_shipping_origin_area")
    var shippingOriginArea: Int? = null,

    @Column(name = "f_shipping_days_id")
    var shippingDaysId: Int? = null,

    @Column(name = "f_shipping_method_id")
    var shippingMethodId: Int? = null,

    // 0:新品...
    @Column(name = "f_item_condition")
    var itemCondition: Int = 0,

    // 0:常温, 1:冷蔵, 2:冷凍
    @Column(name = "f_preservation_method")
    var preservationMethod: Int = 0,

    @Column(name = "f_expiration_date")
    var expirationDate: LocalDateTime? = null,

    @Column(name = "f_brand")
    var brand: String? = null,

    @Column(name = "f_weight")
    var weight: Int? = null,

    @Column(name = "f_likes_count")
    var likesCount: Int = 0,

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
