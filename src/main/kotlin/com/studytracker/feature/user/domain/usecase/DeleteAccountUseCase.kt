package com.studytracker.feature.user.domain.usecase

import com.studytracker.feature.user.domain.port.UserRepository
import com.studytracker.shared.error.AppException
import com.studytracker.shared.storage.FileStorage
import java.util.UUID

class DeleteAccountUseCase(
    private val users: UserRepository,
    private val storage: FileStorage,
) {
    suspend operator fun invoke(userId: UUID) {
        val user = users.findById(userId) ?: throw AppException.NotFound("User not found")
        user.avatarKey?.let { storage.delete(it) }
        users.deleteById(userId)
    }
}
