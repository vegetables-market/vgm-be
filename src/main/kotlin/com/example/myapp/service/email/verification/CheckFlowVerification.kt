package com.example.myapp.service.email.verification

import com.example.myapp.repository.auth.VerificationCodeRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * フロー検証状態チェックユースケース
 */
@Service
class CheckFlowVerification(
    private val verificationCodeRepository: VerificationCodeRepository
) {

    /**
     * flowId がメール認証済みかどうかを確認 (ユーザー登録時用)
     */
    @Transactional(readOnly = true)
    operator fun invoke(flowId: String, email: String): Boolean {
        val verification = verificationCodeRepository.findByFlowId(flowId) ?: return false
        
        // メールアドレスが一致し、使用済み(認証成功済み)であり、タイプがEMAIL_VERIFYであること
        return verification.email == email && 
               verification.isUsed && 
               verification.type == "EMAIL_VERIFY"
    }
}
