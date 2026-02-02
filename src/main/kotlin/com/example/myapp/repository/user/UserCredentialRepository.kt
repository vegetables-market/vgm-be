package com.example.myapp.repository.user

import com.example.myapp.entity.user.User
import com.example.myapp.entity.user.UserCredential
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface UserCredentialRepository : JpaRepository<UserCredential, Int> {
    fun findByCredentialId(credentialId: String): Optional<UserCredential>
    fun findAllByUser(user: User): List<UserCredential>
    fun deleteByCredentialIdAndUser(credentialId: String, user: User)
}
