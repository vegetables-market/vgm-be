package com.example.myapp.repository.auth

import com.example.myapp.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : JpaRepository<User, Int> {
    fun findByUsername(username: String): User?
    fun findByEmail(email: String): User?
    
    // ユーザー名またはメールアドレスで検索
    fun findByUsernameOrEmail(username: String, email: String): User?
    
    fun existsByUsername(username: String): Boolean
    fun existsByEmail(email: String): Boolean
}
