package com.example.myapp.dto

import java.time.LocalDateTime

data class WebAuthnRegistrationStartResponse(
    val publicKey: Any // Returns PublicKeyCredentialCreationOptions map/json
)

data class WebAuthnRegistrationFinishRequest(
    val credentialId: String,
    val response: String, // JSON string of the authenticator response
    val clientDataJSON: String,
    val credentialName: String
)

data class WebAuthnAuthenticationFinishRequest(
    val credentialId: String,
    val response: String // JSON string: { clientDataJSON, authenticatorData, signature, userHandle }
)

data class UserCredentialResponse(
    val credentialId: String,
    val name: String,
    val createdAt: LocalDateTime,
    val lastUsedAt: LocalDateTime?
)
