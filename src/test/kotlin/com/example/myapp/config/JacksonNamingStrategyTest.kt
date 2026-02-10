package com.example.myapp.config

import com.example.myapp.dto.auth.signup.SignupRequest
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class JacksonNamingStrategyTest {

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Test
    fun `snake_case json binds to SignupRequest`() {
        val json = """
            {
              "username": "testuser",
              "email": "test@example.com",
              "password": "P@ssw0rd!",
              "display_name": "Test User",
              "birth_year": 2000,
              "birth_month": 1,
              "birth_day": 2,
              "gender": "other",
              "flow_id": "flow-123"
            }
        """.trimIndent()

        val request = objectMapper.readValue(json, SignupRequest::class.java)

        assertEquals("testuser", request.username)
        assertEquals("test@example.com", request.email)
        assertEquals("P@ssw0rd!", request.password)
        assertEquals("Test User", request.displayName)
        assertEquals(2000, request.birthYear)
        assertEquals(1, request.birthMonth)
        assertEquals(2, request.birthDay)
        assertEquals("other", request.gender)
        assertEquals("flow-123", request.flowId)
    }

    @Test
    fun `SignupRequest serializes to snake_case`() {
        val request = SignupRequest(
            username = "testuser",
            email = "test@example.com",
            password = "P@ssw0rd!",
            displayName = "Test User",
            birthYear = 2000,
            flowId = "flow-123"
        )

        val json = objectMapper.writeValueAsString(request)

        assertTrue(json.contains("\"display_name\""))
        assertTrue(json.contains("\"birth_year\""))
        assertTrue(json.contains("\"flow_id\""))
    }
}
