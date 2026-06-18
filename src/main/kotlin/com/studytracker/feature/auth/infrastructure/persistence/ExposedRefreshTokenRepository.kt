package com.studytracker.feature.auth.infrastructure.persistence

import com.studytracker.feature.auth.domain.port.RefreshTokenRecord
import com.studytracker.feature.auth.domain.port.RefreshTokenRepository
import com.studytracker.shared.db.dbQuery
import com.studytracker.shared.ports.UuidV7
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.isNull
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
import java.time.LocalDateTime
import java.util.UUID

class ExposedRefreshTokenRepository : RefreshTokenRepository {

    override suspend fun save(userId: UUID, tokenHash: String, expiresAt: LocalDateTime, now: LocalDateTime): UUID =
        dbQuery {
            val id = UuidV7.next()
            RefreshTokenTable.insert {
                it[RefreshTokenTable.id] = id
                it[RefreshTokenTable.userId] = userId
                it[RefreshTokenTable.tokenHash] = tokenHash
                it[RefreshTokenTable.expiresAt] = expiresAt
                it[createdAt] = now
            }
            id
        }

    override suspend fun findByHash(tokenHash: String): RefreshTokenRecord? = dbQuery {
        RefreshTokenTable.selectAll().where { RefreshTokenTable.tokenHash eq tokenHash }.firstOrNull()?.toRecord()
    }

    override suspend fun revoke(id: UUID, now: LocalDateTime, replacedBy: UUID?) {
        dbQuery {
            RefreshTokenTable.update({ RefreshTokenTable.id eq id }) {
                it[revokedAt] = now
                it[RefreshTokenTable.replacedBy] = replacedBy
            }
        }
    }

    override suspend fun revokeAllForUser(userId: UUID, now: LocalDateTime) {
        dbQuery {
            RefreshTokenTable.update({ (RefreshTokenTable.userId eq userId) and RefreshTokenTable.revokedAt.isNull() }) {
                it[revokedAt] = now
            }
        }
    }

    override suspend fun rotate(
        oldId: UUID,
        userId: UUID,
        newTokenHash: String,
        expiresAt: LocalDateTime,
        now: LocalDateTime,
    ): UUID? = dbQuery {
        val revoked = RefreshTokenTable.update(
            { (RefreshTokenTable.id eq oldId) and RefreshTokenTable.revokedAt.isNull() },
        ) {
            it[revokedAt] = now
        }
        if (revoked == 0) return@dbQuery null

        val newId = UuidV7.next()
        RefreshTokenTable.insert {
            it[RefreshTokenTable.id] = newId
            it[RefreshTokenTable.userId] = userId
            it[tokenHash] = newTokenHash
            it[RefreshTokenTable.expiresAt] = expiresAt
            it[createdAt] = now
        }
        RefreshTokenTable.update({ RefreshTokenTable.id eq oldId }) {
            it[replacedBy] = newId
        }
        newId
    }
}

private fun ResultRow.toRecord(): RefreshTokenRecord = RefreshTokenRecord(
    id = this[RefreshTokenTable.id].value,
    userId = this[RefreshTokenTable.userId].value,
    tokenHash = this[RefreshTokenTable.tokenHash],
    expiresAt = this[RefreshTokenTable.expiresAt],
    revokedAt = this[RefreshTokenTable.revokedAt],
    replacedBy = this[RefreshTokenTable.replacedBy]?.value,
)
