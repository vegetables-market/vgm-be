package com.example.myapp.controller.user.account

import com.example.myapp.controller.common.getAppUser
import com.example.myapp.dto.user.account.UserProfileInfo
import com.example.myapp.service.auth.session.AppCookieService
import com.example.myapp.service.auth.session.SessionService
import com.example.myapp.service.user.profile.UserProfileService
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/user/account")
class MyAccountController(
    private val userProfileService: UserProfileService,
    private val appCookieService: AppCookieService,
    private val sessionService: SessionService
) {

    /**
     * ユーザー情報取得（現在のユーザー名等）
     */
    @GetMapping("/me")
    fun getMyProfile(
        servletRequest: HttpServletRequest
    ): ResponseEntity<UserProfileInfo> {
        val (userId, _) = servletRequest.getAppUser(appCookieService, sessionService)
        if (userId == null) {
            throw com.example.myapp.exception.AppException(com.example.myapp.exception.ErrorCode.AUTH_REQUIRED, "ログインが必要です")
        }

        val profileInfo = userProfileService.getUserProfileInfo(userId)
        return ResponseEntity.ok(profileInfo)
    }
}
