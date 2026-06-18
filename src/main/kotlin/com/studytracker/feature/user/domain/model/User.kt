package com.studytracker.feature.user.domain.model

import java.time.LocalDateTime
import java.util.UUID

data class User(
    val id: UUID,
    val email: String,
    val firstName: String,
    val lastName: String,
    val middleName: String?,
    val avatarKey: String?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
)
