package com.example.myapp.entity

import jakarta.persistence.*
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "t_user_info")
data class UserInfoEntity(
    @Id
    @Column(name = "f_user_id")
    val userId: Int,

    @Column(name = "f_last_name")
    val lastName: String? = null,

    @Column(name = "f_first_name")
    val firstName: String? = null,

    @Column(name = "f_last_name_kana")
    val lastNameKana: String? = null,

    @Column(name = "f_first_name_kana")
    val firstNameKana: String? = null,

    @Column(name = "f_birth_date")
    val birthDate: LocalDate? = null,

    @Column(name = "f_gender")
    val gender: Short = 0, // 0:未選択, 1:男性, 2:女性, 3:その他

    @Column(name = "f_phone_number")
    val phoneNumber: String? = null,

    @Column(name = "f_created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "f_updated_at")
    val updatedAt: LocalDateTime = LocalDateTime.now()
)
