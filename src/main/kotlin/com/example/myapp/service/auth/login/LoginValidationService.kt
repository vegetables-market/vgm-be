package com.example.myapp.service.auth.login

import com.example.myapp.dto.auth.login.LoginRequest
import com.example.myapp.dto.auth.login.LoginResponse
import com.example.myapp.entity.user.User
import com.example.myapp.repository.user.UserRepository
import com.example.myapp.repository.user.email.UserEmailRepository
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service

/**
 * ログインバリデーションサービス
 * ユーザーの存在確認、パスワード照合などを担当する
 */
@Service
class LoginValidationService(
    private val userRepository: UserRepository,
    private val userEmailRepository: UserEmailRepository
) {
    private val passwordEncoder = BCryptPasswordEncoder()

    /**
     * ログインリクエストを検証し、有効なユーザーを返す
     *
     * @param request ログインリクエスト
     * @return 認証に成功したユーザー、失敗した場合はLoginResponse(エラー)を含む例外を投げる設計にするか、
     *         あるいは呼び出し元で判定するために null を返すか。
     *         ここでは LoginService のロジックに合わせて、
     *         - パスワード空 -> エラーResponse
     *         - ユーザーなし/パスワード不一致 -> エラーResponse
     *         を返す形にするため、戻り値は `User?` ではなく `LoginValidationResult` のような形が良いが、
     *         LoginService の中で分岐しているので、ここでは判定結果だけを返すのが疎結合。
     *         しかし元のロジックを忠実に移行するため、Userを返すか、エラー情報を返すかにする。
     */
    fun validateUser(request: LoginRequest): ValidationResult {
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
            return ValidationResult.Error(
                LoginResponse(status = "PASSWORD_REQUIRED", user = null)
            )
        }

        // パスワードチェック
        val isPasswordCorrect = user != null && passwordEncoder.matches(request.password, user.passwordHash)
        if (!isPasswordCorrect) {
             return ValidationResult.Error(
                LoginResponse(
                    status = "INVALID_CREDENTIALS",
                    user = null,
                    message = "ユーザー名またはパスワードが間違っています"
                )
            )
        }

        return ValidationResult.Success(user!!)
    }

    /**
     * 識別子（ユーザー名またはメールアドレス）からユーザーを取得する
     */
    fun getUserByIdentifier(identifier: String): User? {
        // Try username first
        var user = userRepository.findByUsername(identifier)
        if (user == null) {
            // Try email
            val emailRecord = userEmailRepository.findByEmail(identifier)
            if (emailRecord != null) {
                user = userRepository.findById(emailRecord.userId).orElse(null)
            }
        }
        return user
    }
    
    /**
     * ユーザーIDからユーザーを取得
     */
    fun getUserById(userId: Int): User? {
        return userRepository.findById(userId).orElse(null)
    }

    sealed class ValidationResult {
        data class Success(val user: User) : ValidationResult()
        data class Error(val response: LoginResponse) : ValidationResult()
    }
}
