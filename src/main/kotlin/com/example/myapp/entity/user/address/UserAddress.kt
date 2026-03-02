package com.example.myapp.entity.user.address

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.PreUpdate
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "t_user_address")
class UserAddress(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "f_address_id")
    val addressId: Int = 0,

    @Column(name = "f_user_id", nullable = false)
    val userId: Int,

    @Column(name = "f_postal_code", nullable = false, length = 100)
    var postalCode: String,

    @Column(name = "f_prefecture", nullable = false, length = 50)
    var prefecture: String,

    @Column(name = "f_city", nullable = false, length = 100)
    var city: String,

    @Column(name = "f_address_line1", nullable = false, length = 255)
    var addressLine1: String,

    @Column(name = "f_address_line2", length = 255)
    var addressLine2: String? = null,

    @Column(name = "f_country_code", length = 2)
    var countryCode: String = "JP",

    @Column(name = "f_is_default")
    var isDefault: Short = 0,

    @Column(name = "f_created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "f_updated_at")
    var updatedAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "f_deleted_at")
    var deletedAt: LocalDateTime? = null,
) {
    @PreUpdate
    fun onPreUpdate() {
        updatedAt = LocalDateTime.now()
    }
}
