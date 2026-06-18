package com.studytracker.feature.auth.domain.port

import java.time.LocalDateTime
import java.util.UUID

data class RefreshTokenRecord(
    val id: UUID,
    val userId: UUID,
    val tokenHash: String,
    val expiresAt: LocalDateTime,
    val revokedAt: LocalDateTime?,
    val replacedBy: UUID?,
)

interface RefreshTokenRepository {
    suspend fun save(userId: UUID, tokenHash: String, expiresAt: LocalDateTime, now: LocalDateTime): UUID
    suspend fun findByHash(tokenHash: String): RefreshTokenRecord?
    suspend fun revoke(id: UUID, now: LocalDateTime, replacedBy: UUID?)
    suspend fun revokeAllForUser(userId: UUID, now: LocalDateTime)

    suspend fun rotate(
        oldId: UUID,
        userId: UUID,
        newTokenHash: String,
        expiresAt: LocalDateTime,
        now: LocalDateTime,
    ): UUID?
}
