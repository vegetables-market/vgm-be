package com.example.myapp.controller.user.notifications

import com.example.myapp.dto.user.NotificationSettingsDto
import com.example.myapp.dto.user.UpdateNotificationSettingsRequest
import com.example.myapp.entity.auth.UserSession
import com.example.myapp.service.user.NotificationSettingsService
import com.example.myapp.exception.AppException
import com.example.myapp.exception.ErrorCode
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/v1/user/notification-settings")
class NotificationSettingsController(
    private val notificationSettingsService: NotificationSettingsService
) {

    /**
     * 通知設定取得
     */
    @GetMapping
    fun getSettings(
        @AuthenticationPrincipal userSession: UserSession?
    ): ResponseEntity<NotificationSettingsDto> {
        if (userSession == null) throw AppException(ErrorCode.AUTH_REQUIRED, "Login required")
        val settings = notificationSettingsService.getSettings(userSession.userId)
        return ResponseEntity.ok(settings)
    }

    /**
     * 通知設定更新
     */
    @PutMapping
    fun updateSettings(
        @AuthenticationPrincipal userSession: UserSession?,
        @RequestBody request: UpdateNotificationSettingsRequest
    ): ResponseEntity<NotificationSettingsDto> {
        if (userSession == null) throw AppException(ErrorCode.AUTH_REQUIRED, "Login required")
        val settings = notificationSettingsService.updateSettings(userSession.userId, request)
        return ResponseEntity.ok(settings)
    }
}
