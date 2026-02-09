package com.example.myapp.controller.user.device

import com.example.myapp.entity.auth.UserSession
import com.example.myapp.service.user.UserDeviceService
import com.example.myapp.exception.AppException
import com.example.myapp.exception.ErrorCode
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/v1/user/devices")
class UnregisterDeviceController(
    private val userDeviceService: UserDeviceService
) {

    /**
     * デバイス登録解除
     * トークンを指定して削除する。
     */
    @DeleteMapping("/{token}")
    fun unregisterDevice(
        @AuthenticationPrincipal userSession: UserSession?,
        @PathVariable token: String
    ): ResponseEntity<Map<String, Any>> {
        if (userSession == null) {
             throw AppException(ErrorCode.AUTH_REQUIRED, "Login required")
        }

        userDeviceService.unregisterDevice(token)

        return ResponseEntity.ok(mapOf("success" to true))
    }
}
