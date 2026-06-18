package com.studytracker.feature.auth.domain.usecase

import com.studytracker.feature.auth.domain.model.TokenPair
import com.studytracker.feature.auth.domain.port.RefreshTokenRepository
import com.studytracker.feature.auth.domain.port.TokenIssuer
import com.studytracker.shared.error.AppException
import com.studytracker.shared.ports.TimeProvider

class RefreshUseCase(
    private val refreshTokens: RefreshTokenRepository,
    private val tokens: TokenIssuer,
    private val time: TimeProvider,
) {
    suspend operator fun invoke(rawRefreshToken: String): TokenPair {
        val hash = tokens.hashRefreshToken(rawRefreshToken)
        val record = refreshTokens.findByHash(hash)
            ?: throw AppException.Unauthorized("Invalid refresh token")
        val now = time.now()

        if (record.revokedAt != null) {
            refreshTokens.revokeAllForUser(record.userId, now)
            throw AppException.Unauthorized("Refresh token reuse detected")
        }
        if (!record.expiresAt.isAfter(now)) {
            throw AppException.Unauthorized("Refresh token expired")
        }

        val newRefresh = tokens.generateRefreshToken()
        refreshTokens.rotate(record.id, record.userId, newRefresh.tokenHash, newRefresh.expiresAt, now)
            ?: run {
                refreshTokens.revokeAllForUser(record.userId, now)
                throw AppException.Unauthorized("Refresh token reuse detected")
            }
        val access = tokens.issueAccessToken(record.userId)
        return TokenPair(access.token, access.expiresAt, newRefresh.rawToken)
    }
}
