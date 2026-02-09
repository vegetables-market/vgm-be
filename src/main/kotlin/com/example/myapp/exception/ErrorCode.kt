package com.example.myapp.exception

import org.springframework.http.HttpStatus

/**
 * アプリケーションエラーコード定義
 *
 * @property code エラーコード文字列（クライアントに返却される）
 * @property message 多言語対応のメッセージキーまたはデフォルトメッセージ
 * @property httpStatus 対応するHTTPステータスコード
 */
enum class ErrorCode(
    val code: String,
    val message: String,
    val httpStatus: HttpStatus
) {
    // System
    SYSTEM_ERROR("SYSTEM_ERROR", "システムエラーが発生しました", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_INPUT("INVALID_INPUT", "入力内容に誤りがあります", HttpStatus.BAD_REQUEST),
    
    // Auth
    AUTH_REQUIRED("AUTH_REQUIRED", "ログインが必要です", HttpStatus.UNAUTHORIZED),
    AUTH_INVALID_CREDENTIALS("AUTH_INVALID_CREDENTIALS", "ユーザー名またはパスワードが間違っています", HttpStatus.UNAUTHORIZED),
    AUTH_TOKEN_EXPIRED("AUTH_TOKEN_EXPIRED", "セッションの有効期限が切れました", HttpStatus.UNAUTHORIZED),
    AUTH_ACCOUNT_LOCKED("AUTH_ACCOUNT_LOCKED", "アカウントがロックされています", HttpStatus.UNAUTHORIZED),
    AUTH_FORBIDDEN("AUTH_FORBIDDEN", "アクセス権限がありません", HttpStatus.FORBIDDEN),
    AUTH_CODE_INVALID("AUTH_CODE_INVALID", "認証コードが無効または期限切れです", HttpStatus.BAD_REQUEST),
    AUTH_RESEND_LIMIT_EXCEEDED("AUTH_RESEND_LIMIT_EXCEEDED", "再送信回数の上限に達しました", HttpStatus.BAD_REQUEST),
    AUTH_TOO_MANY_REQUESTS("AUTH_TOO_MANY_REQUESTS", "リクエスト回数が多すぎます", HttpStatus.TOO_MANY_REQUESTS),
    AUTH_FIREBASE_ERROR("AUTH_FIREBASE_ERROR", "外部認証サービスでエラーが発生しました", HttpStatus.UNAUTHORIZED),
    
    // User
    USER_NOT_FOUND("USER_NOT_FOUND", "指定されたユーザーが見つかりません", HttpStatus.NOT_FOUND),
    USER_ALREADY_EXISTS("USER_ALREADY_EXISTS", "そのユーザー名は既に使用されています", HttpStatus.CONFLICT),
    
    // Payment / Market
    RESOURCE_NOT_FOUND("RESOURCE_NOT_FOUND", "対象のリソースが見つかりません", HttpStatus.NOT_FOUND),
    ITEM_OUT_OF_STOCK("ITEM_OUT_OF_STOCK", "在庫切れです", HttpStatus.BAD_REQUEST),
    PAYMENT_FAILED("PAYMENT_FAILED", "決済に失敗しました", HttpStatus.PAYMENT_REQUIRED)
}
