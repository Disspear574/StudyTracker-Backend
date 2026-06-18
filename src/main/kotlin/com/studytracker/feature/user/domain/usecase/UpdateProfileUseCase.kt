package com.studytracker.feature.user.domain.usecase

import com.studytracker.feature.user.domain.model.User
import com.studytracker.feature.user.domain.port.ProfilePatch
import com.studytracker.feature.user.domain.port.UserRepository
import com.studytracker.shared.error.AppException
import com.studytracker.shared.ports.TimeProvider
import java.util.UUID

class UpdateProfileUseCase(
    private val users: UserRepository,
    private val time: TimeProvider,
) {
    suspend operator fun invoke(userId: UUID, patch: ProfilePatch): User {
        val current = users.findById(userId) ?: throw AppException.NotFound("User not found")
        if (patch.email != null && patch.email != current.email && users.existsByEmail(patch.email)) {
            throw AppException.Conflict("Email already in use")
        }
        return users.updateProfile(userId, patch, time.now())
            ?: throw AppException.NotFound("User not found")
    }
}
