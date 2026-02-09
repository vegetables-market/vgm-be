package com.example.myapp.exception

/**
 * アプリケーション共通の例外クラス
 *
 * @property errorCode エラーコード定義
 * @property message エラーメッセージ（デフォルトはErrorCodeのメッセージ）
 * @property details エラー詳細情報（バリデーションエラーなど）
 */
open class AppException(
    val errorCode: ErrorCode,
    override val message: String = errorCode.message,
    val details: List<String>? = null
) : RuntimeException(message)
