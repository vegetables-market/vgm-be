package com.example.myapp.dto.user

data class NotificationSettingsDto(
    val emailNotifications: Boolean,
    val favoritePriceDrop: Boolean,
    val newMessage: Boolean,
    val transactionUpdates: Boolean
)

data class UpdateNotificationSettingsRequest(
    val emailNotifications: Boolean,
    val favoritePriceDrop: Boolean,
    val newMessage: Boolean,
    val transactionUpdates: Boolean
)
