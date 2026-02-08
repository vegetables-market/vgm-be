package com.example.myapp.entity.user.profile

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "t_user_profile")
class UserProfile(
    @Id
    @Column(name = "f_user_id")
    val userId: Int,

    @Column(name = "f_profile_text")
    var profileText: String? = null,

    @Column(name = "f_profile_image_url", length = 500)
    var profileImageUrl: String? = null,

    @Column(name = "f_rating_count")
    val ratingCount: Int = 0,

    @Column(name = "f_rating_sum")
    val ratingSum: Int = 0,

    @Column(name = "f_sales_count")
    val salesCount: Int = 0,

    @Column(name = "f_purchases_count")
    val purchasesCount: Int = 0,

    @Column(name = "f_following_count")
    var followingCount: Int = 0,

    @Column(name = "f_followers_count")
    var followersCount: Int = 0,

    @Column(name = "f_created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "f_updated_at")
    val updatedAt: LocalDateTime = LocalDateTime.now()
)
