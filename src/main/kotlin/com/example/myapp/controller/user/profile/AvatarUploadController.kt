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
import org.springframework.web.multipart.MultipartFile
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.time.LocalDateTime

@RestController
@RequestMapping("/v1/user/profile")
class AvatarUploadController(
    private val appCookieService: AppCookieService,
    private val sessionService: SessionService,
    private val generateUploadToken: GenerateUploadToken,
    private val userProfileService: UserProfileService
) {

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
     * アバター画像アップロード
     */
    @PostMapping("/avatar")
    fun uploadAvatar(
        @RequestParam("image") file: MultipartFile,
        servletRequest: HttpServletRequest
    ): ResponseEntity<Map<String, Any>> {
        val (userId, _) = servletRequest.getAppUser(appCookieService, sessionService)
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(mapOf("error" to "ログインが必要です"))
        }

        // ファイルサイズチェック (5MB)
        if (file.size > 5 * 1024 * 1024) {
            return ResponseEntity.badRequest()
                .body(mapOf("error" to "ファイルサイズは5MB以下にしてください"))
        }

        // ファイル形式チェック
        val contentType = file.contentType
        if (contentType !in listOf("image/jpeg", "image/png", "image/webp")) {
            return ResponseEntity.badRequest()
                .body(mapOf("error" to "JPEG、PNG、WebP形式の画像のみアップロード可能です"))
        }

        try {
            // ファイル名生成
            val extension = when (contentType) {
                "image/jpeg" -> "jpg"
                "image/png" -> "png"
                "image/webp" -> "webp"
                else -> "jpg"
            }
            val filename = "avatar_${userId}_${System.currentTimeMillis()}.$extension"

            // 保存先ディレクトリ
            val uploadDir = Paths.get("uploads/avatars")
            Files.createDirectories(uploadDir)

            // ファイル保存
            val filePath = uploadDir.resolve(filename)
            Files.copy(file.inputStream, filePath, StandardCopyOption.REPLACE_EXISTING)

            // URLを生成（実際の環境に応じて調整）
            val avatarUrl = "/uploads/avatars/$filename"

            // プロフィールを更新
            userProfileService.updateAvatarUrl(userId, avatarUrl)

            return ResponseEntity.ok(mapOf(
                "success" to true,
                "avatarUrl" to avatarUrl,
                "message" to "プロフィール画像を更新しました"
            ))
        } catch (e: Exception) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "画像のアップロードに失敗しました"))
        }
    }
}
