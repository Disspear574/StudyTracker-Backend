package com.studytracker.feature.user.domain.port

import com.studytracker.feature.user.domain.model.User
import java.time.LocalDateTime
import java.util.UUID

data class UserCredentials(val userId: UUID, val passwordHash: String)

data class NewUser(
    val email: String,
    val passwordHash: String,
    val firstName: String,
    val lastName: String,
    val middleName: String?,
)

data class ProfilePatch(
    val firstName: String? = null,
    val lastName: String? = null,
    val middleName: String? = null,
    val email: String? = null,
)

interface UserRepository {
    suspend fun existsByEmail(email: String): Boolean
    suspend fun create(newUser: NewUser, now: LocalDateTime): User
    suspend fun findById(id: UUID): User?
    suspend fun findCredentials(email: String): UserCredentials?
    suspend fun updateProfile(id: UUID, patch: ProfilePatch, now: LocalDateTime): User?
    suspend fun updateAvatarKey(id: UUID, avatarKey: String?, now: LocalDateTime): User?
    suspend fun deleteById(id: UUID): Boolean
}
