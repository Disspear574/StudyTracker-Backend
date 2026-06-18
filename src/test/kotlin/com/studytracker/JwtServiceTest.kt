package com.studytracker

import com.studytracker.shared.security.JwtService
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class JwtServiceTest {
    private val service = JwtService(
        secret = "a-very-long-test-secret-0123456789",
        issuer = "test-iss",
        audience = "test-aud",
        accessTtlSeconds = 3600,
    )

    @Test
    fun `access token encodes subject and verifies`() {
        val userId = UUID.randomUUID()

        val token = service.createAccessToken(userId)
        val decoded = service.verifier().verify(token.token)

        assertEquals(userId.toString(), decoded.subject)
        assertEquals("test-iss", decoded.issuer)
    }

    @Test
    fun `token signed with another secret fails verification`() {
        val token = service.createAccessToken(UUID.randomUUID())
        val other = JwtService("different-secret-9876543210abcdef", "test-iss", "test-aud", 3600)

        assertFailsWith<Exception> { other.verifier().verify(token.token) }
    }
}
