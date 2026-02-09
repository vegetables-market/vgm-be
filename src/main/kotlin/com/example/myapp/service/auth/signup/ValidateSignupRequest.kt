package com.example.myapp.service.auth.signup

import com.example.myapp.dto.auth.signup.SignupRequest
import com.example.myapp.repository.user.UserRepository
import com.example.myapp.repository.user.email.UserEmailRepository
import org.springframework.stereotype.Service

/**
 * 登録リクエスト検証ユースケース
 * 新規登録時の入力チェックを担当する
 */
@Service
class ValidateSignupRequest(
    private val userRepository: UserRepository,
    private val userEmailRepository: UserEmailRepository
) {

    /**
     * 登録リクエストを検証する
     * @throws RuntimeException バリデーションエラー時
     */
    operator fun invoke(request: SignupRequest) {
        if (userRepository.existsByUsername(request.username)) {
            throw RuntimeException("このユーザー名は既に使用されています")
        }
        if (userEmailRepository.existsByEmail(request.email)) {
            throw RuntimeException("このメールアドレスは既に使用されています")
        }
    }

    /**
     * ユーザー名が使用可能かどうかを確認する
     */
    fun isUsernameAvailable(username: String): Boolean {
        return !userRepository.existsByUsername(username)
    }
}
