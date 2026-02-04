package com.example.myapp.repository.auth

import com.example.myapp.entity.auth.TwoFactorAuth
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface TwoFactorAuthRepository : JpaRepository<TwoFactorAuth, Long> {
    fun findByUserId(userId: Int): Optional<TwoFactorAuth>
    fun existsByUserId(userId: Int): Boolean
    fun deleteByUserId(userId: Int)
}
