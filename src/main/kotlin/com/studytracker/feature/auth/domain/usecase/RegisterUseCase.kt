package com.studytracker.feature.auth.domain.usecase

import com.studytracker.feature.auth.domain.model.AuthResult
import com.studytracker.feature.auth.domain.model.RegisterCommand
import com.studytracker.feature.auth.domain.model.TokenPair
import com.studytracker.feature.auth.domain.port.RefreshTokenRepository
import com.studytracker.feature.auth.domain.port.TokenIssuer
import com.studytracker.feature.stats.domain.port.DeviceSessionRepository
import com.studytracker.feature.user.domain.port.NewUser
import com.studytracker.feature.user.domain.port.UserRepository
import com.studytracker.shared.error.AppException
import com.studytracker.shared.ports.TimeProvider
import com.studytracker.shared.ports.Transactor
import com.studytracker.shared.security.PasswordEncoder
import java.time.LocalDateTime
import java.util.UUID

class RegisterUseCase(
    private val users: UserRepository,
    private val refreshTokens: RefreshTokenRepository,
    private val devices: DeviceSessionRepository,
    private val encoder: PasswordEncoder,
    private val tokens: TokenIssuer,
    private val time: TimeProvider,
    private val transactor: Transactor,
) {
    suspend operator fun invoke(command: RegisterCommand): AuthResult = transactor.transaction {
        if (users.existsByEmail(command.email)) {
            throw AppException.Conflict("Email already registered")
        }
        val now = time.now()
        val user = users.create(
            NewUser(
                email = command.email,
                passwordHash = encoder.hash(command.password),
                firstName = command.firstName,
                lastName = command.lastName,
                middleName = command.middleName,
            ),
            now,
        )
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
