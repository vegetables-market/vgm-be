package com.example.myapp.service.auth.signup

import com.example.myapp.dto.auth.login.LoginResponse
import com.example.myapp.dto.auth.login.UserInfo
import com.example.myapp.dto.auth.signup.SignupRequest
import com.example.myapp.service.email.verification.CheckFlowVerification
import com.example.myapp.service.email.verification.SendVerificationEmail
import com.example.myapp.util.AuthUtils
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * ユーザー登録ユースケース
 * 新規ユーザー登録フロー全体を調整する
 */
@Service
class SignupUser(
    private val validateSignupRequest: ValidateSignupRequest,
    private val createVerifiedUser: CreateVerifiedUser,
    private val checkFlowVerification: CheckFlowVerification,
    private val sendVerificationEmail: SendVerificationEmail
) {

    /**
     * 新規ユーザー登録処理
     */
    @Transactional
    operator fun invoke(request: SignupRequest): LoginResponse {
        // 1. バリデーション
        validateSignupRequest(request)

        // 2. 事前認証チェック
        val isPreVerified = if (request.flowId != null) {
            val verified = checkFlowVerification(request.flowId, request.email)
            if (!verified) {
                // flow_idが送られてきたのに検証できない場合はエラー
                throw RuntimeException("Invalid verification flow.")
            }
            true
        } else {
            false
        }

        // 3. ユーザー作成
        val result = createVerifiedUser(request, isPreVerified)

        // 4. メール送信 (未認証の場合)
        val flowId = if (!isPreVerified) {
            val (fid, _, _) = sendVerificationEmail(result.user.userId, request.email)
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
}
