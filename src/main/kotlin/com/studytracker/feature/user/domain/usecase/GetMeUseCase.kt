package com.studytracker.feature.user.domain.usecase

import com.studytracker.feature.user.domain.model.User
import com.studytracker.feature.user.domain.port.UserRepository
import com.studytracker.shared.error.AppException
import java.util.UUID

class GetMeUseCase(private val users: UserRepository) {
    suspend operator fun invoke(userId: UUID): User =
        users.findById(userId) ?: throw AppException.NotFound("User not found")
}
