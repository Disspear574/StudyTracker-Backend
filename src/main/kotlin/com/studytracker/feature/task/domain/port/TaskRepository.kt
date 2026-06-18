package com.studytracker.feature.task.domain.port

import com.studytracker.feature.task.domain.model.Task
import com.studytracker.feature.task.domain.model.TaskContent
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

interface TaskRepository {
    suspend fun create(userId: UUID, content: TaskContent, now: LocalDateTime): Task
    suspend fun findByIdForUser(id: UUID, userId: UUID): Task?
    suspend fun listForUserByDate(userId: UUID, date: LocalDate): List<Task>
    suspend fun update(id: UUID, userId: UUID, content: TaskContent, now: LocalDateTime): Task?
    suspend fun setCompleted(id: UUID, userId: UUID, completed: Boolean, now: LocalDateTime): Task?
    suspend fun deleteForUser(id: UUID, userId: UUID): Boolean
}
