package com.studytracker.feature.user.infrastructure.persistence

import org.jetbrains.exposed.v1.core.dao.id.java.UUIDTable
import org.jetbrains.exposed.v1.javatime.datetime

object UserTable : UUIDTable("users") {
    val email = varchar("email", 320).uniqueIndex()
    val passwordHash = varchar("password_hash", 100)
    val firstName = varchar("first_name", 100)
    val lastName = varchar("last_name", 100)
    val middleName = varchar("middle_name", 100).nullable()
    val avatarKey = varchar("avatar_key", 512).nullable()
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")
}
