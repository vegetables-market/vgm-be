package com.example.myapp.repository.user

import com.example.myapp.entity.user.profile.UserInfoEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserInfoRepository : JpaRepository<UserInfoEntity, Int>
