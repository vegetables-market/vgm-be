package com.example.myapp.repository.user.device

import com.example.myapp.entity.user.device.UserDevice
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface UserDeviceRepository : JpaRepository<UserDevice, Long> {
    fun findByUserIdAndFcmToken(userId: Int, fcmToken: String): Optional<UserDevice>
    fun findByFcmToken(fcmToken: String): Optional<UserDevice>
    fun deleteByFcmToken(fcmToken: String)
    fun findByUserId(userId: Int): List<UserDevice>
}
