package com.example.myapp.repository.auth

import com.example.myapp.entity.auth.UserOAuthConnection
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserOAuthConnectionRepository : JpaRepository<UserOAuthConnection, Long> {
    fun findByUserId(userId: Int): List<UserOAuthConnection>
    fun findByUserIdAndProvider(userId: Int, provider: String): UserOAuthConnection?
    fun findByProviderAndProviderUserId(provider: String, providerUserId: String): UserOAuthConnection?
    fun existsByUserIdAndProvider(userId: Int, provider: String): Boolean
    fun deleteByUserIdAndProvider(userId: Int, provider: String)
}
