package com.studytracker.feature.auth.domain.usecase

import com.studytracker.feature.auth.domain.port.RefreshTokenRepository
import com.studytracker.feature.auth.domain.port.TokenIssuer
import com.studytracker.shared.ports.TimeProvider

class LogoutUseCase(
    private val refreshTokens: RefreshTokenRepository,
    private val tokens: TokenIssuer,
    private val time: TimeProvider,
) {
    suspend operator fun invoke(rawRefreshToken: String) {
        val record = refreshTokens.findByHash(tokens.hashRefreshToken(rawRefreshToken)) ?: return
        refreshTokens.revoke(record.id, time.now(), null)
    }
}
