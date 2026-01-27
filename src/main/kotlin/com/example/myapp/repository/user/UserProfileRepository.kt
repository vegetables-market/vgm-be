package com.example.myapp.repository.user

import com.example.myapp.entity.user.UserProfile
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserProfileRepository : JpaRepository<UserProfile, Int>
