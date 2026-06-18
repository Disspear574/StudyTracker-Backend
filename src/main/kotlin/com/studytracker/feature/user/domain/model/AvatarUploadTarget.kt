package com.studytracker.feature.user.domain.model

import java.time.LocalDateTime

data class AvatarUploadTarget(
    val uploadUrl: String,
    val objectKey: String,
    val expiresAt: LocalDateTime,
)
