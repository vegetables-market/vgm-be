package com.example.myapp.util

object AuthUtils {
    /**
     * メールアドレスをマスキングする
     *
     * @param email マスキング対象のメールアドレス
     * @return マスキングされたメールアドレス (例: t******t@e*e.com)
     */
    fun maskEmail(email: String): String {
        if (email.isBlank() || !email.contains("@")) {
            return email
        }

        val atIndex = email.indexOf('@')
        if (atIndex <= 1) return email

        val localPart = email.substring(0, atIndex)
        val domainPart = email.substring(atIndex + 1)

        val maskedLocal = if (localPart.length >= 2) {
            "${localPart.first()}******${localPart.last()}"
        } else {
            "${localPart}******"
        }

        val dotIndex = domainPart.lastIndexOf('.')
        val maskedDomain = if (dotIndex > 1) {
            val domainName = domainPart.substring(0, dotIndex)
            val tld = domainPart.substring(dotIndex)
            "${domainName.first()}*${domainName.last()}$tld"
        } else {
            domainPart
        }

        return "$maskedLocal@$maskedDomain"
    }
}
