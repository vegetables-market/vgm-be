package com.example.myapp.controller.user.device

import com.example.myapp.entity.auth.UserSession
import com.example.myapp.service.user.UserDeviceService
import com.example.myapp.exception.AppException
import com.example.myapp.exception.ErrorCode
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

data class RegisterDeviceRequest(
    val fcmToken: String,
    val deviceType: String? = null,
    val appVersion: String? = null,
    val osVersion: String? = null
)

@RestController
@RequestMapping("/v1/user/devices")
class RegisterDeviceController(
    private val userDeviceService: UserDeviceService
) {

    /**
     * デバイス（FCMトークン）登録・更新
     */
    @PostMapping
    fun registerDevice(
        @AuthenticationPrincipal userSession: UserSession?,
        @RequestBody request: RegisterDeviceRequest
    ): ResponseEntity<Map<String, Any>> {
        if (userSession == null) {
            throw AppException(ErrorCode.AUTH_REQUIRED, "Login required")
        }

        userDeviceService.registerDevice(
            userId = userSession.userId,
            fcmToken = request.fcmToken,
            deviceType = request.deviceType,
            appVersion = request.appVersion,
            osVersion = request.osVersion
        )

        return ResponseEntity.ok(mapOf("success" to true))
    }
}
