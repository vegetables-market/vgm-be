package com.example.myapp.controller

import com.example.myapp.repository.UserRepository
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal



@RestController
@RequestMapping("/api/users")
class UserPreferenceController(
    private val userRepository: UserRepository
) {

    /**
     * ユーザーのテーマを取得します。
     * 注意: 実際の DB スキーマによっては `users.theme` のカラム名が異なる場合があります。
     */
    @GetMapping("/{userId}/theme")
    fun getTheme(@PathVariable userId: Long): ResponseEntity<Map<String, String?>> {
        val user = userRepository.findById(userId).orElse(null) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(mapOf("theme" to (user.theme ?: "light")))
    }

    /**
     * ユーザーのテーマを更新します。値は "light" または "dark" を想定しています。
     */
    @PutMapping("/{userId}/theme")
    fun updateTheme(@PathVariable userId: Long, @RequestBody body: Map<String, Any>): ResponseEntity<Map<String, String?>> {
        val newTheme = parseThemeFromBody(body) ?: return ResponseEntity.badRequest().body(mapOf("error" to "theme is required or invalid"))
        val user = userRepository.findById(userId).orElse(null) ?: return ResponseEntity.notFound().build()
        user.theme = newTheme
        userRepository.save(user)
        return ResponseEntity.ok(mapOf("theme" to user.theme))
    }

    // Update theme for the currently authenticated user
    @PreAuthorize("isAuthenticated()")
    @PutMapping("/me/theme")
    fun updateMyTheme(@AuthenticationPrincipal userSession: com.example.myapp.entity.auth.UserSession?, @RequestBody body: Map<String, Any>): ResponseEntity<Map<String, String?>> {
        if (userSession == null) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED).build()
        }

        val newTheme = parseThemeFromBody(body) ?: return ResponseEntity.badRequest().body(mapOf("error" to "theme is required or invalid"))

        val userId = userSession.userId
        val user = userRepository.findById(userId).orElse(null) ?: return ResponseEntity.notFound().build()
        user.theme = newTheme
        userRepository.save(user)
        return ResponseEntity.ok(mapOf("theme" to user.theme))
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/me/theme")
    fun getMyTheme(@AuthenticationPrincipal userSession: com.example.myapp.entity.auth.UserSession?): ResponseEntity<Map<String, Int>> {
        if (userSession == null) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED).build()
        }

        val userId = userSession.userId
        val user = userRepository.findById(userId).orElse(null) ?: return ResponseEntity.notFound().build()
        val numeric = if (user.theme == "dark") 1 else 0
        return ResponseEntity.ok(mapOf("theme" to numeric))
    }

    // Helper to accept either numeric (0/1) or string ("light"/"dark") theme values
    private fun parseThemeFromBody(body: Map<String, Any>): String? {
        val raw = body["theme"] ?: return null
        return when (raw) {
            is Number -> {
                when (raw.toInt()) {
                    0 -> "light"
                    1 -> "dark"
                    else -> null
                }
            }
            is String -> {
                when (raw) {
                    "0" -> "light"
                    "1" -> "dark"
                    "light", "dark" -> raw
                    else -> null
                }
            }
            else -> null
        }
    }
}
