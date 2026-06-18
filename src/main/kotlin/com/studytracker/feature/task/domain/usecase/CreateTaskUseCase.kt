package com.studytracker.feature.task.domain.usecase

import com.studytracker.feature.task.domain.model.Task
import com.studytracker.feature.task.domain.model.TaskContent
import com.studytracker.feature.task.domain.port.TaskRepository
import com.studytracker.shared.ports.TimeProvider
import java.util.UUID

class CreateTaskUseCase(
    private val tasks: TaskRepository,
    private val time: TimeProvider,
) {
    suspend operator fun invoke(userId: UUID, content: TaskContent): Task =
        tasks.create(userId, content, time.now())
}
