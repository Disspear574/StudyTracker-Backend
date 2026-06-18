package com.studytracker.support

import com.studytracker.feature.auth.domain.port.GeneratedRefreshToken
import com.studytracker.feature.auth.domain.port.IssuedAccessToken
import com.studytracker.feature.auth.domain.port.RefreshTokenRecord
import com.studytracker.feature.auth.domain.port.RefreshTokenRepository
import com.studytracker.feature.auth.domain.port.TokenIssuer
import com.studytracker.feature.stats.domain.model.DeviceInfo
import com.studytracker.feature.stats.domain.port.DeviceSessionRepository
import com.studytracker.feature.user.domain.model.User
import com.studytracker.feature.user.domain.port.NewUser
import com.studytracker.feature.user.domain.port.ProfilePatch
import com.studytracker.feature.user.domain.port.UserCredentials
import com.studytracker.feature.user.domain.port.UserRepository
import com.studytracker.shared.ports.TimeProvider
import com.studytracker.shared.ports.Transactor
import com.studytracker.shared.security.PasswordEncoder
import java.time.LocalDateTime
import java.util.UUID

class FakeTransactor : Transactor {
    override suspend fun <T> transaction(block: suspend () -> T): T = block()
}

class MutableTimeProvider(var current: LocalDateTime = LocalDateTime.of(2026, 6, 1, 9, 0)) : TimeProvider {
    override fun now(): LocalDateTime = current
    fun advanceSeconds(seconds: Long) { current = current.plusSeconds(seconds) }
}

class FakePasswordEncoder : PasswordEncoder {
    override fun hash(raw: String): String = "enc:$raw"
    override fun matches(raw: String, hash: String): Boolean = hash == "enc:$raw"
}

class FakeTokenIssuer(private val time: TimeProvider, private val refreshTtlSeconds: Long = 2_592_000) : TokenIssuer {
    private var refreshSeq = 0
    override fun issueAccessToken(userId: UUID): IssuedAccessToken =
        IssuedAccessToken("access:$userId", time.now().plusSeconds(3600))

    override fun generateRefreshToken(): GeneratedRefreshToken {
        val raw = "refresh-${++refreshSeq}"
        return GeneratedRefreshToken(raw, hashRefreshToken(raw), time.now().plusSeconds(refreshTtlSeconds))
    }

    override fun hashRefreshToken(rawToken: String): String = "h:$rawToken"
}

class FakeUserRepository : UserRepository {
    val byId = mutableMapOf<UUID, User>()
    private val emailToId = mutableMapOf<String, UUID>()
    private val passwordHashByEmail = mutableMapOf<String, String>()
    private var seq = 0

    override suspend fun existsByEmail(email: String): Boolean = emailToId.containsKey(email)

    override suspend fun create(newUser: NewUser, now: LocalDateTime): User {
        val id = UUID.nameUUIDFromBytes("user-${++seq}".toByteArray())
        val user = User(id, newUser.email, newUser.firstName, newUser.lastName, newUser.middleName, null, now, now)
        byId[id] = user
        emailToId[newUser.email] = id
        passwordHashByEmail[newUser.email] = newUser.passwordHash
        return user
    }

    override suspend fun findById(id: UUID): User? = byId[id]

    override suspend fun findCredentials(email: String): UserCredentials? {
        val id = emailToId[email] ?: return null
        return UserCredentials(id, passwordHashByEmail.getValue(email))
    }

    override suspend fun updateProfile(id: UUID, patch: ProfilePatch, now: LocalDateTime): User? {
        val existing = byId[id] ?: return null
        val updated = existing.copy(
            firstName = patch.firstName ?: existing.firstName,
            lastName = patch.lastName ?: existing.lastName,
            middleName = patch.middleName ?: existing.middleName,
            email = patch.email ?: existing.email,
            updatedAt = now,
        )
        byId[id] = updated
        if (patch.email != null) emailToId[patch.email] = id
        return updated
    }

    override suspend fun updateAvatarKey(id: UUID, avatarKey: String?, now: LocalDateTime): User? {
        val existing = byId[id] ?: return null
        val updated = existing.copy(avatarKey = avatarKey, updatedAt = now)
        byId[id] = updated
        return updated
    }

    override suspend fun deleteById(id: UUID): Boolean {
        val user = byId.remove(id) ?: return false
        emailToId.remove(user.email)
        passwordHashByEmail.remove(user.email)
        return true
    }
}

class FakeRefreshTokenRepository : RefreshTokenRepository {
    val records = mutableListOf<RefreshTokenRecord>()
    private var seq = 0

    override suspend fun save(userId: UUID, tokenHash: String, expiresAt: LocalDateTime, now: LocalDateTime): UUID {
        val id = UUID.nameUUIDFromBytes("rt-${++seq}".toByteArray())
        records.add(RefreshTokenRecord(id, userId, tokenHash, expiresAt, null, null))
        return id
    }

    override suspend fun findByHash(tokenHash: String): RefreshTokenRecord? =
        records.lastOrNull { it.tokenHash == tokenHash }

    override suspend fun revoke(id: UUID, now: LocalDateTime, replacedBy: UUID?) {
        replace(id) { it.copy(revokedAt = now, replacedBy = replacedBy) }
    }

    override suspend fun revokeAllForUser(userId: UUID, now: LocalDateTime) {
        records.replaceAll { if (it.userId == userId && it.revokedAt == null) it.copy(revokedAt = now) else it }
    }

    override suspend fun rotate(
        oldId: UUID,
        userId: UUID,
        newTokenHash: String,
        expiresAt: LocalDateTime,
        now: LocalDateTime,
    ): UUID? {
        val existing = records.firstOrNull { it.id == oldId }
        if (existing == null || existing.revokedAt != null) return null
        val newId = save(userId, newTokenHash, expiresAt, now)
        replace(oldId) { it.copy(revokedAt = now, replacedBy = newId) }
        return newId
    }

    private fun replace(id: UUID, transform: (RefreshTokenRecord) -> RefreshTokenRecord) {
        val idx = records.indexOfFirst { it.id == id }
        if (idx >= 0) records[idx] = transform(records[idx])
    }
}

class FakeDeviceSessionRepository : DeviceSessionRepository {
    val recorded = mutableListOf<Triple<UUID, DeviceInfo, LocalDateTime>>()
    override suspend fun record(userId: UUID, info: DeviceInfo, now: LocalDateTime) {
        recorded.add(Triple(userId, info, now))
    }
}
