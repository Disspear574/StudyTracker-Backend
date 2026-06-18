package com.studytracker.feature.auth.domain.model

import com.studytracker.feature.user.domain.model.User
import java.time.LocalDateTime

data class TokenPair(
    val accessToken: String,
    val accessExpiresAt: LocalDateTime,
    val refreshToken: String,
)

data class AuthResult(
    val tokens: TokenPair,
    val user: User,
)
