package com.studytracker.feature.user.domain.usecase

import com.studytracker.feature.user.domain.model.User
import com.studytracker.feature.user.domain.port.UserRepository
import com.studytracker.shared.error.AppException
import com.studytracker.shared.ports.TimeProvider
import com.studytracker.shared.storage.FileStorage
import java.util.UUID

class ConfirmAvatarUseCase(
    private val users: UserRepository,
    private val storage: FileStorage,
    private val time: TimeProvider,
) {
    suspend operator fun invoke(userId: UUID, objectKey: String): User {
        if (!objectKey.startsWith("avatars/$userId/")) {
            throw AppException.Forbidden("Object key does not belong to the user")
        }
        val stored = storage.stat(objectKey)
            ?: throw AppException.Validation("Avatar object was not uploaded")
        if (!stored.contentType.startsWith("image/")) {
            throw AppException.Validation("Avatar must be an image")
        }
        return users.updateAvatarKey(userId, objectKey, time.now())
            ?: throw AppException.NotFound("User not found")
    }
}
