package com.example.myapp.controller.auth

import com.example.myapp.dto.auth.SignupRequest

import com.example.myapp.service.auth.SignupService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/v1/auth")
class SignUpController(
    private val signupService: SignupService,
) {

    @PostMapping("/signup")
    fun signup(@RequestBody request: SignupRequest): ResponseEntity<Any> {
        return try {
            val response = signupService.signup(request)
            ResponseEntity.status(HttpStatus.CREATED).body(response)
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(mapOf("message" to e.message))
        }
    }


}
