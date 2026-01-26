package com.example.myapp.repository.auth

import com.example.myapp.entity.UserInfoEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserInfoRepository : JpaRepository<UserInfoEntity, Int>
