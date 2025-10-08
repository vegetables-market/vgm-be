package com.example.myapp.controller

import com.example.myapp.model.User
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
@RequestMapping("/auth")
class AuthController {

    private val passwordEncoder = BCryptPasswordEncoder()
    private val users = mutableListOf<User>()

    @GetMapping("/login")
    fun loginForm(): String {
        return "auth/login"
    }

    @PostMapping("/login")
    fun login(
        @RequestParam email: String,
        @RequestParam password: String,
        model: Model
    ): String {
        if (email.isBlank() || password.isBlank()) {
            model.addAttribute("error", "メールアドレスとパスワードを入力してください")
            return "auth/login"
        }

        // ユーザー検索と認証
        val user = users.find { it.email == email }
        if (user != null && passwordEncoder.matches(password, user.password)) {
            // 認証成功時はトップページにリダイレクト
            return "redirect:/"
        } else {
            // 認証失敗時はエラーメッセージを表示
            model.addAttribute("error", "メールアドレスまたはパスワードが正しくありません")
            return "auth/login"
        }
    }

    @GetMapping("/logout")
    fun logout(): String {
        // TODO: ログアウト処理を実装
        return "redirect:/auth/login"
    }

    @GetMapping("/register")
    fun registerForm(): String {
        return "auth/register"
    }

    @PostMapping("/register")
    fun register(
        @RequestParam username: String,
        @RequestParam email: String,
        @RequestParam password: String,
        @RequestParam passwordConfirm: String,
        model: Model
    ): String {
        // バリデーション
        if (username.isBlank() || email.isBlank() || password.isBlank()) {
            model.addAttribute("error", "すべてのフィールドを入力してください")
            return "auth/register"
        }

        if (password != passwordConfirm) {
            model.addAttribute("error", "パスワードが一致しません")
            return "auth/register"
        }

        if (password.length < 6) {
            model.addAttribute("error", "パスワードは6文字以上で入力してください")
            return "auth/register"
        }

        // メールアドレスの重複チェック
        if (users.any { it.email == email }) {
            model.addAttribute("error", "このメールアドレスは既に登録されています")
            return "auth/register"
        }

        // ユーザー作成
        val hashedPassword = passwordEncoder.encode(password)
        val newUser = User(
            id = users.size + 1L,
            username = username,
            email = email,
            password = hashedPassword
        )
        users.add(newUser)

        model.addAttribute("success", "アカウントが作成されました。ログインしてください。")
        return "auth/login"
    }
}