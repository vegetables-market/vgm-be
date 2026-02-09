package com.example.myapp.service.auth.signup

import com.example.myapp.repository.user.UserRepository
import org.springframework.stereotype.Service
import java.time.Year
import java.util.Random
import java.util.UUID

/**
 * ユーザー名提案ユースケース
 * 新規登録時などに、利用可能なユーザー名の候補を生成する
 */
@Service
class SuggestUsernames(
    private val userRepository: UserRepository
) {

    /**
     * ユーザー名の候補を生成する（入力されたユーザー名をベースに）
     */
    operator fun invoke(baseUsername: String): List<String> {
        val suggestions = mutableListOf<String>()
        val random = Random()
        
        // パターン試行 (1回のみ)
        // パターン1: 数字3桁
        val candidate1 = baseUsername + (random.nextInt(900) + 100)
        if (!userRepository.existsByUsername(candidate1)) {
            suggestions.add(candidate1)
        }

        // パターン2: アンダースコア + 数字3桁
        if (suggestions.size < 3) {
            val candidate2 = baseUsername + "_" + (random.nextInt(900) + 100)
            if (!userRepository.existsByUsername(candidate2) && !suggestions.contains(candidate2)) {
                suggestions.add(candidate2)
            }
        }
        
        // パターン3: 現在の年
        if (suggestions.size < 3) {
            val candidate3 = baseUsername + Year.now().value
             val exists = userRepository.existsByUsername(candidate3)
             if (!exists && !suggestions.contains(candidate3)) {
                suggestions.add(candidate3)
            }
        }
        
        // それでも足りない場合はランダムで埋める (最大10回試行)
        var attempts = 0
        while (suggestions.size < 3 && attempts < 10) {
             val suffix = (random.nextInt(9000) + 1000).toString()
             val candidate = baseUsername + suffix
             if (!userRepository.existsByUsername(candidate) && !suggestions.contains(candidate)) {
                suggestions.add(candidate)
            }
            attempts++
        }

        // それでも埋まらない場合の最終手段 (UUID)
        while (suggestions.size < 3) {
             val fallback = baseUsername + "_" + UUID.randomUUID().toString().substring(0, 6)
             if (!suggestions.contains(fallback)) {
                 suggestions.add(fallback)
             }
        }

        return suggestions
    }

    /**
     * 初期表示用のユーザー名候補を生成する（ランダム）
     */
    fun getInitialSuggestions(): List<String> {
        val suggestions = mutableListOf<String>()
        val random = Random()
        val prefixes = listOf("user", "player", "member", "guest", "account")
        
        while (suggestions.size < 3) {
            val prefix = prefixes[random.nextInt(prefixes.size)]
            val suffix = (random.nextInt(900000) + 100000).toString() // 6桁
            val candidate = "${prefix}_$suffix"
            
            if (!userRepository.existsByUsername(candidate) && !suggestions.contains(candidate)) {
                suggestions.add(candidate)
            }
        }
        return suggestions
    }
}
