package com.example.myapp.service.auth.signup

import com.example.myapp.dto.auth.signup.SignupRequest
import com.example.myapp.repository.user.UserRepository
import com.example.myapp.repository.user.email.UserEmailRepository
import org.springframework.stereotype.Service

/**
 * 登録バリデーションサービス
 * 新規登録時の入力チェックを担当する
 */
@Service
class SignupValidationService(
    private val userRepository: UserRepository,
    private val userEmailRepository: UserEmailRepository
) {
    /**
     * 登録リクエストを検証する
     *
     * @param request 登録リクエスト
     * @throws RuntimeException バリデーションエラー時
     */
    fun validateSignupRequest(request: SignupRequest) {
        if (userRepository.existsByUsername(request.username)) {
            throw RuntimeException("このユーザー名は既に使用されています")
        }
        if (userEmailRepository.existsByEmail(request.email)) {
            throw RuntimeException("このメールアドレスは既に使用されています")
        }
    }

    /**
     * ユーザー名が使用可能かどうかを確認する
     *
     * @param username 確認したいユーザー名
     * @return 使用可能な場合は true
     */
    fun isUsernameAvailable(username: String): Boolean {
        return !userRepository.existsByUsername(username)
    }
}
