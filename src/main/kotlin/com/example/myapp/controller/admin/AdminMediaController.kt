package com.example.myapp.controller.admin

import com.example.myapp.entity.auth.UserSession
import com.example.myapp.repository.user.UserRepository
import com.example.myapp.service.market.media.GenerateUploadToken
import com.example.myapp.service.market.media.UploadTokenResponse
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

data class AdminUploadRequest(
    val filename: String
)

@RestController
@RequestMapping("/v1/admin/media")
class AdminMediaController(
    private val generateUploadToken: GenerateUploadToken,
    private val userRepository: UserRepository
) {

    @PostMapping("/upload-token")
    fun getAdminUploadToken(
        @AuthenticationPrincipal userSession: UserSession,
        @RequestBody request: AdminUploadRequest
    ): UploadTokenResponse {
        // ユーザー情報を取得してロールを確認
        val user = userRepository.findById(userSession.userId)
            .orElseThrow { ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found") }

        if (user.role != "ADMIN") {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied")
        }

        return generateUploadToken(user.userId, "ADMIN", request.filename)
    }
}
