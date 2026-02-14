package com.example.myapp.service.auth.login

import com.example.myapp.dto.auth.login.CheckUserResponse
import com.example.myapp.repository.user.UserRepository
import com.example.myapp.repository.user.email.UserEmailRepository
import com.example.myapp.service.email.verification.SendVerificationEmail
import org.springframework.stereotype.Service

@Service
class CheckUser(
    private val userRepository: UserRepository,
    private val userEmailRepository: UserEmailRepository,
    private val sendVerificationEmail: SendVerificationEmail
) {
    operator fun invoke(identifier: String): CheckUserResponse {
        // メール形式かどうかチェック（簡易）
        val isEmail = identifier.contains("@")

        if (isEmail) {
            // メールアドレスの場合: ユーザー検索
            val userEmail = userEmailRepository.findByEmail(identifier)
            if (userEmail != null) {
                // 登録済みユーザー -> OTP送信
                val (flowId, _, _) = sendVerificationEmail(userEmail.userId, identifier)
                return CheckUserResponse(
                    nextStep = "email_otp",
                    identifier = identifier,
                    flowId = flowId
                )
            } else {
                // 未登録ユーザー -> 新規登録フロー開始 (メール所有確認)
                val (flowId, _, _) = sendVerificationEmail.sendPreRegistration(identifier)
                return CheckUserResponse(
                    nextStep = "email_otp",
                    identifier = identifier,
                    flowId = flowId
                )
            }
        } else {
            // ユーザー名の場合: 常に "password"
            // (ユーザーが存在しなくてもパスワード入力を求める -> Account Enumeration対策)
            return CheckUserResponse(
                nextStep = "password",
                identifier = identifier
            )
        }
    }
}
