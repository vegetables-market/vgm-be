package com.example.myapp.service.user

import com.example.myapp.entity.user.device.UserDevice
import com.example.myapp.repository.user.UserDeviceRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class UserDeviceService(
    private val userDeviceRepository: UserDeviceRepository
) {

    /**
     * デバイス（FCMトークン）を登録・更新する
     */
    @Transactional
    fun registerDevice(
        userId: Int,
        fcmToken: String,
        deviceType: String? = null,
        appVersion: String? = null,
        osVersion: String? = null
    ): UserDevice {
        val existingDeviceOpt = userDeviceRepository.findByFcmToken(fcmToken)
        
        return if (existingDeviceOpt.isPresent) {
            val device = existingDeviceOpt.get()
            // ユーザーが変わった場合、または同じユーザーで情報更新
            val updatedDevice = device.copy(
                userId = userId, // 所有者を上書き（ユーザー切り替え対応）
                deviceType = deviceType ?: device.deviceType,
                appVersion = appVersion ?: device.appVersion,
                osVersion = osVersion ?: device.osVersion,
                lastActiveAt = LocalDateTime.now()
            )
            userDeviceRepository.save(updatedDevice)
        } else {
            val newDevice = UserDevice(
                userId = userId,
                fcmToken = fcmToken,
                deviceType = deviceType,
                appVersion = appVersion,
                osVersion = osVersion,
                lastActiveAt = LocalDateTime.now()
            )
            userDeviceRepository.save(newDevice)
        }
    }

    /**
     * デバイス登録を解除する (ログアウト時など)
     */
    @Transactional
    fun unregisterDevice(fcmToken: String) {
        userDeviceRepository.deleteByFcmToken(fcmToken)
    }

    /**
     * 特定ユーザーのデバイス一覧を取得
     */
    fun getUserDevices(userId: Int): List<UserDevice> {
        return userDeviceRepository.findByUserId(userId)
    }
}
