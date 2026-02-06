package com.example.myapp.repository.auth

import com.example.myapp.entity.auth.VerificationCode
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface VerificationCodeRepository : JpaRepository<VerificationCode, Long> {
    fun findByEmailAndCodeAndTypeAndIsUsedFalseAndExpiresAtAfter(
        email: String,
        code: String,
        type: String,
        now: LocalDateTime
    ): VerificationCode?

    fun findByFlowIdAndCodeAndTypeAndIsUsedFalseAndExpiresAtAfter(
        flowId: String,
        code: String,
        type: String,
        now: LocalDateTime
    ): VerificationCode?

    // flowIdで検索（有効期限問わず）
    fun findByFlowId(flowId: String): VerificationCode?
    
    // 特定のユーザーの未使用コードを検索
    fun findByUserIdAndTypeAndIsUsedFalse(userId: Int, type: String): List<VerificationCode>

    // 特定のメールアドレスの未使用コードを検索 (未登録ユーザー用)
    fun findByEmailAndTypeAndIsUsedFalse(email: String, type: String): List<VerificationCode>

    // FlowIdとCodeで検索（Type問わず）
    fun findByFlowIdAndCodeAndIsUsedFalseAndExpiresAtAfter(
        flowId: String,
        code: String,
        now: LocalDateTime
    ): VerificationCode?

    // FlowIdとTypeで検索 (ActionToken用)
    fun findByFlowIdAndTypeAndIsUsedFalseAndExpiresAtAfter(
        flowId: String,
        type: String,
        now: LocalDateTime
    ): VerificationCode?
}
