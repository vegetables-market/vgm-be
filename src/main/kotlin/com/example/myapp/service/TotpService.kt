package com.example.myapp.service
//
//import dev.samstevens.totp.code.*
//import dev.samstevens.totp.exceptions.QrGenerationException
//import dev.samstevens.totp.qr.QrData
//import dev.samstevens.totp.qr.QrGenerator
//import dev.samstevens.totp.qr.ZxingPngQrGenerator
//import dev.samstevens.totp.secret.DefaultSecretGenerator
//import dev.samstevens.totp.time.SystemTimeProvider
//import dev.samstevens.totp.time.TimeProvider
//import org.springframework.stereotype.Service
//import java.util.Base64
//
//@Service
//class TotpService {
//    private val secretGenerator = DefaultSecretGenerator()
//    private val qrGenerator: QrGenerator = ZxingPngQrGenerator()
//    private val timeProvider: TimeProvider = SystemTimeProvider()
//    private val codeGenerator: CodeGenerator = DefaultCodeGenerator()
//    private val codeVerifier: CodeVerifier
//
//    init {
//        codeVerifier = DefaultCodeVerifier(codeGenerator, timeProvider)
//    }
//
//    /**
//     * Base32エンコードされたシークレットキーを生成
//     */
//    fun generateSecret(): String {
//        return secretGenerator.generate()
//    }
//
//    /**
//     * QRコード用のURIを生成
//     * @param username ユーザー名
//     * @param secret シークレットキー
//     * @return otpauth://totp/... 形式のURI
//     */
//    fun generateQrCodeUri(username: String, secret: String): String {
//        val data = QrData.Builder()
//            .label("VGM - $username")
//            .secret(secret)
//            .issuer("VGM Application")
//            .algorithm(HashingAlgorithm.SHA1)
//            .digits(6)
//            .period(30)
//            .build()
//
//        return data.uri
//    }
//
//    /**
//     * QRコードの画像をBase64エンコードして返す
//     * @param username ユーザー名
//     * @param secret シークレットキー
//     * @return Base64エンコードされたPNG画像
//     */
//    fun generateQrCodeImageBase64(username: String, secret: String): String {
//        val data = QrData.Builder()
//            .label("VGM - $username")
//            .secret(secret)
//            .issuer("VGM Application")
//            .algorithm(HashingAlgorithm.SHA1)
//            .digits(6)
//            .period(30)
//            .build()
//
//        try {
//            val imageData = qrGenerator.generate(data)
//            return Base64.getEncoder().encodeToString(imageData)
//        } catch (e: QrGenerationException) {
//            throw RuntimeException("QRコード生成に失敗しました", e)
//        }
//    }
//
//    /**
//     * TOTPコードを検証
//     * @param secret シークレットキー
//     * @param code ユーザーが入力した6桁コード
//     * @return 検証結果（true: 正しい、false: 間違っている）
//     */
//    fun verifyCode(secret: String, code: String): Boolean {
//        return try {
//            codeVerifier.isValidCode(secret, code)
//        } catch (e: Exception) {
//            println("TOTPコード検証エラー: ${e.message}")
//            false
//        }
//    }
//
//    /**
//     * 現在の時刻に基づいてTOTPコードを生成（テスト用）
//     * @param secret シークレットキー
//     * @return 6桁のTOTPコード
//     */
//    fun generateCurrentCode(secret: String): String {
//        return try {
//            codeGenerator.generate(secret, Math.floorDiv(timeProvider.getTime(), 30))
//        } catch (e: Exception) {
//            println("TOTPコード生成エラー: ${e.message}")
//            "000000"
//        }
//    }
//}
