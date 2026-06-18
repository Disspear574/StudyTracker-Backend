package com.studytracker.feature.auth.infrastructure.security

import com.studytracker.feature.auth.domain.port.GeneratedRefreshToken
import com.studytracker.feature.auth.domain.port.IssuedAccessToken
import com.studytracker.feature.auth.domain.port.TokenIssuer
import com.studytracker.shared.ports.TimeProvider
import com.studytracker.shared.security.JwtService
import java.security.MessageDigest
import java.security.SecureRandom
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.Base64
import java.util.UUID

class AuthTokenIssuer(
    private val jwtService: JwtService,
    private val refreshTtlSeconds: Long,
    private val time: TimeProvider,
) : TokenIssuer {
    private val random = SecureRandom()

    override fun issueAccessToken(userId: UUID): IssuedAccessToken {
        val access = jwtService.createAccessToken(userId)
        val expiresAt = LocalDateTime.ofInstant(access.expiresAt, ZoneOffset.UTC)
        return IssuedAccessToken(access.token, expiresAt)
    }

    override fun generateRefreshToken(): GeneratedRefreshToken {
        val bytes = ByteArray(32).also(random::nextBytes)
        val raw = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
        return GeneratedRefreshToken(raw, hashRefreshToken(raw), time.now().plusSeconds(refreshTtlSeconds))
    }

    override fun hashRefreshToken(rawToken: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest(rawToken.toByteArray())
            .joinToString("") { "%02x".format(it) }
}
