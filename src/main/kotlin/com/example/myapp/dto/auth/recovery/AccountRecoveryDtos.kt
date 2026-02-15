package com.example.myapp.dto.auth.recovery

data class StartRecoveryRequest(
    val username: String
)

data class StartRecoveryResponse(
    val state: String
)

data class GetOptionsResponse(
    val options: List<String>
)

data class SendChallengeRequest(
    val state: String,
    val method: String
)

data class VerifyChallengeRequest(
    val state: String,
    val method: String,
    val code: String
)

data class CompleteRecoveryRequest(
    val state: String
)
