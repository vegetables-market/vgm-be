package com.example.myapp.controller.auth.password

import com.example.myapp.dto.auth.password.PasswordResetRequest
import com.example.myapp.service.auth.password.PasswordResetService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/auth/password")
class PasswordResetController(
    private val passwordResetService: PasswordResetService
) {

    @PostMapping("/reset")
    fun resetPassword(@RequestBody request: PasswordResetRequest): ResponseEntity<Void> {
        passwordResetService.resetPassword(request.token, request.newPassword)
        return ResponseEntity.ok().build()
    }
}
