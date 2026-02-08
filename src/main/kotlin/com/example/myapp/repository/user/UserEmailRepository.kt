package com.example.myapp.repository.user

import com.example.myapp.entity.user.email.UserEmail
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserEmailRepository : JpaRepository<UserEmail, Long> {
    fun findByUserId(userId: Int): List<UserEmail>
    fun findByEmail(email: String): UserEmail?
    fun findByUserIdAndIsPrimaryTrue(userId: Int): UserEmail?
    fun findByUserIdAndType(userId: Int, type: String): List<UserEmail>
    fun existsByEmail(email: String): Boolean
}
