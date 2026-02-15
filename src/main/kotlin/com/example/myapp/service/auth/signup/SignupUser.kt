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
    private val sendVerificationEmail: SendVerificationEmail,
    private val oauthConnectionService: com.example.myapp.service.auth.oauth.OAuthConnectionService,
    private val firebaseAuthService: com.example.myapp.service.auth.FirebaseAuthService,
    private val oauthUserService: com.example.myapp.service.auth.oauth.OAuthUserService
) {

    /**
     * 新規ユーザー登録処理
     */
    @Transactional
    operator fun invoke(request: SignupRequest): LoginResponse {
        // 1. バリデーション
        validateSignupRequest(request)

        // 2. 事前認証チェック
        var isPreVerified = false
        var oauthUid: String? = null

        if (request.flowId != null) {
            val verified = checkFlowVerification(request.flowId, request.email)
            if (!verified) {
                // flow_idが送られてきたのに検証できない場合はエラー
                throw RuntimeException("Invalid verification flow.")
            }
            isPreVerified = true
        } else if (request.oauthToken != null && request.oauthProvider != null) {
            // OAuthトークンによる検証
            try {
                val verifiedToken = firebaseAuthService.verifyToken(request.oauthToken)
                if (verifiedToken.email != request.email) {
                    throw RuntimeException("Email mismatch: Token email does not match request email.")
                }
                isPreVerified = true
                oauthUid = verifiedToken.uid
            } catch (e: Exception) {
                throw RuntimeException("Invalid OAuth token: ${e.message}", e)
            }
        }

        // 3. ユーザー作成
        val result = createVerifiedUser(request, isPreVerified)

        // 4. OAuth接続作成 (OAuth登録の場合)
        if (oauthUid != null && request.oauthProvider != null) {
             // メールレコードを確保 (新規ユーザーとして作成されたばかりなので isNewUser=true扱いだが、ensureEmailRecordは存在確認も兼ねる)
             val emailRecord = oauthUserService.ensureEmailRecord(result.user.userId, request.email, request.oauthProvider, true)

             // Connection作成
             oauthConnectionService.createConnection(
                 userId = result.user.userId,
                 provider = request.oauthProvider,
                 providerUserId = oauthUid,
                 emailId = emailRecord.emailId,
                 displayName = result.user.displayName
             )
             
             // ログイン情報更新
             oauthUserService.updateLoginInfo(result.user.userId, request.oauthProvider)
        }

        // 5. メール送信 (未認証の場合)
        val flowId = if (!isPreVerified) {
            val (fid, _, _) = sendVerificationEmail(result.user.userId, request.email)
            fid
        } else {
             // 認証済みの場合はログインセッション用のIDを発行したいが、
             // ここでは便宜上 null または ダミーを返す (LoginService側でログイン処理を行わせるため)
             null
        }
        
        // 6. レスポンス作成
        // OAuth登録の場合は自動的にログイン状態にしたいが、現状のアーキテクチャでは
        // LoginControllerなどが別途セッションを発行したりするため、ここではあくまでユーザー作成結果を返す。
        // フロントエンドはこれを受け取って自動ログインするか、あるいはLogin APIを叩く。
        // ここでは AUTHENTICATED として返し、フロントで自動ログイン処理に回すのが良い。
        
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
            maskedEmail = if (!isPreVerified) AuthUtils.maskEmail(request.email) else null,
            oauthProvider = request.oauthProvider // 念のため返す
        )
    }
}
