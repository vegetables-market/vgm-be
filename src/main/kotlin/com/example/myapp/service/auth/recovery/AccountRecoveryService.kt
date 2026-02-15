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
            if (emails != null) {
                 user = userRepository.findById(emails.userId).orElse(null)
            }
        }

        // Create Session (User may be null for decoy session)
        val sessionId = UUID.randomUUID().toString()
        val session = RecoverySession(
            sessionId = sessionId,
            userId = user?.userId, // Nullable
            status = STATUS_CREATED,
            expiresAt = LocalDateTime.now().plusMinutes(SESSION_TTL_MINUTES)
        )
        recoverySessionRepository.save(session)

        return sessionId
    }

    /**
     * Get available recovery options
     * Always returns constant options to prevent enumeration
     */
    fun getOptions(sessionId: String): List<String> {
        val session = findValidSession(sessionId)
        
        // If session is invalid or user not found, strictly mimic the response
        if (session == null || session.userId == null) {
            return listOf(METHOD_EMAIL)
        }

        // Real user options
        val options = mutableListOf<String>()

        // Check Email (Always added for valid user to match decoy)
        val email = userEmailRepository.findByUserIdAndIsPrimaryTrue(session.userId!!)
        if (email != null) {
            options.add(METHOD_EMAIL)
        } else {
             // Should theoretically not happen for a valid user in this flow, but fallback
             options.add(METHOD_EMAIL)
        }

        // Check TOTP
        if (mfaService.isTotpEnabled(session.userId!!)) {
            // For now, only returning email to keep response identical
            // If we want to support TOTP, we must ensure decoy also returns it if it was requested in a way that implies TOTP is possible?
            // Or just return email only for now as per plan.
            // Plan says: "recommended" is okay but list should be fixed.
            // Let's stick to ["email"] for now to be perfectly safe as per "options: constant response" task.
            // However, the previous code supported TOTP.
            // To be safe against enumeration, we should only return commonly available options.
            // If we return TOTP only for users who have it, that leaks info.
            // So we return ["email"] for everyone for now.
        }

        return listOf(METHOD_EMAIL)
    }

    /**
     * Send challenge code (e.g. Email)
     */
    @Transactional
    fun sendChallenge(sessionId: String, method: String) {
        val session = findValidSession(sessionId)

        // Invalid session or Decoy session -> Do nothing, return success (200 OK)
        if (session == null || session.userId == null) {
            return
        }
        
        // Allow resend even if SENT
        if (session.status != STATUS_CREATED && session.status != STATUS_CHALLENGE_SENT) {
            // Treat as success to avoid leaking state
            return
        }

        when (method) {
            METHOD_EMAIL -> {
                val email = userEmailRepository.findByUserIdAndIsPrimaryTrue(session.userId!!)
                
                if (email != null) {
                    // Generate Code
                    val code = (100000..999999).random().toString()
                    val expiresAt = LocalDateTime.now().plusMinutes(CODE_TTL_MINUTES)
                    
                    // Check if code exists for this flow
                    val existingCode = verificationCodeRepository.findByFlowId(sessionId)
                    
                    if (existingCode != null) {
                        existingCode.code = code
                        existingCode.expiresAt = expiresAt
                        existingCode.isUsed = false
                        existingCode.resendCount++
                        verificationCodeRepository.save(existingCode)
                    } else {
                        val newCode = VerificationCode(
                            userId = session.userId!!,
                            email = email.email,
                            code = code,
                            type = "PASSWORD_RESET",
                            flowId = sessionId,
                            expiresAt = expiresAt
                        )
                        verificationCodeRepository.save(newCode)
                    }
                    
                    emailSenderService.sendHtmlEmail(email.email, "パスワード再設定確認コード", "<p>あなたの確認コードは <b>$code</b> です。</p>")
                }
                
                // Update Session
                session.status = STATUS_CHALLENGE_SENT
                recoverySessionRepository.save(session)
            }
            METHOD_TOTP -> {
                // No send needed for TOTP
                session.status = STATUS_CHALLENGE_SENT
                recoverySessionRepository.save(session)
            }
            // Ignore unsupported methods
        }
    }

    /**
     * Verify challenge
     */
    @Transactional
    fun verifyChallenge(sessionId: String, method: String, code: String): Boolean {
        val session = findValidSession(sessionId)

        // Invalid or Decoy -> Always false
        if (session == null || session.userId == null) {
            // Simulate processing time if needed? For now, just return false.
            return false
        }
        
        // Rate limit check
        if (session.attemptCount >= MAX_ATTEMPTS) {
            session.status = STATUS_LOCKED
            recoverySessionRepository.save(session)
            // Locked session acts as invalid/failed verify
            return false
        }

        var isValid = false
        when (method) {
            METHOD_EMAIL -> {
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
                isValid = mfaService.verifyCode(session.userId!!, code)
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
     * Send ID reminder to recovery email
     * Security:
     * 1. Only sends to SUB/RECOVERY email types
     * 2. Masks the primary email ID in the body
     * 3. Always returns successfully to prevent enumeration
     */
    @Transactional
    fun sendIdReminder(email: String) {
        // 1. Find the email record
        val userEmail = userEmailRepository.findByEmail(email) ?: return

        // 2. Security Check: Must be SUB or RECOVERY type
        // Adjust these types based on your actual enum/string values
        if (userEmail.type != "SUB" && userEmail.type != "RECOVERY") {
            // Log security event?
            return
        }

        // 3. Find Primary Email (ID) for the user
        val primaryEmail = userEmailRepository.findByUserIdAndIsPrimaryTrue(userEmail.userId) ?: return
        
        // 4. Mask the ID
        val maskedId = maskEmail(primaryEmail.email)

        // 5. Send Email
        val subject = "[VGM] ログインIDのご案内"
        // TODO: Externalize URL configuration
        val loginUrl = "http://localhost:3000/login" 
        
        val body = """
            <p>お客様のログインID（メールアドレス）をお知らせします。</p>
            <p>ログインID候補: <b>$maskedId</b></p>
            <p>心当たりがある場合は、以下のリンクからログインしてください。</p>
            <p><a href="$loginUrl">ログイン画面へ</a></p>
            <br>
            <p>※このメールにお心当たりがない場合は、破棄してください。</p>
        """.trimIndent()

        emailSenderService.sendHtmlEmail(userEmail.email, subject, body)
    }

    private fun maskEmail(email: String): String {
        val parts = email.split("@")
        if (parts.size != 2) return email 
        
        val name = parts[0]
        val domain = parts[1]
        
        if (name.length <= 2) {
            return "*".repeat(name.length) + "@" + domain
        }
        
        // Show f***@domain
        // Or f***l@domain ?
        // User request: y***@g****.com
        // Let's do: first char + *** (len-1)
        
        val maskedName = name.first() + "*".repeat(name.length - 1)
        
        // Also mask domain? User example suggests masking domain too: g****.com
        val domainParts = domain.split(".")
        val maskedDomain = if (domainParts.size >= 2) {
             val domainName = domainParts[0]
             val domainSuffix = domainParts.drop(1).joinToString(".")
             if (domainName.length <= 1) {
                 domainName + "." + domainSuffix
             } else {
                 domainName.first() + "*".repeat(domainName.length - 1) + "." + domainSuffix
             }
        } else {
            domain
        }
        
        return "$maskedName@$maskedDomain"
    }

    /**
     * Complete recovery (Send Reset Email)
     */
    @Transactional
    fun completeRecovery(sessionId: String) {
        val session = findValidSession(sessionId)

        // Invalid or Decoy -> Do nothing, return success
        if (session == null || session.userId == null) {
            return
        }

        // Must be VERIFIED
        if (session.status != STATUS_VERIFIED && session.status != STATUS_COMPLETED) {
             // Return success to hide state
             return
        }

        // Send Password Reset Email
        passwordResetService.sendResetEmail(session.userId!!)

        session.status = STATUS_COMPLETED
        recoverySessionRepository.save(session)
    }

    /**
     * Helper to find a strictly valid session.
     * Returns null if not found, expired, locked, etc.
     */
    private fun findValidSession(sessionId: String): RecoverySession? {
        val sessionOpt = recoverySessionRepository.findById(sessionId)
        if (sessionOpt.isEmpty) {
            return null
        }
        val session = sessionOpt.get()

        if (session.status == STATUS_LOCKED) {
             return null
        }
        
        if (session.status == STATUS_EXPIRED || session.expiresAt.isBefore(LocalDateTime.now())) {
             // Mark as expired if not already? Or just treat as invalid.
             if (session.status != STATUS_EXPIRED) {
                 session.status = STATUS_EXPIRED
                 recoverySessionRepository.save(session)
             }
             return null
        }

        return session
    }
}
