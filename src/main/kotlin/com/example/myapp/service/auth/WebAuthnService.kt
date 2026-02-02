package com.example.myapp.service.auth

import com.example.myapp.entity.user.User
import com.example.myapp.entity.user.UserCredential
import com.example.myapp.repository.user.UserCredentialRepository
import com.example.myapp.exception.BusinessException // Add this
import com.webauthn4j.WebAuthnManager
import com.webauthn4j.data.*
import com.webauthn4j.data.attestation.statement.COSEAlgorithmIdentifier
import com.webauthn4j.data.client.Origin
import com.webauthn4j.data.client.challenge.DefaultChallenge
import com.webauthn4j.server.ServerProperty
import com.webauthn4j.converter.exception.DataConversionException
import com.webauthn4j.validator.exception.ValidationException
import jakarta.servlet.http.HttpSession
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.Base64

@Service
class WebAuthnService(
    private val userCredentialRepository: UserCredentialRepository,
    @Value("\${app.webauthn.origin:http://localhost:3000}") private val originUrl: String,
    @Value("\${app.webauthn.rpId:localhost}") private val rpId: String,
    @Value("\${app.webauthn.rpName:VGM}") private val rpName: String
) {

    private val webAuthnManager = WebAuthnManager.createNonStrictWebAuthnManager()

    private val objectMapper = com.fasterxml.jackson.databind.ObjectMapper()

    fun startRegistration(user: User, session: HttpSession): PublicKeyCredentialCreationOptions {
        val challenge = DefaultChallenge()
        session.setAttribute("WEB_AUTHN_CHALLENGE", Base64.getUrlEncoder().withoutPadding().encodeToString(challenge.value))
        session.setAttribute("WEB_AUTHN_USER_ID", user.userId)

        // Existing credentials to exclude
        val existingCredentials = userCredentialRepository.findAllByUser(user).map {
            PublicKeyCredentialDescriptor(
                PublicKeyCredentialType.PUBLIC_KEY,
                Base64.getUrlDecoder().decode(it.credentialId),
                emptySet()
            )
        }

        return PublicKeyCredentialCreationOptions(
            PublicKeyCredentialRpEntity(rpId, rpName),
            PublicKeyCredentialUserEntity(
                Base64.getUrlEncoder().withoutPadding().encodeToString(user.userId.toString().toByteArray()),
                user.username,
                user.displayName
            ),
            challenge,
            listOf(
                PublicKeyCredentialParameters(PublicKeyCredentialType.PUBLIC_KEY, COSEAlgorithmIdentifier.ES256),
                PublicKeyCredentialParameters(PublicKeyCredentialType.PUBLIC_KEY, COSEAlgorithmIdentifier.RS256)
            ),
            null,
            existingCredentials,
            AuthenticatorSelectionCriteria(
                AuthenticatorAttachment.CROSS_PLATFORM,
                true,
                UserVerificationRequirement.PREFERRED
            ),
            AttestationConveyancePreference.NONE,
            null
        )
    }

    @Transactional
    fun finishRegistration(user: User, session: HttpSession, credentialId: String, responseJson: String, credentialName: String) {
        val challengeStr = session.getAttribute("WEB_AUTHN_CHALLENGE") as? String
            ?: throw BusinessException("BAD_REQUEST", "Challenge not found")
        val challenge = DefaultChallenge(Base64.getUrlDecoder().decode(challengeStr))

        // Parse responseJson (AuthenticatorAttestationResponse)
        val responseNode = objectMapper.readTree(responseJson)
        val clientDataJSON = Base64.getUrlDecoder().decode(responseNode.get("clientDataJSON").asText())
        val attestationObject = Base64.getUrlDecoder().decode(responseNode.get("attestationObject").asText())

        val serverProperty = ServerProperty(
            Origin(originUrl),
            rpId,
            challenge,
            null
        )

        val registrationRequest = RegistrationRequest(
            Base64.getUrlDecoder().decode(credentialId), // attestationObject includes this, but passed separately often
            attestationObject,
            clientDataJSON
        )
        
        // Use default RegistrationParameters
        val registrationParameters = RegistrationParameters(
            serverProperty,
            null, // pubKeyCredCreationOptions (optional for validation if we manually check challenge)
            false // userVerificationRequired
        )

        val registrationData = webAuthnManager.validate(registrationRequest, registrationParameters)

        // Extract Public Key (COSE)
        val coseKey = registrationData.attestationObject.authenticatorData.attestedCredentialData!!.coseKey
        val publicKeyCbor = webAuthnManager.cborConverter.writeValueAsBytes(coseKey)
        val publicKeyBase64 = Base64.getUrlEncoder().withoutPadding().encodeToString(publicKeyCbor)

        val credential = UserCredential(
            credentialId = credentialId,
            user = user,
            name = credentialName,
            publicKey = publicKeyBase64,
            signCount = registrationData.attestationObject.authenticatorData.signCount
        )
        userCredentialRepository.save(credential)
    }

    fun startLogin(session: HttpSession): PublicKeyCredentialRequestOptions {
        val challenge = DefaultChallenge()
        session.setAttribute("WEB_AUTHN_CHALLENGE", Base64.getUrlEncoder().withoutPadding().encodeToString(challenge.value))

        return PublicKeyCredentialRequestOptions(
            challenge,
            60000,
            rpId,
            null, // Allow all credentials (or filter by user if user identifies first)
            UserVerificationRequirement.PREFERRED,
            null
        )
    }

    // Need DTO for Login Finish because request structure is different
    // Assuming controller maps it to args
    @Transactional
    fun finishLogin(session: HttpSession, credentialId: String, responseJson: String): User {
        val challengeStr = session.getAttribute("WEB_AUTHN_CHALLENGE") as? String
            ?: throw BusinessException("BAD_REQUEST", "Challenge not found")
        val challenge = DefaultChallenge(Base64.getUrlDecoder().decode(challengeStr))

        val credential = userCredentialRepository.findByCredentialId(credentialId)
            .orElseThrow { BusinessException("NOT_FOUND", "Credential not found") }

        val responseNode = objectMapper.readTree(responseJson)
        val clientDataJSON = Base64.getUrlDecoder().decode(responseNode.get("clientDataJSON").asText())
        val authenticatorData = Base64.getUrlDecoder().decode(responseNode.get("authenticatorData").asText())
        val signature = Base64.getUrlDecoder().decode(responseNode.get("signature").asText())
        
        val serverProperty = ServerProperty(
            Origin(originUrl),
            rpId,
            challenge,
            null
        )

        val authenticationRequest = AuthenticationRequest(
            Base64.getUrlDecoder().decode(credentialId),
            authenticatorData,
            clientDataJSON,
            signature
        )

        // Reconstruct COSE Key
        val coseKeyBytes = Base64.getUrlDecoder().decode(credential.publicKey)
        val coseKey = webAuthnManager.cborConverter.readValue(coseKeyBytes, com.webauthn4j.data.attestation.statement.COSEKey::class.java)

        val authenticationParameters = AuthenticationParameters(
            serverProperty,
            coseKey, // The stored public key
            null, // old sign count check logic
            false
        )
        
        // Verify sign count to prevent cloning (basic check)
        authenticationParameters.authenticator = com.webauthn4j.data.AuthenticatorImpl(
            null, null, com.webauthn4j.data.extension.authenticator.AuthenticationExtensionsAuthenticatorOutputs(), credential.signCount
        )

        val authenticationData = webAuthnManager.validate(authenticationRequest, authenticationParameters)

        // Update sign count
        credential.signCount = authenticationData.authenticatorData.signCount
        credential.lastUsedAt = java.time.LocalDateTime.now()
        userCredentialRepository.save(credential)

        return credential.user
    }

    fun getCredentials(user: User): List<com.example.myapp.dto.UserCredentialResponse> {
        return userCredentialRepository.findAllByUser(user).map {
            com.example.myapp.dto.UserCredentialResponse(
                credentialId = it.credentialId,
                name = it.name,
                createdAt = it.createdAt,
                lastUsedAt = it.lastUsedAt
            )
        }
    }

    @Transactional
    fun deleteCredential(user: User, credentialId: String) {
        userCredentialRepository.deleteByCredentialIdAndUser(credentialId, user)
    }
}
