package com.example.myapp.service

import com.example.myapp.repository.UserRepository
import org.springframework.stereotype.Service

@Service
class UserServiceImpl(
    private val userRepository: UserRepository
) {

    fun getTheme(id: Long): String {
        val user = userRepository.findById(id)
            .orElseThrow { IllegalArgumentException("User not found: $id") }

        return when (user.theme.toInt()) {
            0 -> "light"
            1 -> "dark"
            else -> "light"
        }
    }
}