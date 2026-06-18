package com.studytracker.feature.task.domain.usecase

import com.studytracker.feature.task.domain.model.TaskListResult
import com.studytracker.feature.task.domain.port.TaskRepository
import java.time.LocalDate
import java.util.UUID

class GetTasksByDateUseCase(private val tasks: TaskRepository) {
    suspend operator fun invoke(userId: UUID, date: LocalDate): TaskListResult {
        val items = tasks.listForUserByDate(userId, date)
        return TaskListResult(items = items, completed = items.count { it.isCompleted }, total = items.size)
    }
}
