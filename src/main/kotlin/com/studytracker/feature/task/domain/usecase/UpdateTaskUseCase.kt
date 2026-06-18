package com.studytracker.feature.task.domain.usecase

import com.studytracker.feature.task.domain.model.Task
import com.studytracker.feature.task.domain.model.TaskContent
import com.studytracker.feature.task.domain.port.TaskRepository
import com.studytracker.shared.error.AppException
import com.studytracker.shared.ports.TimeProvider
import java.util.UUID

class UpdateTaskUseCase(
    private val tasks: TaskRepository,
    private val time: TimeProvider,
) {
    suspend operator fun invoke(userId: UUID, id: UUID, content: TaskContent): Task =
        tasks.update(id, userId, content, time.now()) ?: throw AppException.NotFound("Task not found")
}
