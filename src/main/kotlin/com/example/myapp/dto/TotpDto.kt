package com.example.myapp.dto

//// TOTP有効化リクエスト
//data class EnableTotpRequest(
//    val userId: Int
//)
//
//// TOTP有効化レスポンス
//data class EnableTotpResponse(
//    val success: Boolean,
//    val message: String,
//    val secret: String? = null,
//    val qrCodeUri: String? = null,
//    val qrCodeImage: String? = null  // Base64エンコードされた画像
//)
//
//// TOTP検証＆有効化リクエスト
//data class VerifyAndEnableTotpRequest(
//    val userId: Int,
//    val code: String
//)
//
//// TOTP検証＆有効化レスポンス
//data class VerifyAndEnableTotpResponse(
//    val success: Boolean,
//    val message: String
//)
//
//// TOTP無効化リクエスト
//data class DisableTotpRequest(
//    val userId: Int,
//    val password: String  // 本人確認用
//)
//
//// TOTP無効化レスポンス
//data class DisableTotpResponse(
//    val success: Boolean,
//    val message: String
//)
//
//// TOTPログインリクエスト
//data class TotpLoginRequest(
//    val username: String,
//    val password: String,
//    val totpCode: String
//)
//
//// LoginResponseを拡張（TOTP必要かどうかのフラグを追加）
//data class LoginWithTotpResponse(
//    val success: Boolean,
//    val message: String,
//    val requireTotp: Boolean = false,  // TOTP入力が必要か
//    val userId: Int? = null,
//    val username: String? = null
//    // val email: String? = null
//)
