package com.example.myapp.controller.auth.recovery

import com.example.myapp.dto.auth.recovery.*
import com.example.myapp.service.auth.recovery.AccountRecoveryService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/v1/auth/recovery")
class AccountRecoveryController(
    private val accountRecoveryService: AccountRecoveryService
) {

    @PostMapping("/start")
    fun startRecovery(@RequestBody request: StartRecoveryRequest): ResponseEntity<StartRecoveryResponse> {
        val state = accountRecoveryService.startRecovery(request.username)
        return ResponseEntity.ok(StartRecoveryResponse(state))
    }

    @GetMapping("/options")
    fun getOptions(@RequestParam state: String): ResponseEntity<GetOptionsResponse> {
        val options = accountRecoveryService.getOptions(state)
        return ResponseEntity.ok(GetOptionsResponse(options))
    }

    @PostMapping("/send")
    fun sendChallenge(@RequestBody request: SendChallengeRequest): ResponseEntity<Void> {
        accountRecoveryService.sendChallenge(request.state, request.method)
        return ResponseEntity.ok().build()
    }

    @PostMapping("/verify")
    fun verifyChallenge(@RequestBody request: VerifyChallengeRequest): ResponseEntity<VerifyChallengeResponse> {
        val isValid = accountRecoveryService.verifyChallenge(request.state, request.method, request.code)
        return ResponseEntity.ok(VerifyChallengeResponse(isValid))
    }

    @PostMapping("/complete")
    fun completeRecovery(@RequestBody request: CompleteRecoveryRequest): ResponseEntity<Void> {
        accountRecoveryService.completeRecovery(request.state)
        return ResponseEntity.ok().build()
    }

    @PostMapping("/forgot-id")
    fun forgotId(@RequestBody request: ForgotIdRequest): ResponseEntity<Void> {
        // TODO: Implement proper rate limiting (IP/Email based)
        // Simple delay to mitigate timing attacks
        Thread.sleep(500) 
        
        accountRecoveryService.sendIdReminder(request.email)
        return ResponseEntity.ok().build()
    }
}
