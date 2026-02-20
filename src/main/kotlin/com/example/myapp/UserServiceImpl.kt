package com.example.myapp.service

import com.example.myapp.repository.UserRepository
import org.springframework.stereotype.Service

@Service
class UserServiceImpl(
    private val userRepository: UserRepository
) : UserService {

    override fun getTheme(id: Long): Int {
        val user = userRepository.findById(id)
            .orElseThrow { IllegalArgumentException("User not found: $id") }

        return user.theme
    }
}