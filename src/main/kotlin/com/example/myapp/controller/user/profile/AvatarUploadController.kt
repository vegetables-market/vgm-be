package com.example.myapp.controller.user.profile

import com.example.myapp.controller.common.getAppUser
import com.example.myapp.service.auth.session.AppCookieService
import com.example.myapp.service.auth.session.SessionService
import com.example.myapp.service.market.media.GenerateUploadToken
import com.example.myapp.service.user.profile.UserProfileService
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

data class UpdateAvatarRequest(
    val filename: String
)

@RestController
@RequestMapping("/v1/user/profile")
class AvatarUploadController(
    private val appCookieService: AppCookieService,
    private val sessionService: SessionService,
    private val generateUploadToken: GenerateUploadToken,
    private val userProfileService: UserProfileService
) {
    companion object {
        private const val DEFAULT_AVATAR_IMAGE = "/images/no-image.png"
    }

    /**
     * アバター画像アップロード用トークン生成
     */
    @PostMapping("/avatar/upload-token")
    fun getAvatarUploadToken(
        servletRequest: HttpServletRequest
    ): ResponseEntity<Map<String, Any>> {
        val (userId, _) = servletRequest.getAppUser(appCookieService, sessionService)
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(mapOf("error" to "ログインが必要です"))
        }

        val tokenResponse = generateUploadToken(userId, "USER")

        return ResponseEntity.ok(mapOf(
            "token" to tokenResponse.token,
            "filename" to tokenResponse.filename,
            "expiresAt" to tokenResponse.expiresAt
        ))
    }

    /**
     * vgm-media にアップロード済みのアバター画像をユーザープロフィールへ紐付け
     */
    @PutMapping("/avatar")
    fun updateAvatar(
        @RequestBody request: UpdateAvatarRequest,
        servletRequest: HttpServletRequest
    ): ResponseEntity<Map<String, Any>> {
        return saveAvatarFilename(request, servletRequest)
    }

    /**
     * vgm-media にアップロード済みのアバター画像をユーザープロフィールへ紐付け
     * （新エンドポイント）
     */
    @PostMapping("/upload-avatar")
    fun uploadAvatar(
        @RequestBody request: UpdateAvatarRequest,
        servletRequest: HttpServletRequest
    ): ResponseEntity<Map<String, Any>> {
        return saveAvatarFilename(request, servletRequest)
    }

    /**
     * 現在のユーザーのアバターURLを返却
     * （新エンドポイント）
     */
    @GetMapping("/get-avatar")
    fun getAvatar(
        servletRequest: HttpServletRequest
    ): ResponseEntity<Map<String, Any?>> {
        val (userId, _) = servletRequest.getAppUser(appCookieService, sessionService)
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(mapOf("error" to "ログインが必要です"))
        }

        val profile = userProfileService.getProfile(userId)
        val avatarUrl = profile?.profileImageUrl ?: DEFAULT_AVATAR_IMAGE
        return ResponseEntity.ok(
            mapOf(
                "avatarUrl" to avatarUrl
            )
        )
    }

    private fun saveAvatarFilename(
        request: UpdateAvatarRequest,
        servletRequest: HttpServletRequest
    ): ResponseEntity<Map<String, Any>> {
        val (userId, _) = servletRequest.getAppUser(appCookieService, sessionService)
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(mapOf("error" to "ログインが必要です"))
        }

        val filename = request.filename.trim()
        if (filename.isBlank()) {
            return ResponseEntity.badRequest()
                .body(mapOf("error" to "filename is required"))
        }
        if (filename.contains("/") || filename.contains("\\")) {
            return ResponseEntity.badRequest()
                .body(mapOf("error" to "invalid filename"))
        }

        try {
            userProfileService.updateAvatarUrl(userId, filename)

            return ResponseEntity.ok(mapOf(
                "success" to true,
                "avatarUrl" to filename,
                "message" to "プロフィール画像を更新しました"
            ))
        } catch (e: Exception) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "画像のアップロードに失敗しました"))
        }
    }
}
