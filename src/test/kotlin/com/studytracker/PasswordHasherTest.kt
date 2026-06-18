package com.studytracker

import com.studytracker.shared.security.PasswordHasher
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class PasswordHasherTest {
    @Test
    fun `hash verifies correct password and rejects wrong`() {
        val hash = PasswordHasher.hash("secret123")

        assertNotEquals("secret123", hash)
        assertTrue(PasswordHasher.verify("secret123", hash))
        assertFalse(PasswordHasher.verify("wrong", hash))
    }
}
