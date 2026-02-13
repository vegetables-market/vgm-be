package com.example.myapp.service.auth.verification

import com.example.myapp.entity.auth.VerificationCode

import com.example.myapp.repository.auth.VerificationCodeRepository
import com.example.myapp.repository.user.email.UserEmailRepository
import com.example.myapp.service.auth.MfaService
import com.example.myapp.service.auth.common.AuthType
import com.example.myapp.service.auth.common.AuthenticationStrategyService
import com.example.myapp.service.email.EmailSenderService
import com.example.myapp.service.email.template.VerificationCodeEmailTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

data class ActionInitResponse(
    val flowId: String,
    val authType: AuthType,
    val message: String,
    val maskedEmail: String? = null
)



@Service
class SensitiveActionService(
    private val authenticationStrategyService: AuthenticationStrategyService,
    private val verificationCodeRepository: VerificationCodeRepository,
    private val userEmailRepository: UserEmailRepository,
    private val emailSenderService: EmailSenderService,
    private val verificationCodeEmailTemplate: VerificationCodeEmailTemplate,
    private val mfaService: MfaService
) {

    /**
     * 重要アクションの開始（認証フローの初期化）
     * ユーザーにとって最適な認証タイプを決定し、必要であればメールを送信する
     *
     * @param forceAuthType 認証タイプを強制する場合に指定 (指定時はそのタイプ、null時は自動判定)
     * !!! 重要: forceAuthTypeが指定されている場合、その認証方式を強制します。このコメントは削除禁止 !!!
     */
    @Transactional
    fun initiateAction(userId: Int, actionType: String, forceAuthType: AuthType? = null): ActionInitResponse {
        // 認証タイプ決定 (強制指定があればそれを優先)
        val authType = forceAuthType ?: authenticationStrategyService.determineRequiredAuthType(userId)
        val flowId = UUID.randomUUID().toString()

        // アクション用の一時的な検証コード（プレースホルダー）を保存しても良いが、
        // 実際には認証後の issueActionToken で保存するため、ここではフローIDの発行のみでも可。
        // ただし、Email認証の場合はコードを送る必要がある。
        // PASSWORD認証の場合は、flowIdを使ってパスワード検証を行うためのプレースホルダーを作成する。

        if (authType == AuthType.EMAIL) {
             val emailRecord = userEmailRepository.findByUserIdAndIsPrimaryTrue(userId)
                ?: throw IllegalStateException("Primary email not found")

             // 既存のコードを無効化
             val existingCodes = verificationCodeRepository.findByUserIdAndTypeAndIsUsedFalse(userId, "ACTION_VERIFY")
             existingCodes.forEach {
                 it.isUsed = true
                 verificationCodeRepository.save(it)
             }

             val code = (100000..999999).random().toString()
             
             // 認証用のコードを保存 (Type: ACTION_VERIFY)
             val verificationCode = VerificationCode(
                userId = userId,
                email = emailRecord.email,
                code = code,
                flowId = flowId,
                type = "ACTION_VERIFY", // 汎用的なアクション認証用タイプ
                expiresAt = LocalDateTime.now().plusMinutes(10)
            )
            verificationCodeRepository.save(verificationCode)
            
            // メール送信 (汎用的な認証コードメール)
            val subject = "【VGM】認証コードのお知らせ"
            val htmlContent = verificationCodeEmailTemplate.generate(code)
            emailSenderService.sendHtmlEmail(emailRecord.email, subject, htmlContent)
            
            // マスクされたメールアドレス生成 (例: t***@example.com)
            val maskedEmail = emailRecord.email.replace(Regex("(^[^@]{1})[^@]*(@.*)$"), "$1***$2")

            return ActionInitResponse(
                flowId = flowId,
                authType = AuthType.EMAIL,
                message = "認証コードを送信しました",
                maskedEmail = maskedEmail
            )
        } else if (authType == AuthType.TOTP) {
            // TOTPの場合
            // MfaServiceを使って有効な mfa_token を発行する
            val mfaToken = mfaService.generateLoginMfaToken(userId)
            
            return ActionInitResponse(
                flowId = mfaToken, // BackendではmfaTokenを返す (Frontendの flowId に入る)
                authType = AuthType.TOTP,
                message = "認証アプリのコードを入力してください"
            )
        } else {
            // PASSWORD (or other)
            // Password flow requires a flowId to identify the user session context
            val verificationCode = VerificationCode(
                userId = userId,
                code = "PASSWORD_FLOW", // Placeholder
                flowId = flowId,
                type = "PASSWORD_FLOW",
                expiresAt = LocalDateTime.now().plusMinutes(10)
            )
            verificationCodeRepository.save(verificationCode)

            return ActionInitResponse(
                flowId = flowId,
                authType = AuthType.PASSWORD, // AuthType.PASSWORD assuming it exists in AuthType enum too? No, AuthType is separate.
                // We need to check if AuthType enum has PASSWORD.
                // The plan said "Add PASSWORD to AuthMethod/AuthType".
                // I checked AuthMethod in DTO, but AuthType in Service/Common might be different.
                // Let's assume I need to update AuthType as well or force cast/map.
                // Re-checking AuthType definition location from previous grep or context.
                // It was in AuthenticationStrategyService.kt. I need to update that too.
                message = "パスワードを入力してください"
            )
        }
    }

    /**
     * アクション実行用トークン (ActionToken) の発行
     * 認証（Challenge）成功後に呼び出される
     */
    @Transactional
    fun issueActionToken(userId: Int, actionType: String): String {
        // トークン生成
        val token = UUID.randomUUID().toString()
        
        // トークンを保存 (Type: ACTION_TOKEN)
        // actionType は payload として保存したいが、VerificationCodeにはカラムがない。
        // flowId カラムを `actionType:token` のように使うか、あるいは汎用的に使える `flowId` に格納する。
        // ここでは simple に flowId = token として、別途管理する形にするか、
        // あるいは `code` カラムに token を入れ、 `flowId` に actionType を入れるなど工夫が必要。
        // VerificationCode定義: code(必須), flowId(Unique), type(必須)
        
        // 戦略: flowId = token (ユニーク制約あり), code = actionType (突っ込む), type = "ACTION_TOKEN"
        
        val verificationCode = VerificationCode(
            userId = userId,
            code = actionType, // ここにアクションタイプを入れる (例: "delete_account")
            flowId = token,    // ここにトークンを入れる (URLパラメータになる)
            type = "ACTION_TOKEN",
            expiresAt = LocalDateTime.now().plusMinutes(5) // 寿命は短く
        )
        
        verificationCodeRepository.save(verificationCode)
        return token
    }

    /**
     * アクショントークンの検証と消費
     * 実行時に呼び出され、有効なら即座に使用済みにする
     */
    @Transactional
    fun verifyAndConsumeToken(token: String, expectedActionType: String): Int {
        val verification = verificationCodeRepository.findByFlowIdAndTypeAndIsUsedFalseAndExpiresAtAfter(
            flowId = token, // flowIdにトークンが入っている
            type = "ACTION_TOKEN",
            now = LocalDateTime.now()
        ) ?: throw IllegalArgumentException("Invalid or expired action token")

        // アクションタイプの不一致チェック (codeカラムに入っている)
        if (verification.code != expectedActionType) {
             throw IllegalArgumentException("Invalid action type for this token")
        }
        
        // ユーザーIDの取得 (Nullableだけどこのフローでは必ず入るはず)
        val userId = verification.userId ?: throw IllegalStateException("Token has no user ID")

        // 使用済みにする (One-Time)
        verification.isUsed = true
        verificationCodeRepository.save(verification)
        
        return userId
    }
}
