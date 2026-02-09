package com.example.myapp.dto.user

/**
 * 通知設定DTO
 *
 * @property emailNotifications メール通知
 * @property favoritePriceDrop いいねした商品の値下げ通知
 * @property newMessage 新着メッセージ通知
 * @property transactionUpdates 取引関連の更新通知
 * Used in: [com.example.myapp.controller.user.notifications.NotificationSettingsController]
 */
data class NotificationSettingsDto(
    val emailNotifications: Boolean,
    val favoritePriceDrop: Boolean,
    val newMessage: Boolean,
    val transactionUpdates: Boolean
)

/**
 * 通知設定更新リクエストDTO
 *
 * @property emailNotifications メール通知
 * @property favoritePriceDrop いいねした商品の値下げ通知
 * @property newMessage 新着メッセージ通知
 * @property transactionUpdates 取引関連の更新通知
 * Used in: [com.example.myapp.controller.user.notifications.NotificationSettingsController]
 */
data class UpdateNotificationSettingsRequest(
    val emailNotifications: Boolean,
    val favoritePriceDrop: Boolean,
    val newMessage: Boolean,
    val transactionUpdates: Boolean
)
