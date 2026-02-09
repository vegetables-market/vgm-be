package com.example.myapp.service.auth.login

import com.example.myapp.dto.auth.login.LoginRequest
import com.example.myapp.dto.auth.login.LoginResponse
import com.example.myapp.entity.user.User
import com.example.myapp.repository.user.UserRepository
import com.example.myapp.repository.user.email.UserEmailRepository
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service

/**
 * ログインリクエスト検証ユースケース
 * ユーザーの存在確認、パスワード照合などを担当する
 */
@Service
class ValidateLoginRequest(
    private val userRepository: UserRepository,
    private val userEmailRepository: UserEmailRepository
) {
    private val passwordEncoder = BCryptPasswordEncoder()

    sealed class Result {
        data class Success(val user: User) : Result()
        data class Error(val response: LoginResponse) : Result()
    }

    /**
     * ログインリクエストを検証し、有効なユーザーを返す
     */
    operator fun invoke(request: LoginRequest): Result {
        // ユーザー名またはメールでユーザーを検索
        var user = userRepository.findByUsername(request.username)
        if (user == null) {
            // メールで検索
            val emailRecord = userEmailRepository.findByEmail(request.username)
            if (emailRecord != null) {
                user = userRepository.findById(emailRecord.userId).orElse(null)
            }
        }

        if (request.password == null) {
            return Result.Error(
                LoginResponse(status = "PASSWORD_REQUIRED", user = null)
            )
        }

        // パスワードチェック
        val isPasswordCorrect = user != null && passwordEncoder.matches(request.password, user.passwordHash)
        if (!isPasswordCorrect) {
             return Result.Error(
                LoginResponse(
                    status = "INVALID_CREDENTIALS",
                    user = null,
                    message = "ユーザー名またはパスワードが間違っています"
                )
            )
        }

        return Result.Success(user!!)
    }
}
