package com.example.myapp.controller.auth

import com.example.myapp.dto.WebAuthnRegistrationFinishRequest
import com.example.myapp.dto.WebAuthnRegistrationStartResponse
import com.example.myapp.dto.WebAuthnAuthenticationFinishRequest
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
        if (principal == null) return ResponseEntity.status(401).build()
        
        val user = userRepository.findByUsername(principal.name) ?: throw RuntimeException("User not found")
        
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
        val user = userRepository.findByUsername(principal.name) ?: throw RuntimeException("User not found")

        webAuthnService.finishRegistration(
            user, 
            session, 
            request.credentialId, 
            request.response, 
            request.credentialName
        )
        return ResponseEntity.ok("Passkey registered")
    }

    @GetMapping("/credentials")
    fun listCredentials(@AuthenticationPrincipal principal: Principal?): ResponseEntity<List<com.example.myapp.dto.UserCredentialResponse>> {
        if (principal == null) return ResponseEntity.status(401).build()
        val user = userRepository.findByUsername(principal.name) ?: throw RuntimeException("User not found")
        
        return ResponseEntity.ok(webAuthnService.getCredentials(user))
    }

    @DeleteMapping("/credentials/{credentialId}")
    fun deleteCredential(
        @PathVariable credentialId: String,
        @AuthenticationPrincipal principal: Principal?
    ): ResponseEntity<String> {
        if (principal == null) return ResponseEntity.status(401).build()
        val user = userRepository.findByUsername(principal.name) ?: throw RuntimeException("User not found")

        webAuthnService.deleteCredential(user, credentialId)
        return ResponseEntity.ok("Credential deleted")
    }

    @PostMapping("/login/start")
    fun startLogin(session: HttpSession): ResponseEntity<com.webauthn4j.data.PublicKeyCredentialRequestOptions> {
        val options = webAuthnService.startLogin(session)
        return ResponseEntity.ok(options)
    }
    
    @PostMapping("/login/finish")
    fun finishLogin(
        @RequestBody request: WebAuthnAuthenticationFinishRequest,
        session: HttpSession
    ): ResponseEntity<String> {
        val user = webAuthnService.finishLogin(session, request.credentialId, request.response)
        
        // Manual Login
        val authorities = org.springframework.security.core.authority.AuthorityUtils.createAuthorityList("ROLE_USER")
        val auth = org.springframework.security.authentication.UsernamePasswordAuthenticationToken(user.username, null, authorities)
        SecurityContextHolder.getContext().authentication = auth
        
        // Save to session explicitly to ensure persistence across requests
        session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext())
        
        return ResponseEntity.ok("Login successful")
    }
}
