package com.example.myapp.repository.user

import com.example.myapp.entity.user.NotificationSettings
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface NotificationSettingsRepository : JpaRepository<NotificationSettings, Int>
