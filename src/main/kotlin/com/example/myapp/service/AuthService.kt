package com.example.myapp.service
//
//import com.example.myapp.dto.LoginRequest
//import com.example.myapp.dto.LoginResponse
//import com.example.myapp.dto.RegisterRequest
//import com.example.myapp.dto.RegisterResponse
//import com.example.myapp.entity.User
//import com.example.myapp.repository.UserRepository
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
//import org.springframework.stereotype.Service
//
//@Service
//class AuthService(
//    private val userRepository: UserRepository
//) {
//    private val passwordEncoder = BCryptPasswordEncoder()
//
//    fun register(request: RegisterRequest): RegisterResponse {
//        if (request.username.isBlank() || request.password.isBlank()) {
//            return RegisterResponse(
//                success = false,
//                message = "全ての項目を入力してください"
//            )
//        }
//
//        if (userRepository.existsByUsername(request.username)) {
//            return RegisterResponse(
//                success = false,
//                message = "このユーザー名は既に使用されています"
//            )
//        }
//
//        // if (userRepository.existsByEmail(request.email)) {
//        //     return RegisterResponse(
//        //         success = false,
//        //         message = "このメールアドレスは既に使用されています"
//        //     )
//        // }
//
//        if (request.password.length < 6) {
//            return RegisterResponse(
//                success = false,
//                message = "パスワードは6文字以上で設定してください"
//            )
//        }
//
//        val hashedPassword = passwordEncoder.encode(request.password)
//
//        val newUser = User(
//            username = request.username,
//            // email = request.email,
//            password = hashedPassword
//        )
//
//        val savedUser = userRepository.save(newUser)
//
//        return RegisterResponse(
//            success = true,
//            message = "登録が完了しました",
//            userId = savedUser.id,
//            username = savedUser.username
//        )
//    }
//
//    fun login(request: LoginRequest): LoginResponse {
//        if (request.username.isBlank() || request.password.isBlank()) {
//            return LoginResponse(
//                success = false,
//                message = "ユーザー名とパスワードを入力してください"
//            )
//        }
//
//        val user = userRepository.findByUsername(request.username)
//            ?: return LoginResponse(
//                success = false,
//                message = "ユーザー名またはパスワードが間違っています"
//            )
//
//        if (!passwordEncoder.matches(request.password, user.password)) {
//            return LoginResponse(
//                success = false,
//                message = "ユーザー名またはパスワードが間違っています"
//            )
//        }
//
//        // TOTP有効ユーザーの場合、requireTotpフラグを返す
//        if (user.totpEnabled) {
//            return LoginResponse(
//                success = false,
//                message = "TOTPコードを入力してください",
//                requireTotp = true
//            )
//        }
//
//        return LoginResponse(
//            success = true,
//            message = "ログインに成功しました",
//            userId = user.id,
//            username = user.username
//            // email = user.email
//        )
//    }
//
//    fun getUserById(userId: Int): User? {
//        return userRepository.findById(userId).orElse(null)
//    }
//}
