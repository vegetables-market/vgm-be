package com.example.myapp.service.auth.signup

import com.example.myapp.dto.auth.login.LoginResponse
import com.example.myapp.dto.auth.signup.SignupRequest
import com.example.myapp.dto.auth.login.UserInfo
import com.example.myapp.service.email.EmailVerificationService
import com.example.myapp.util.AuthUtils
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class SignupService(
    private val signupValidationService: SignupValidationService,
    private val signupCreationService: SignupCreationService,
    private val usernameSuggestionService: UsernameSuggestionService,
    private val emailVerificationService: EmailVerificationService
) {

    /**
     * 新規ユーザー登録処理
     *
     * @param request 登録リクエスト情報
     * @return ログインレスポンス（登録成功時）
     */
    @Transactional
    fun signup(request: SignupRequest): LoginResponse {
        // 1. バリデーション
        signupValidationService.validateSignupRequest(request)

        // 2. 事前認証チェック
        val isPreVerified = if (request.flowId != null) {
            val verified = emailVerificationService.isFlowVerified(request.flowId, request.email)
            if (!verified) {
                // flow_idが送られてきたのに検証できない場合はエラー
                throw RuntimeException("Invalid verification flow.")
            }
            true
        } else {
            false
        }

        // 3. ユーザー作成
        val result = signupCreationService.createUser(request, isPreVerified)

        // 4. メール送信 (未認証の場合)
        val flowId = if (!isPreVerified) {
            val (fid, _) = emailVerificationService.sendVerificationEmail(result.user.userId, request.email)
            fid
        } else {
             // 認証済みの場合はログインセッション用のIDを発行したいが、
             // ここでは便宜上 null または ダミーを返す (LoginService側でログイン処理を行わせるため)
             null
        }

        return LoginResponse(
            status = if (isPreVerified) "AUTHENTICATED" else "REGISTERED",
            user = UserInfo(
                username = result.user.username,
                displayName = result.user.displayName,
                email = request.email,
                avatarUrl = null,
                isEmailVerified = isPreVerified
            ),
            requireVerification = !isPreVerified,
            flowId = flowId,
            maskedEmail = if (!isPreVerified) AuthUtils.maskEmail(request.email) else null
        )
    }

    /**
     * ユーザー名が使用可能かどうかを確認する
     *
     * @param username 確認したいユーザー名
     * @return 使用可能な場合は true
     */
    fun isUsernameAvailable(username: String): Boolean {
        return signupValidationService.isUsernameAvailable(username)
    }

    /**
     * ユーザー名の候補を生成する（入力されたユーザー名をベースに）
     *
     * @param baseUsername ベースとなるユーザー名
     * @return 提案するユーザー名のリスト
     */
    fun generateUsernameSuggestions(baseUsername: String): List<String> {
        return usernameSuggestionService.generateUsernameSuggestions(baseUsername)
    }

    /**
     * 初期表示用のユーザー名候補を生成する（ランダム）
     *
     * @return 提案するユーザー名のリスト
     */
    fun getInitialSuggestions(): List<String> {
        return usernameSuggestionService.getInitialSuggestions()
    }
}
