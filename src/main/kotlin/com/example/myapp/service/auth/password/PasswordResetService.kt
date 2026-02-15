package com.example.myapp.service.auth.password

import com.example.myapp.entity.auth.PasswordResetToken
import com.example.myapp.repository.auth.PasswordResetTokenRepository
import com.example.myapp.repository.user.UserRepository
import com.example.myapp.repository.user.email.UserEmailRepository
import com.example.myapp.service.email.EmailSenderService
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.security.MessageDigest
import java.time.LocalDateTime
import java.util.Base64
import java.util.UUID

@Service
class PasswordResetService(
    private val passwordResetTokenRepository: PasswordResetTokenRepository,
    private val userRepository: UserRepository,
    private val userEmailRepository: UserEmailRepository,
    private val emailSenderService: EmailSenderService,
    private val passwordEncoder: PasswordEncoder
) {

    companion object {
        const val TOKEN_TTL_MINUTES = 30L
    }

    @Transactional
    fun sendResetEmail(userId: Int) {
        val email = userEmailRepository.findByUserIdAndIsPrimaryTrue(userId)
            ?: throw IllegalStateException("No primary email found")

        // Invalidate old tokens? Or just let them expire.
        // Better to be safe and clean up if needed, but for now we generate a new one.

        // Generate Token
        val rawToken = UUID.randomUUID().toString()
        val tokenHash = hashToken(rawToken)

        val tokenEntity = PasswordResetToken(
            userId = userId,
            tokenHash = tokenHash,
            expiresAt = LocalDateTime.now().plusMinutes(TOKEN_TTL_MINUTES)
        )
        passwordResetTokenRepository.save(tokenEntity)

        // Send Email
        val link = "http://localhost:3000/auth/reset-password?token=$rawToken" // TODO: Use config for base URL
        val htmlContent = """
            <p>パスワード再設定のリクエストを受け付けました。</p>
            <p>以下のリンクをクリックしてパスワードを再設定してください（30分間有効）：</p>
            <p><a href="$link">$link</a></p>
            <p>心当たりがない場合は、このメールを破棄してください。</p>
        """.trimIndent()

        emailSenderService.sendHtmlEmail(email.email, "パスワード再設定", htmlContent)
    }

    @Transactional
    fun resetPassword(rawToken: String, newPassword: String) {
        val tokenHash = hashToken(rawToken)
        val tokenEntity = passwordResetTokenRepository.findByTokenHash(tokenHash)
            .orElseThrow { IllegalArgumentException("Invalid or expired token") }

        if (tokenEntity.isUsed || tokenEntity.expiresAt.isBefore(LocalDateTime.now())) {
            throw IllegalArgumentException("Invalid or expired token")
        }

        // Update Password
        val user = userRepository.findById(tokenEntity.userId)
            .orElseThrow { IllegalStateException("User not found") }
        
        user.passwordHash = passwordEncoder.encode(newPassword)
        userRepository.save(user)

        // Mark token as used
        tokenEntity.isUsed = true
        passwordResetTokenRepository.save(tokenEntity)
    }

    private fun hashToken(token: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(token.toByteArray(Charsets.UTF_8))
        return Base64.getEncoder().encodeToString(hash)
    }
}
