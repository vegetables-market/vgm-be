package com.example.myapp.service.user

import com.example.myapp.dto.user.NotificationSettingsDto
import com.example.myapp.dto.user.UpdateNotificationSettingsRequest
import com.example.myapp.entity.user.settings.NotificationSettings
import com.example.myapp.repository.user.NotificationSettingsRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class NotificationSettingsService(
    private val notificationSettingsRepository: NotificationSettingsRepository
) {

    /**
     * 通知設定取得（存在しない場合はデフォルト値で作成）
     */
    @Transactional
    fun getSettings(userId: Int): NotificationSettingsDto {
        val settings = notificationSettingsRepository.findById(userId)
            .orElseGet {
                // 存在しない場合はデフォルト値で作成
                val newSettings = NotificationSettings(userId = userId)
                notificationSettingsRepository.save(newSettings)
            }

        return NotificationSettingsDto(
            emailNotifications = settings.emailNotifications,
            favoritePriceDrop = settings.favoritePriceDrop,
            newMessage = settings.newMessage,
            transactionUpdates = settings.transactionUpdates
        )
    }

    /**
     * 通知設定更新
     */
    @Transactional
    fun updateSettings(userId: Int, request: UpdateNotificationSettingsRequest): NotificationSettingsDto {
        val settings = notificationSettingsRepository.findById(userId)
            .orElseGet {
                NotificationSettings(userId = userId)
            }

        settings.emailNotifications = request.emailNotifications
        settings.favoritePriceDrop = request.favoritePriceDrop
        settings.newMessage = request.newMessage
        settings.transactionUpdates = request.transactionUpdates

        val saved = notificationSettingsRepository.save(settings)

        return NotificationSettingsDto(
            emailNotifications = saved.emailNotifications,
            favoritePriceDrop = saved.favoritePriceDrop,
            newMessage = saved.newMessage,
            transactionUpdates = saved.transactionUpdates
        )
    }
}
