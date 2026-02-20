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
    fun getTheme(@PathVariable userId: Long): ResponseEntity<Map<String, String>> {
        val user = userRepository.findById(userId).orElse(null) ?: return ResponseEntity.notFound().build()
        val themeStr = when (user.theme.toInt()) {
            0 -> "light"
            1 -> "dark"
            else -> "light"
        }
        return ResponseEntity.ok(mapOf("theme" to themeStr))
    }

    /**
     * ユーザーのテーマを更新します。値は "light" または "dark" を想定しています。
     */
    @PutMapping("/{userId}/theme")
    fun updateTheme(@PathVariable userId: Long, @RequestBody body: Map<String, Any>): ResponseEntity<Map<String, String>> {
        val newThemeByte = parseThemeFromBody(body) ?: return ResponseEntity.badRequest().body(mapOf("error" to "theme is required or invalid"))
        val user = userRepository.findById(userId).orElse(null) ?: return ResponseEntity.notFound().build()
        user.theme = newThemeByte
        userRepository.save(user)
        val themeStr = when (newThemeByte.toInt()) {
            0 -> "light"
            1 -> "dark"
            else -> "light"
        }
        return ResponseEntity.ok(mapOf("theme" to themeStr))
    }

    // Update theme for the currently authenticated user
    @PreAuthorize("isAuthenticated()")
    @PutMapping("/me/theme")
    fun updateMyTheme(@AuthenticationPrincipal userSession: com.example.myapp.entity.auth.UserSession?, @RequestBody body: Map<String, Any>): ResponseEntity<Map<String, String>> {
        if (userSession == null) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED).build()
        }

        val newThemeByte = parseThemeFromBody(body) ?: return ResponseEntity.badRequest().body(mapOf("error" to "theme is required or invalid"))

        val userId = userSession.userId
        val user = userRepository.findById(userId).orElse(null) ?: return ResponseEntity.notFound().build()
        user.theme = newThemeByte
        userRepository.save(user)
        val themeStr = when (newThemeByte.toInt()) {
            0 -> "light"
            1 -> "dark"
            else -> "light"
        }
        return ResponseEntity.ok(mapOf("theme" to themeStr))
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/me/theme")
    fun getMyTheme(@AuthenticationPrincipal userSession: com.example.myapp.entity.auth.UserSession?): ResponseEntity<Map<String, Int>> {
        if (userSession == null) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED).build()
        }

        val userId = userSession.userId
        val user = userRepository.findById(userId).orElse(null) ?: return ResponseEntity.notFound().build()
        val numeric = user.theme.toInt()
        return ResponseEntity.ok(mapOf("theme" to numeric))
    }

    // Helper to accept either numeric (0/1) or string ("light"/"dark") theme values
    // Returns Byte: 0 for light, 1 for dark
    private fun parseThemeFromBody(body: Map<String, Any>): Byte? {
        val raw = body["theme"] ?: return null
        return when (raw) {
            is Number -> {
                when (raw.toInt()) {
                    0 -> 0.toByte()
                    1 -> 1.toByte()
                    else -> null
                }
            }
            is String -> {
                when (raw) {
                    "0" -> 0.toByte()
                    "1" -> 1.toByte()
                    "light" -> 0.toByte()
                    "dark" -> 1.toByte()
                    else -> null
                }
            }
            else -> null
        }
    }
}
