package com.example.myapp.controller.user.profile

import com.example.myapp.controller.common.getAppUser
import com.example.myapp.dto.user.profile.UpdateBioRequest
import com.example.myapp.exception.AppException
import com.example.myapp.service.auth.session.AppCookieService
import com.example.myapp.service.auth.session.SessionService
import com.example.myapp.service.user.profile.UserProfileService
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/user/profile")
class UpdateBioController(
    private val userProfileService: UserProfileService,
    private val appCookieService: AppCookieService,
    private val sessionService: SessionService
) {

    /**
     * プロフィールテキスト更新
     */
    @PutMapping("/bio")
    fun updateBio(
        @RequestBody request: UpdateBioRequest,
        servletRequest: HttpServletRequest
    ): ResponseEntity<Map<String, Any?>> {
        val (userId, _) = servletRequest.getAppUser(appCookieService, sessionService)
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(mapOf("error" to "ログインが必要です"))
        }

        try {
            val profile = userProfileService.updateProfileText(userId, request.bio)
            return ResponseEntity.ok(mapOf<String, Any?>(
                "success" to true,
                "message" to "自己紹介を更新しました",
                "bio" to profile.profileText
            ))
        } catch (e: AppException) {
            return ResponseEntity.status(e.errorCode.httpStatus)
                .body(mapOf("error" to e.message))
        }
    }
}
