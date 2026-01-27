package com.example.myapp.config

import dev.samstevens.totp.code.HashingAlgorithm
import dev.samstevens.totp.qr.QrDataFactory
import dev.samstevens.totp.qr.ZxingPngQrGenerator
import dev.samstevens.totp.secret.DefaultSecretGenerator
import dev.samstevens.totp.secret.SecretGenerator
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * TOTP（MFA）設定
 */
@Configuration
class TotpConfig {

    @Bean
    fun secretGenerator(): SecretGenerator {
        return DefaultSecretGenerator()
    }

    @Bean
    fun qrDataFactory(): QrDataFactory {
        return QrDataFactory(HashingAlgorithm.SHA1, 6, 30)
    }

    @Bean
    fun qrGenerator(): ZxingPngQrGenerator {
        return ZxingPngQrGenerator()
    }
}
