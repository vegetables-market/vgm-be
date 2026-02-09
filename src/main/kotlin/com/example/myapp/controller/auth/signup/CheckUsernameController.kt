package com.example.myapp.controller.auth.signup

import com.example.myapp.service.auth.signup.SignupService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/auth")
class CheckUsernameController(
    private val signupService: SignupService,
) {

    @GetMapping("/check-username")
    fun checkUsername(@RequestParam username: String): ResponseEntity<Map<String, Any>> {
        val available = signupService.isUsernameAvailable(username)
        val suggestions = signupService.generateUsernameSuggestions(username)
        val response = mutableMapOf<String, Any>(
            "available" to available,
            "suggestions" to suggestions
        )
        
        if (!available) {
            response["message"] = "このユーザー名は既に使用されています"
        }
        
        return ResponseEntity.ok(response)
    }
}
