package com.example.myapp.controller.auth.recovery

import com.example.myapp.entity.user.User
import com.example.myapp.entity.user.email.UserEmail
import com.example.myapp.repository.user.UserRepository
import com.example.myapp.repository.user.email.UserEmailRepository
import com.example.myapp.service.auth.recovery.AccountRecoveryService
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AccountRecoveryControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var userRepository: UserRepository
    
    @Autowired
    private lateinit var userEmailRepository: UserEmailRepository

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var accountRecoveryService: AccountRecoveryService

    private val existingUsername = "exists@example.com"
    private val nonExistingUsername = "doesnotexist@example.com"

    @BeforeEach
    fun setup() {
        // Create a test user
        if (userRepository.findByUsername(existingUsername) == null) {
            val user = User(
                username = existingUsername,
                passwordHash = "hash",
                displayName = "Test User",
                isEnabled = true
            )
            val savedUser = userRepository.save(user)
            
            val userEmail = UserEmail(
                userId = savedUser.userId,
                email = existingUsername,
                isPrimary = true,
                isVerified = true
            )
            userEmailRepository.save(userEmail)
        }
    }

    @Test
    fun `startRecovery returns identical structure for existing and non-existing users`() {
        // 1. Existing User
        val resultExisting = mockMvc.perform(post("/v1/auth/recovery/start")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(mapOf("username" to existingUsername))))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.state").exists())
                .andReturn()
        
        val contentExisting = resultExisting.response.contentAsString
        val stateExisting = objectMapper.readTree(contentExisting).get("state").asText()
        assertNotNull(stateExisting)

        // 2. Non-Existing User
        val resultNonExisting = mockMvc.perform(post("/v1/auth/recovery/start")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(mapOf("username" to nonExistingUsername))))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.state").exists())
                .andReturn()

        val contentNonExisting = resultNonExisting.response.contentAsString
        val stateNonExisting = objectMapper.readTree(contentNonExisting).get("state").asText()
        assertNotNull(stateNonExisting)
        
        // Assert structures are identical (keys)
        assertEquals(objectMapper.readTree(contentExisting).fieldNames().asSequence().toSet(), 
                     objectMapper.readTree(contentNonExisting).fieldNames().asSequence().toSet())
    }

    @Test
    fun `getOptions returns identical response for valid and invalid sessions`() {
        // Valid Session
        val validState = accountRecoveryService.startRecovery(existingUsername)
        
        mockMvc.perform(get("/v1/auth/recovery/options")
                .param("state", validState))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.options").isArray)
                .andExpect(jsonPath("$.options[0]").value("email"))

        // Invalid Session (Random UUID)
        val invalidState = "00000000-0000-0000-0000-000000000000"
        
        mockMvc.perform(get("/v1/auth/recovery/options")
                .param("state", invalidState))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.options").isArray)
                .andExpect(jsonPath("$.options[0]").value("email"))
    }

    @Test
    fun `sendChallenge returns 200 OK for valid and invalid sessions`() {
        // Valid Session
        val validState = accountRecoveryService.startRecovery(existingUsername)
        
        mockMvc.perform(post("/v1/auth/recovery/send")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(mapOf("state" to validState, "method" to "email"))))
                .andExpect(status().isOk)

        // Invalid Session
        val invalidState = "00000000-0000-0000-0000-000000000000"
        
        mockMvc.perform(post("/v1/auth/recovery/send")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(mapOf("state" to invalidState, "method" to "email"))))
                .andExpect(status().isOk)
    }

    @Test
    fun `verifyChallenge returns verified=false for invalid code or session without error`() {
        val validState = accountRecoveryService.startRecovery(existingUsername)
        
        // 1. Valid Session, Invalid Code
        mockMvc.perform(post("/v1/auth/recovery/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(mapOf("state" to validState, "method" to "email", "code" to "000000"))))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.verified").value(false))

        // 2. Invalid Session
        val invalidState = "00000000-0000-0000-0000-000000000000"
        mockMvc.perform(post("/v1/auth/recovery/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(mapOf("state" to invalidState, "method" to "email", "code" to "123456"))))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.verified").value(false))
    }
    
    @Test
    fun `completeRecovery acts blindly for invalid session`() {
        val invalidState = "00000000-0000-0000-0000-000000000000"
        
        mockMvc.perform(post("/v1/auth/recovery/complete")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(mapOf("state" to invalidState))))
                .andExpect(status().isOk)
    }
}
