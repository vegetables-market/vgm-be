package com.example.myapp.repository.auth

import com.example.myapp.entity.auth.UserAuthStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserAuthStatusRepository : JpaRepository<UserAuthStatus, Int> {
    fun findByUserId(userId: Int): UserAuthStatus?
}
