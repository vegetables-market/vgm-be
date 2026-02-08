package com.example.myapp.service.user.profile

import com.example.myapp.entity.user.profile.UserInfoEntity
import com.example.myapp.repository.user.UserInfoRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

/**
 * ユーザー詳細情報管理を担当するサービス
 */
@Service
class UserInfoService(
    private val userInfoRepository: UserInfoRepository
) {

    /**
     * ユーザー情報を取得
     * @param userId ユーザーID
     * @return ユーザー情報 (存在しない場合null)
     */
    fun getUserInfo(userId: Int): UserInfoEntity? {
        return userInfoRepository.findById(userId).orElse(null)
    }

    /**
     * ユーザー情報を更新
     * @param userId ユーザーID
     * @param gender 性別
     * @param birthDate 生年月日
     * @return 更新されたユーザー情報
     */
    @Transactional
    fun updateUserInfo(userId: Int, gender: Short?, birthDate: LocalDate?): UserInfoEntity {
        val userInfo = userInfoRepository.findById(userId).orElseGet {
            // ユーザー情報が存在しない場合は新規作成
            UserInfoEntity(userId = userId)
        }

        gender?.let { userInfo.gender = it }
        birthDate?.let { userInfo.birthDate = it }

        return userInfoRepository.save(userInfo)
    }
}
