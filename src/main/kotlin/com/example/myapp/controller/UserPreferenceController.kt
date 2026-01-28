package com.example.myapp.controller

import com.example.myapp.repository.UserRepository
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

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
    fun updateTheme(@PathVariable userId: Long, @RequestBody body: Map<String, String>): ResponseEntity<Map<String, String?>> {
        val newTheme = body["theme"] ?: return ResponseEntity.badRequest().body(mapOf("error" to "theme is required"))
        if (newTheme !in listOf("light", "dark")) {
            return ResponseEntity.badRequest().body(mapOf("error" to "invalid theme"))
        }
        val user = userRepository.findById(userId).orElse(null) ?: return ResponseEntity.notFound().build()
        user.theme = newTheme
        userRepository.save(user)
        return ResponseEntity.ok(mapOf("theme" to user.theme))
    }
}
