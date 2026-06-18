package com.studytracker.feature.auth.infrastructure.web.dto

import com.studytracker.feature.auth.domain.model.AuthResult
import com.studytracker.feature.auth.domain.model.LoginCommand
import com.studytracker.feature.auth.domain.model.RegisterCommand
import com.studytracker.feature.auth.domain.model.TokenPair
import com.studytracker.feature.stats.domain.model.DeviceInfo
import com.studytracker.feature.stats.domain.model.OsType
import com.studytracker.feature.user.infrastructure.web.dto.UserResponse
import com.studytracker.feature.user.infrastructure.web.dto.toResponse
import com.studytracker.shared.error.AppException
import com.studytracker.shared.storage.FileStorage
import com.studytracker.shared.util.normalizeEmail
import kotlinx.serialization.Serializable
import java.time.ZoneOffset

@Serializable
data class DeviceDto(
    val uuid: String,
    val os: String,
    val osVersion: String,
    val appVersion: String,
    val model: String? = null,
)

@Serializable
data class RegisterRequest(
    val email: String,
    val password: String,
    val firstName: String,
    val lastName: String,
    val middleName: String? = null,
    val device: DeviceDto,
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String,
    val device: DeviceDto,
)

@Serializable
data class RefreshRequest(val refreshToken: String)

@Serializable
data class LogoutRequest(val refreshToken: String)

@Serializable
data class ForgotPasswordRequest(val email: String)

@Serializable
data class TokenPairResponse(val accessToken: String, val refreshToken: String, val accessExpiresAt: String)

@Serializable
data class AuthResponse(val tokens: TokenPairResponse, val user: UserResponse)

fun DeviceDto.toDomain(): DeviceInfo {
    val osType = try {
        OsType.valueOf(os.uppercase())
    } catch (_: IllegalArgumentException) {
        throw AppException.Validation("Unknown os value: $os")
    }
    return DeviceInfo(uuid, osType, osVersion, appVersion, model)
}

fun RegisterRequest.toCommand(): RegisterCommand =
    RegisterCommand(email.normalizeEmail(), password, firstName, lastName, middleName, device.toDomain())

fun LoginRequest.toCommand(): LoginCommand =
    LoginCommand(email.normalizeEmail(), password, device.toDomain())

fun TokenPair.toResponse(): TokenPairResponse =
    TokenPairResponse(accessToken, refreshToken, accessExpiresAt.atOffset(ZoneOffset.UTC).toString())

fun AuthResult.toResponse(fileStorage: FileStorage): AuthResponse =
    AuthResponse(tokens.toResponse(), user.toResponse(fileStorage))
