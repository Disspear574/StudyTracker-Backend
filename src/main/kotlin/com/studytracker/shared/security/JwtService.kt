package com.studytracker.shared.security

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import java.time.Instant
import java.util.Date
import java.util.UUID

data class AccessToken(val token: String, val expiresAt: Instant)

class JwtService(
    private val secret: String,
    private val issuer: String,
    private val audience: String,
    private val accessTtlSeconds: Long,
) {
    val realm: String = "studytracker"
    private val algorithm: Algorithm = Algorithm.HMAC256(secret)

    fun createAccessToken(userId: UUID): AccessToken {
        val now = Instant.now()
        val expiresAt = now.plusSeconds(accessTtlSeconds)
        val token = JWT.create()
            .withIssuer(issuer)
            .withAudience(audience)
            .withSubject(userId.toString())
            .withIssuedAt(Date.from(now))
            .withExpiresAt(Date.from(expiresAt))
            .sign(algorithm)
        return AccessToken(token, expiresAt)
    }

    fun verifier(): JWTVerifier =
        JWT.require(algorithm)
            .withIssuer(issuer)
            .withAudience(audience)
            .build()
}
