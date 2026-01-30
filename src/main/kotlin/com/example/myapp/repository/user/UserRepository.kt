package com.example.myapp.repository.user

import com.example.myapp.entity.user.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : JpaRepository<User, Int> {
    fun findByUsername(username: String): User?
    fun existsByUsername(username: String): Boolean
}
