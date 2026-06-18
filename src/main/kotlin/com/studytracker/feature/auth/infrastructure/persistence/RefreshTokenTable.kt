package com.studytracker.feature.auth.infrastructure.persistence

import com.studytracker.feature.user.infrastructure.persistence.UserTable
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.java.UUIDTable
import org.jetbrains.exposed.v1.javatime.datetime

object RefreshTokenTable : UUIDTable("refresh_tokens") {
    val userId = reference("user_id", UserTable, onDelete = ReferenceOption.CASCADE)
    val tokenHash = varchar("token_hash", 64).uniqueIndex()
    val expiresAt = datetime("expires_at")
    val createdAt = datetime("created_at")
    val revokedAt = datetime("revoked_at").nullable()

    val replacedBy = optReference("replaced_by", RefreshTokenTable, onDelete = ReferenceOption.SET_NULL)
}
