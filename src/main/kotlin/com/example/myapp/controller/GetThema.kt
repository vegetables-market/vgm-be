package com.example.myapp.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import com.example.myapp.service.UserService

@RestController
class ThemeController(val userService: UserService) {
    @GetMapping("/api/users/{id}/theme")
    fun getTheme(@PathVariable id: Long): Map<String, Any> {
        val theme = userService.getTheme(id)
        return mapOf("theme" to theme)
    }
}