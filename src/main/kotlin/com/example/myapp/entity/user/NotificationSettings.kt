package com.example.myapp.entity.user

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "t_notification_settings")
class NotificationSettings(
    @Id
    @Column(name = "f_user_id")
    val userId: Int,

    @Column(name = "f_email_notifications")
    var emailNotifications: Boolean = true,

    @Column(name = "f_favorite_price_drop")
    var favoritePriceDrop: Boolean = true,

    @Column(name = "f_new_message")
    var newMessage: Boolean = true,

    @Column(name = "f_transaction_updates")
    var transactionUpdates: Boolean = true,

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
