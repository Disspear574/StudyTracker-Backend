package com.studytracker

import com.studytracker.feature.auth.domain.model.LoginCommand
import com.studytracker.feature.auth.domain.model.RegisterCommand
import com.studytracker.feature.auth.domain.usecase.LoginUseCase
import com.studytracker.feature.auth.domain.usecase.LogoutUseCase
import com.studytracker.feature.auth.domain.usecase.RefreshUseCase
import com.studytracker.feature.auth.domain.usecase.RegisterUseCase
import com.studytracker.feature.stats.domain.model.DeviceInfo
import com.studytracker.feature.stats.domain.model.OsType
import com.studytracker.shared.error.AppException
import com.studytracker.support.FakeDeviceSessionRepository
import com.studytracker.support.FakePasswordEncoder
import com.studytracker.support.FakeTransactor
import com.studytracker.support.FakeRefreshTokenRepository
import com.studytracker.support.FakeTokenIssuer
import com.studytracker.support.FakeUserRepository
import com.studytracker.support.MutableTimeProvider
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AuthUseCasesTest {
    private val time = MutableTimeProvider()
    private val users = FakeUserRepository()
    private val refreshTokens = FakeRefreshTokenRepository()
    private val devices = FakeDeviceSessionRepository()
    private val encoder = FakePasswordEncoder()
    private val tokens = FakeTokenIssuer(time)

    private val register = RegisterUseCase(users, refreshTokens, devices, encoder, tokens, time, FakeTransactor())
    private val login = LoginUseCase(users, refreshTokens, devices, encoder, tokens, time, FakeTransactor())
    private val refresh = RefreshUseCase(refreshTokens, tokens, time)
    private val logout = LogoutUseCase(refreshTokens, tokens, time)

    private val device = DeviceInfo("dev-1", OsType.ANDROID, "15", "1.0.0", "Pixel")
    private fun registerCmd(email: String = "a@b.c") =
        RegisterCommand(email, "password123", "Иван", "Иванов", "Иванович", device)

    @Test
    fun `register creates user, stores hashed password, issues tokens, records device`() = runBlocking {
        val result = register(registerCmd())

        assertEquals("a@b.c", result.user.email)
        assertTrue(result.tokens.accessToken.isNotBlank())
        assertTrue(result.tokens.refreshToken.isNotBlank())
        assertEquals(1, refreshTokens.records.size)
        assertEquals(1, devices.recorded.size)
        assertEquals("enc:password123", users.findCredentials("a@b.c")!!.passwordHash)
    }

    @Test
    fun `register with existing email throws Conflict`() = runBlocking {
        register(registerCmd())
        assertFailsWith<AppException.Conflict> { register(registerCmd()) }
        Unit
    }

    @Test
    fun `login with correct password returns tokens and user`() = runBlocking {
        register(registerCmd())
        val result = login(LoginCommand("a@b.c", "password123", device))
        assertEquals("a@b.c", result.user.email)
        assertTrue(result.tokens.accessToken.isNotBlank())
    }

    @Test
    fun `login with wrong password throws Unauthorized`() = runBlocking {
        register(registerCmd())
        assertFailsWith<AppException.Unauthorized> { login(LoginCommand("a@b.c", "wrong", device)) }
        Unit
    }

    @Test
    fun `login with unknown email throws Unauthorized`() = runBlocking {
        assertFailsWith<AppException.Unauthorized> { login(LoginCommand("nope@b.c", "x", device)) }
        Unit
    }

    @Test
    fun `refresh rotates token - old revoked with replacedBy, new usable`() = runBlocking {
        val raw = register(registerCmd()).tokens.refreshToken

        val pair = refresh(raw)

        assertNotEquals(raw, pair.refreshToken)
        val old = refreshTokens.records.first { it.tokenHash == tokens.hashRefreshToken(raw) }
        assertNotNull(old.revokedAt)
        assertNotNull(old.replacedBy)
        assertNotEquals(pair.refreshToken, refresh(pair.refreshToken).refreshToken)
    }

    @Test
    fun `refresh with expired token throws Unauthorized`() = runBlocking {
        val raw = register(registerCmd()).tokens.refreshToken
        time.advanceSeconds(3_000_000)
        assertFailsWith<AppException.Unauthorized> { refresh(raw) }
        Unit
    }

    @Test
    fun `refresh reuse of a revoked token revokes the whole family`() = runBlocking {
        val raw = register(registerCmd()).tokens.refreshToken
        refresh(raw)

        assertFailsWith<AppException.Unauthorized> { refresh(raw) }
        assertTrue(refreshTokens.records.all { it.revokedAt != null }, "whole token family must be revoked")
    }

    @Test
    fun `refresh with unknown token throws Unauthorized`() = runBlocking {
        assertFailsWith<AppException.Unauthorized> { refresh("never-issued") }
        Unit
    }

    @Test
    fun `logout revokes the refresh token`() = runBlocking {
        val raw = register(registerCmd()).tokens.refreshToken
        logout(raw)
        assertTrue(refreshTokens.records.first().revokedAt != null)
    }

    @Test
    fun `logout with unknown token is a no-op`() = runBlocking {
        logout("never-issued")
    }
}
