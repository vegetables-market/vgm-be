package com.example.myapp.controller.auth.signup

import com.example.myapp.service.auth.signup.SignupService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/auth")
class SuggestionsController(
    private val signupService: SignupService,
) {

    @GetMapping("/suggestions")
    fun getInitialSuggestions(): ResponseEntity<Map<String, Any>> {
        val suggestions = signupService.getInitialSuggestions()
        return ResponseEntity.ok(mapOf("suggestions" to suggestions))
    }
}
