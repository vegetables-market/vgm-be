package com.example.myapp.repository.auth

import com.example.myapp.entity.auth.RecoverySession
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface RecoverySessionRepository : JpaRepository<RecoverySession, String>
