package com.example.myapp.client

import com.example.myapp.dto.LoginRequest
import com.example.myapp.dto.LoginResponse
import com.example.myapp.dto.RegisterRequest
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.WebClientRequestException

@Service
class ApiClient(
    private val webClient: WebClient = WebClient.create(System.getenv("API_BASE_URL") ?: "http://localhost:8080")
) {

    fun register(request: RegisterRequest): LoginResponse? {
        return try {
            webClient.post()
                .uri("/api/auth/register")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(LoginResponse::class.java)
                .block()
        } catch (e: WebClientRequestException) {
            throw RuntimeException("接続に失敗しました: ${e.message}", e)
        } catch (e: WebClientResponseException) {
            throw RuntimeException("レスポンスエラー: ${e.statusCode} ${e.responseBodyAsString}", e)
        }
    }

    fun login(request: LoginRequest): LoginResponse? {
        return try {
            webClient.post()
                .uri("/api/auth/login")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(LoginResponse::class.java)
                .block()
        } catch (e: WebClientRequestException) {
            throw RuntimeException("接続に失敗しました: ${e.message}", e)
        } catch (e: WebClientResponseException) {
            throw RuntimeException("レスポンスエラー: ${e.statusCode} ${e.responseBodyAsString}", e)
        }
    }

    data class ItemRequest(val name: String, val price: Int)
    data class Item(val id: Long?, val name: String?, val price: Int?)

    fun createItem(request: ItemRequest): Item? {
        return try {
            webClient.post()
                .uri("/api/items")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(Item::class.java)
                .block()
        } catch (e: WebClientRequestException) {
            throw RuntimeException("接続に失敗しました: ${e.message}", e)
        } catch (e: WebClientResponseException) {
            throw RuntimeException("レスポンスエラー: ${e.statusCode} ${e.responseBodyAsString}", e)
        }
    }

    fun getItems(): List<Item> {
        return try {
            webClient.get()
                .uri("/api/items")
                .retrieve()
                .bodyToFlux(Item::class.java)
                .collectList()
                .block() ?: emptyList()
        } catch (e: WebClientRequestException) {
            throw RuntimeException("接続に失敗しました: ${e.message}", e)
        } catch (e: WebClientResponseException) {
            throw RuntimeException("レスポンスエラー: ${e.statusCode} ${e.responseBodyAsString}", e)
        }
    }

    data class ThemeRequest(val theme: String)

    fun updateTheme(userId: Long, theme: String) {
        try {
            webClient.put()
                .uri("/api/users/$userId/theme")
                .bodyValue(ThemeRequest(theme))
                .retrieve()
                .bodyToMono(Void::class.java)
                .block()
        } catch (e: WebClientRequestException) {
            throw RuntimeException("接続に失敗しました: ${e.message}", e)
        } catch (e: WebClientResponseException) {
            throw RuntimeException("レスポンスエラー: ${e.statusCode} ${e.responseBodyAsString}", e)
        }
    }
}
