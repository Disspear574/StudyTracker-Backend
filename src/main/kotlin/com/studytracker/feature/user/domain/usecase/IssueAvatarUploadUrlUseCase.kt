package com.studytracker.feature.user.domain.usecase

import com.studytracker.feature.user.domain.model.AvatarUploadTarget
import com.studytracker.shared.error.AppException
import com.studytracker.shared.ports.TimeProvider
import com.studytracker.shared.ports.UuidV7
import com.studytracker.shared.storage.FileStorage
import java.util.UUID

class IssueAvatarUploadUrlUseCase(
    private val storage: FileStorage,
    private val time: TimeProvider,
) {
    private val ttlSeconds = 300L

    suspend operator fun invoke(userId: UUID, contentType: String): AvatarUploadTarget {
        if (!contentType.startsWith("image/")) {
            throw AppException.Validation("Only image uploads are allowed")
        }
        val key = "avatars/$userId/${UuidV7.next()}"
        return AvatarUploadTarget(
            uploadUrl = storage.presignedPutUrl(key, ttlSeconds),
            objectKey = key,
            expiresAt = time.now().plusSeconds(ttlSeconds),
        )
    }
}
