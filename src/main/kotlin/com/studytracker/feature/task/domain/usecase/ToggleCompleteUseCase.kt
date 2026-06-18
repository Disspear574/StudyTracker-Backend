package com.studytracker.feature.task.domain.usecase

import com.studytracker.feature.task.domain.model.Task
import com.studytracker.feature.task.domain.port.TaskRepository
import com.studytracker.shared.error.AppException
import com.studytracker.shared.ports.TimeProvider
import java.util.UUID

class ToggleCompleteUseCase(
    private val tasks: TaskRepository,
    private val time: TimeProvider,
) {
    suspend operator fun invoke(userId: UUID, id: UUID, completed: Boolean): Task =
        tasks.setCompleted(id, userId, completed, time.now()) ?: throw AppException.NotFound("Task not found")
}
