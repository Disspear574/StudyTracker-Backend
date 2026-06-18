package com.studytracker.feature.auth.domain.usecase

import com.studytracker.feature.auth.domain.model.AuthResult
import com.studytracker.feature.auth.domain.model.LoginCommand
import com.studytracker.feature.auth.domain.model.TokenPair
import com.studytracker.feature.auth.domain.port.RefreshTokenRepository
import com.studytracker.feature.auth.domain.port.TokenIssuer
import com.studytracker.feature.stats.domain.port.DeviceSessionRepository
import com.studytracker.feature.user.domain.port.UserRepository
import com.studytracker.shared.error.AppException
import com.studytracker.shared.ports.TimeProvider
import com.studytracker.shared.ports.Transactor
import com.studytracker.shared.security.PasswordEncoder
import java.time.LocalDateTime
import java.util.UUID

class LoginUseCase(
    private val users: UserRepository,
    private val refreshTokens: RefreshTokenRepository,
    private val devices: DeviceSessionRepository,
    private val encoder: PasswordEncoder,
    private val tokens: TokenIssuer,
    private val time: TimeProvider,
    private val transactor: Transactor,
) {
    suspend operator fun invoke(command: LoginCommand): AuthResult = transactor.transaction {
        val credentials = users.findCredentials(command.email)
            ?: throw AppException.Unauthorized("Invalid email or password")
        if (!encoder.matches(command.password, credentials.passwordHash)) {
            throw AppException.Unauthorized("Invalid email or password")
        }
        val user = users.findById(credentials.userId)
            ?: throw AppException.Unauthorized("Invalid email or password")

        val now = time.now()
        val tokenPair = issueTokens(user.id, now)
        devices.record(user.id, command.device, now)
        AuthResult(tokenPair, user)
    }

    private suspend fun issueTokens(userId: UUID, now: LocalDateTime): TokenPair {
        val access = tokens.issueAccessToken(userId)
        val refresh = tokens.generateRefreshToken()
        refreshTokens.save(userId, refresh.tokenHash, refresh.expiresAt, now)
        return TokenPair(access.token, access.expiresAt, refresh.rawToken)
    }
}
