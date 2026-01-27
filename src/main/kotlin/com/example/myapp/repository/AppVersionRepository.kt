package com.example.myapp.repository

import com.example.myapp.entity.common.AppVersion
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface AppVersionRepository : JpaRepository<AppVersion, Long> {
    fun findTopByOrderByVersionCodeDesc(): AppVersion?
}