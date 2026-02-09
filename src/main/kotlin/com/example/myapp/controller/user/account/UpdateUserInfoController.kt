package com.example.myapp.controller.user.account

import com.example.myapp.controller.common.getAppUser
import com.example.myapp.dto.user.profile.UpdateUserInfoRequest
import com.example.myapp.exception.AppException
import com.example.myapp.service.auth.session.AppCookieService
import com.example.myapp.service.auth.session.SessionService
import com.example.myapp.service.user.profile.UserInfoService
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@RestController
@RequestMapping("/v1/user/account")
class UpdateUserInfoController(
    private val userInfoService: UserInfoService,
    private val appCookieService: AppCookieService,
    private val sessionService: SessionService
) {

    /**
     * ユーザー情報更新 (性別、生年月日)
     */
    @PutMapping("/info")
    fun updateUserInfo(
        @RequestBody request: UpdateUserInfoRequest,
        servletRequest: HttpServletRequest
    ): ResponseEntity<Map<String, Any?>> {
        val (userId, _) = servletRequest.getAppUser(appCookieService, sessionService)
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(mapOf("error" to "ログインが必要です"))
        }

        // バリデーション
        request.gender?.let { gender ->
            if (gender !in 0..3) {
                return ResponseEntity.badRequest()
                    .body(mapOf("error" to "性別の値が不正です"))
            }
        }

        val birthDate = request.birthDate?.let { dateStr ->
            try {
                val date = LocalDate.parse(dateStr)
                if (date.isAfter(LocalDate.now())) {
                    return ResponseEntity.badRequest()
                        .body(mapOf("error" to "生年月日は過去の日付を入力してください"))
                }
                date
            } catch (e: Exception) {
                return ResponseEntity.badRequest()
                    .body(mapOf("error" to "生年月日の形式が不正です（YYYY-MM-DD）"))
            }
        }

        try {
            val userInfo = userInfoService.updateUserInfo(userId, request.gender, birthDate)
            return ResponseEntity.ok(mapOf<String, Any?>(
                "success" to true,
                "message" to "ユーザー情報を更新しました",
                "gender" to userInfo.gender,
                "birthDate" to userInfo.birthDate?.toString()
            ))
        } catch (e: Exception) {
             return ResponseEntity.badRequest()
                    .body(mapOf("error" to e.message))
        }
    }
}
