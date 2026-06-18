package com.studytracker.feature.task.domain.usecase

import com.studytracker.feature.task.domain.model.Task
import com.studytracker.feature.task.domain.port.TaskRepository
import com.studytracker.shared.error.AppException
import java.util.UUID

class GetTaskUseCase(private val tasks: TaskRepository) {
    suspend operator fun invoke(userId: UUID, id: UUID): Task =
        tasks.findByIdForUser(id, userId) ?: throw AppException.NotFound("Task not found")
}
