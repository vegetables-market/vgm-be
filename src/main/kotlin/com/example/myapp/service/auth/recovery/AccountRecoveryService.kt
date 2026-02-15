package com.example.myapp.service.auth.recovery

import com.example.myapp.entity.auth.RecoverySession
import com.example.myapp.entity.auth.VerificationCode
import com.example.myapp.repository.auth.RecoverySessionRepository
import com.example.myapp.repository.auth.VerificationCodeRepository
import com.example.myapp.repository.user.UserRepository
import com.example.myapp.repository.user.email.UserEmailRepository
import com.example.myapp.service.auth.MfaService
import com.example.myapp.service.auth.password.PasswordResetService
import com.example.myapp.service.email.EmailSenderService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

@Service
class AccountRecoveryService(
    private val recoverySessionRepository: RecoverySessionRepository,
    private val userRepository: UserRepository,
    private val userEmailRepository: UserEmailRepository,
    private val verificationCodeRepository: VerificationCodeRepository,
    private val mfaService: MfaService,
    private val emailSenderService: EmailSenderService,
    private val passwordResetService: PasswordResetService
) {

    // Status Constants
    companion object {
        const val STATUS_CREATED = "CREATED"
        const val STATUS_CHALLENGE_SENT = "CHALLENGE_SENT"
        const val STATUS_VERIFIED = "VERIFIED"
        const val STATUS_COMPLETED = "COMPLETED"
        const val STATUS_LOCKED = "LOCKED"
        const val STATUS_EXPIRED = "EXPIRED"

        const val METHOD_EMAIL = "email"
        const val METHOD_TOTP = "totp"
        
        const val SESSION_TTL_MINUTES = 10L
        const val MAX_ATTEMPTS = 5
        const val CODE_TTL_MINUTES = 10L
    }

    /**
     * Start recovery process
     * Returns a session ID (state)
     */
    @Transactional
    fun startRecovery(username: String): String {
        // Find user by username or email
        var user = userRepository.findByUsername(username)
        if (user == null) {
            val emails = userEmailRepository.findByEmail(username)
            if (emails != null) { // Note: findByEmail returns UserEmail? (single) based on interface def in step 146
                 user = userRepository.findById(emails.userId).orElse(null)
            }
        }

        if (user == null) {
            throw IllegalArgumentException("User not found") 
        }

        // Create Session
        val sessionId = UUID.randomUUID().toString()
        val session = RecoverySession(
            sessionId = sessionId,
            userId = user.userId,
            status = STATUS_CREATED,
            expiresAt = LocalDateTime.now().plusMinutes(SESSION_TTL_MINUTES)
        )
        recoverySessionRepository.save(session)

        return sessionId
    }

    /**
     * Get available recovery options
     */
    fun getOptions(sessionId: String): List<String> {
        val session = getValidSession(sessionId)
        val options = mutableListOf<String>()

        // Check Email
        val email = userEmailRepository.findByUserIdAndIsPrimaryTrue(session.userId)
        if (email != null) {
            options.add(METHOD_EMAIL)
        }

        // Check TOTP
        if (mfaService.isTotpEnabled(session.userId)) {
            options.add(METHOD_TOTP)
        }

        return options
    }

    /**
     * Send challenge code (e.g. Email)
     */
    @Transactional
    fun sendChallenge(sessionId: String, method: String) {
        val session = getValidSession(sessionId)
        
        // Allow resend even if SENT
        if (session.status != STATUS_CREATED && session.status != STATUS_CHALLENGE_SENT) {
            throw IllegalStateException("Invalid status for sending challenge")
        }

        when (method) {
            METHOD_EMAIL -> {
                val email = userEmailRepository.findByUserIdAndIsPrimaryTrue(session.userId)
                    ?: throw IllegalStateException("No primary email found")
                
                // Generate Code
                val code = (100000..999999).random().toString()
                
                // Save Code
                // Invalidate potential old codes for this flow
                val expiresAt = LocalDateTime.now().plusMinutes(CODE_TTL_MINUTES)
                val newCode = VerificationCode(
                    userId = session.userId,
                    email = email.email,
                    code = code,
                    type = "PASSWORD_RESET",
                    flowId = sessionId,
                    expiresAt = expiresAt
                )
                verificationCodeRepository.save(newCode)
                
                emailSenderService.sendHtmlEmail(email.email, "パスワード再設定確認コード", "<p>あなたの確認コードは <b>$code</b> です。</p>")
                
                // Update Session
                session.status = STATUS_CHALLENGE_SENT
                recoverySessionRepository.save(session)
            }
            METHOD_TOTP -> {
                // No send needed for TOTP
                session.status = STATUS_CHALLENGE_SENT
                recoverySessionRepository.save(session)
            }
            else -> throw IllegalArgumentException("Unsupported method")
        }
    }

    /**
     * Verify challenge
     */
    @Transactional
    fun verifyChallenge(sessionId: String, method: String, code: String): Boolean {
        val session = getValidSession(sessionId)
        
        // Rate limit check
        if (session.attemptCount >= MAX_ATTEMPTS) {
            session.status = STATUS_LOCKED
            recoverySessionRepository.save(session)
            throw IllegalStateException("Too many failed attempts. Session locked.")
        }

        var isValid = false
        when (method) {
            METHOD_EMAIL -> {
                // Find valid code by flowId
                val verificationCode = verificationCodeRepository.findByFlowIdAndCodeAndIsUsedFalseAndExpiresAtAfter(
                    flowId = sessionId,
                    code = code,
                    now = LocalDateTime.now()
                )
                
                if (verificationCode != null) {
                    isValid = true
                    verificationCode.isUsed = true
                    verificationCodeRepository.save(verificationCode)
                }
            }
            METHOD_TOTP -> {
                isValid = mfaService.verifyCode(session.userId, code)
            }
        }

        if (isValid) {
            session.status = STATUS_VERIFIED
            recoverySessionRepository.save(session)
            return true
        } else {
            session.attemptCount++
            recoverySessionRepository.save(session)
            return false
        }
    }

    /**
     * Complete recovery (Send Reset Email)
     */
    @Transactional
    fun completeRecovery(sessionId: String) {
        val session = getValidSession(sessionId)
        if (session.status != STATUS_VERIFIED && session.status != STATUS_COMPLETED) { // Allow retry if verified
             throw IllegalStateException("Session not verified")
        }

        // Send Password Reset Email
        passwordResetService.sendResetEmail(session.userId)

        session.status = STATUS_COMPLETED
        recoverySessionRepository.save(session)
    }

    private fun getValidSession(sessionId: String): RecoverySession {
        val session = recoverySessionRepository.findById(sessionId)
            .orElseThrow { IllegalArgumentException("Session not found") }

        if (session.status == STATUS_LOCKED) {
             throw IllegalStateException("Session is locked")
        }
        
        if(session.status == STATUS_EXPIRED || session.expiresAt.isBefore(LocalDateTime.now())) {
             session.status = STATUS_EXPIRED
             recoverySessionRepository.save(session)
             throw IllegalStateException("Session expired")
        }

        return session
    }
}
