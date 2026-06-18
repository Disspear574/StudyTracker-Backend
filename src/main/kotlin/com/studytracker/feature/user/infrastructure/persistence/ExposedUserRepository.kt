package com.studytracker.feature.user.infrastructure.persistence

import com.studytracker.feature.user.domain.model.User
import com.studytracker.feature.user.domain.port.NewUser
import com.studytracker.feature.user.domain.port.ProfilePatch
import com.studytracker.feature.user.domain.port.UserCredentials
import com.studytracker.feature.user.domain.port.UserRepository
import com.studytracker.shared.db.dbQuery
import com.studytracker.shared.db.isUniqueViolation
import com.studytracker.shared.error.AppException
import com.studytracker.shared.ports.UuidV7
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.JdbcTransaction
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
import java.time.LocalDateTime
import java.util.UUID

class ExposedUserRepository : UserRepository {

    override suspend fun existsByEmail(email: String): Boolean = dbQuery {
        UserTable.selectAll().where { UserTable.email eq email }.limit(1).count() > 0
    }

    override suspend fun create(newUser: NewUser, now: LocalDateTime): User = try {
        dbQuery {
            val id = UuidV7.next()
            UserTable.insert {
                it[UserTable.id] = id
                it[email] = newUser.email
                it[passwordHash] = newUser.passwordHash
                it[firstName] = newUser.firstName
                it[lastName] = newUser.lastName
                it[middleName] = newUser.middleName
                it[createdAt] = now
                it[updatedAt] = now
            }
            User(id, newUser.email, newUser.firstName, newUser.lastName, newUser.middleName, null, now, now)
        }
    } catch (e: Exception) {
        if (e.isUniqueViolation()) throw AppException.Conflict("Email already registered") else throw e
    }

    override suspend fun findById(id: UUID): User? = dbQuery { fetchUser(id) }

    override suspend fun findCredentials(email: String): UserCredentials? = dbQuery {
        UserTable.selectAll().where { UserTable.email eq email }.firstOrNull()?.let {
            UserCredentials(it[UserTable.id].value, it[UserTable.passwordHash])
        }
    }

    override suspend fun updateProfile(id: UUID, patch: ProfilePatch, now: LocalDateTime): User? = try {
        dbQuery {
            val updated = UserTable.update({ UserTable.id eq id }) {
                patch.firstName?.let { v -> it[firstName] = v }
                patch.lastName?.let { v -> it[lastName] = v }
                patch.middleName?.let { v -> it[middleName] = v }
                patch.email?.let { v -> it[email] = v }
                it[updatedAt] = now
            }
            if (updated == 0) null else fetchUser(id)
        }
    } catch (e: Exception) {
        if (e.isUniqueViolation()) throw AppException.Conflict("Email already in use") else throw e
    }

    override suspend fun updateAvatarKey(id: UUID, avatarKey: String?, now: LocalDateTime): User? = dbQuery {
        val updated = UserTable.update({ UserTable.id eq id }) {
            it[UserTable.avatarKey] = avatarKey
            it[updatedAt] = now
        }
        if (updated == 0) null else fetchUser(id)
    }

    override suspend fun deleteById(id: UUID): Boolean = dbQuery {
        UserTable.deleteWhere { UserTable.id eq id } > 0
    }

    private fun JdbcTransaction.fetchUser(id: UUID): User? =
        UserTable.selectAll().where { UserTable.id eq id }.firstOrNull()?.toUser()
}

private fun ResultRow.toUser(): User = User(
    id = this[UserTable.id].value,
    email = this[UserTable.email],
    firstName = this[UserTable.firstName],
    lastName = this[UserTable.lastName],
    middleName = this[UserTable.middleName],
    avatarKey = this[UserTable.avatarKey],
    createdAt = this[UserTable.createdAt],
    updatedAt = this[UserTable.updatedAt],
)
