package com.example.myapp.controller.auth

import com.example.myapp.dto.WebAuthnRegistrationFinishRequest
import com.example.myapp.dto.WebAuthnRegistrationStartResponse
import com.example.myapp.entity.user.User
import com.example.myapp.repository.user.UserRepository
import com.example.myapp.service.auth.WebAuthnService
import com.webauthn4j.data.PublicKeyCredentialCreationOptions
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpSession
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*
import java.security.Principal

@RestController
@RequestMapping("/v1/auth/webauthn")
class WebAuthnController(
    private val webAuthnService: WebAuthnService,
    private val userRepository: UserRepository
) {

    @PostMapping("/register/start")
    fun startRegistration(
        session: HttpSession,
        @AuthenticationPrincipal principal: Principal?
    ): ResponseEntity<PublicKeyCredentialCreationOptions> {
        // In a real app, you get the authenticated user from SecurityContext
        // For this demo/skeleton, we assume user is logged in via session or JWT
        // If principal is null, we return 401
        if (principal == null) return ResponseEntity.status(401).build()
        
        val user = userRepository.findByUsername(principal.name).orElseThrow { RuntimeException("User not found") }
        
        val options = webAuthnService.startRegistration(user, session)
        return ResponseEntity.ok(options)
    }

    @PostMapping("/register/finish")
    fun finishRegistration(
        @RequestBody request: WebAuthnRegistrationFinishRequest,
        session: HttpSession,
        @AuthenticationPrincipal principal: Principal?
    ): ResponseEntity<String> {
        if (principal == null) return ResponseEntity.status(401).build()
        val user = userRepository.findByUsername(principal.name).orElseThrow { RuntimeException("User not found") }

        webAuthnService.finishRegistration(
            user, 
            session, 
            request.credentialId, 
            request.response, 
            request.credentialName
        )
        return ResponseEntity.ok("Passkey registered")
    }
}
