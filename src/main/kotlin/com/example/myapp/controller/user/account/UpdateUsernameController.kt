package com.example.myapp.controller.user.account

import com.example.myapp.controller.common.getAppUser
import com.example.myapp.dto.user.profile.ChangeUsernameRequest
import com.example.myapp.exception.AppException
import com.example.myapp.service.auth.AppCookieService
import com.example.myapp.service.auth.SessionService
import com.example.myapp.service.user.profile.UserProfileService
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/user/account")
class UpdateUsernameController(
    private val userProfileService: UserProfileService,
    private val appCookieService: AppCookieService,
    private val sessionService: SessionService
) {

    /**
     * ユーザー名変更
     */
    @PutMapping("/username")
    fun changeUsername(
        @RequestBody request: ChangeUsernameRequest,
        servletRequest: HttpServletRequest
    ): ResponseEntity<Map<String, Any>> {
        val (userId, _) = servletRequest.getAppUser(appCookieService, sessionService)
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(mapOf("error" to "ログインが必要です"))
        }

        try {
            val result = userProfileService.updateUsername(userId, request.newUsername, request.password)
            return ResponseEntity.ok(result)
        } catch (e: AppException) {
            return ResponseEntity.status(e.errorCode.httpStatus)
                .body(mapOf("error" to e.message))
        }
    }
}
