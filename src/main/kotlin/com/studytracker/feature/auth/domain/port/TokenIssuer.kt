package com.studytracker.feature.auth.domain.port

import java.time.LocalDateTime
import java.util.UUID

data class IssuedAccessToken(val token: String, val expiresAt: LocalDateTime)

data class GeneratedRefreshToken(
    val rawToken: String,
    val tokenHash: String,
    val expiresAt: LocalDateTime,
)

interface TokenIssuer {
    fun issueAccessToken(userId: UUID): IssuedAccessToken
    fun generateRefreshToken(): GeneratedRefreshToken

    fun hashRefreshToken(rawToken: String): String
}
