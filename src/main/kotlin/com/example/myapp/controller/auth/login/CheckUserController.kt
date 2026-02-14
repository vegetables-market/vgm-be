package com.example.myapp.controller.auth.login

import com.example.myapp.dto.auth.login.CheckUserRequest
import com.example.myapp.dto.auth.login.CheckUserResponse
import com.example.myapp.service.auth.login.CheckUser
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * ユーザー識別子チェックコントローラー
 * ログインフローの最初のステップとして呼び出される
 */
@RestController
@RequestMapping("/v1/auth")
class CheckUserController(
    private val checkUser: CheckUser
) {
    @PostMapping("/check-user")
    fun checkUser(@RequestBody request: CheckUserRequest): ResponseEntity<CheckUserResponse> {
        val response = checkUser(request.identifier)
        return ResponseEntity.ok(response)
    }
}
