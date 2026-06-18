package com.studytracker.feature.user.infrastructure.web.dto

import com.studytracker.feature.user.domain.model.AvatarUploadTarget
import com.studytracker.feature.user.domain.model.User
import com.studytracker.feature.user.domain.port.ProfilePatch
import com.studytracker.shared.storage.FileStorage
import com.studytracker.shared.util.normalizeEmail
import kotlinx.serialization.Serializable
import java.time.ZoneOffset

private const val AVATAR_GET_TTL_SECONDS = 3600L

@Serializable
data class UserResponse(
    val id: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val middleName: String? = null,
    val avatarUrl: String? = null,
)

@Serializable
data class UpdateProfileRequest(
    val firstName: String? = null,
    val lastName: String? = null,
    val middleName: String? = null,
    val email: String? = null,
)

@Serializable
data class AvatarUploadUrlRequest(val contentType: String)

@Serializable
data class AvatarUploadUrlResponse(val uploadUrl: String, val objectKey: String, val expiresAt: String)

@Serializable
data class ConfirmAvatarRequest(val objectKey: String)

fun User.toResponse(fileStorage: FileStorage): UserResponse = UserResponse(
    id = id.toString(),
    email = email,
    firstName = firstName,
    lastName = lastName,
    middleName = middleName,
    avatarUrl = avatarKey?.let { fileStorage.presignedGetUrl(it, AVATAR_GET_TTL_SECONDS) },
)

fun UpdateProfileRequest.toPatch(): ProfilePatch =
    ProfilePatch(firstName = firstName, lastName = lastName, middleName = middleName, email = email?.normalizeEmail())

fun AvatarUploadTarget.toResponse(): AvatarUploadUrlResponse =
    AvatarUploadUrlResponse(
        uploadUrl = uploadUrl,
        objectKey = objectKey,
        expiresAt = expiresAt.atOffset(ZoneOffset.UTC).toString(),
    )
