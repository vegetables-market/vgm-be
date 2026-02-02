package com.example.myapp.dto

data class WebAuthnRegistrationStartResponse(
    val publicKey: Any // Returns PublicKeyCredentialCreationOptions map/json
)

data class WebAuthnRegistrationFinishRequest(
    val credentialId: String,
    val response: String, // JSON string of the authenticator response
    val clientDataJSON: String,
    val credentialName: String
)
