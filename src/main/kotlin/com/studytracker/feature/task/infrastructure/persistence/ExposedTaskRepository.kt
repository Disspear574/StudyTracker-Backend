package com.studytracker.feature.task.infrastructure.persistence

import com.studytracker.feature.task.domain.model.Task
import com.studytracker.feature.task.domain.model.TaskContent
import com.studytracker.feature.task.domain.port.TaskRepository
import com.studytracker.shared.db.dbQuery
import com.studytracker.shared.ports.UuidV7
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.JdbcTransaction
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

class ExposedTaskRepository : TaskRepository {

    override suspend fun create(userId: UUID, content: TaskContent, now: LocalDateTime): Task = dbQuery {
        val id = UuidV7.next()
        TaskTable.insert {
            it[TaskTable.id] = id
            it[TaskTable.userId] = userId
            it[title] = content.title
            it[description] = content.description
            it[taskDate] = content.date
            it[startTime] = content.startTime
            it[intervalStart] = content.intervalStart
            it[intervalEnd] = content.intervalEnd
            it[color] = content.color
            it[notificationOffset] = content.notificationOffset
            it[isCompleted] = false
            it[createdAt] = now
            it[updatedAt] = now
        }
        Task(
            id, userId, content.title, content.description, content.date, content.startTime,
            content.intervalStart, content.intervalEnd, content.color, content.notificationOffset,
            false, now, now,
        )
    }

    override suspend fun findByIdForUser(id: UUID, userId: UUID): Task? = dbQuery { fetchTask(id, userId) }

    override suspend fun listForUserByDate(userId: UUID, date: LocalDate): List<Task> = dbQuery {
        TaskTable.selectAll()
            .where { (TaskTable.userId eq userId) and (TaskTable.taskDate eq date) }
            .orderBy(TaskTable.startTime)
            .map { it.toTask() }
    }

    override suspend fun update(id: UUID, userId: UUID, content: TaskContent, now: LocalDateTime): Task? = dbQuery {
        val updated = TaskTable.update({ (TaskTable.id eq id) and (TaskTable.userId eq userId) }) {
            it[title] = content.title
            it[description] = content.description
            it[taskDate] = content.date
            it[startTime] = content.startTime
            it[intervalStart] = content.intervalStart
            it[intervalEnd] = content.intervalEnd
            it[color] = content.color
            it[notificationOffset] = content.notificationOffset
            it[updatedAt] = now
        }
        if (updated == 0) null else fetchTask(id, userId)
    }

    override suspend fun setCompleted(id: UUID, userId: UUID, completed: Boolean, now: LocalDateTime): Task? = dbQuery {
        val updated = TaskTable.update({ (TaskTable.id eq id) and (TaskTable.userId eq userId) }) {
            it[isCompleted] = completed
            it[updatedAt] = now
        }
        if (updated == 0) null else fetchTask(id, userId)
    }

    override suspend fun deleteForUser(id: UUID, userId: UUID): Boolean = dbQuery {
        TaskTable.deleteWhere { (TaskTable.id eq id) and (TaskTable.userId eq userId) } > 0
    }

    private fun JdbcTransaction.fetchTask(id: UUID, userId: UUID): Task? =
        TaskTable.selectAll()
            .where { (TaskTable.id eq id) and (TaskTable.userId eq userId) }
            .firstOrNull()
            ?.toTask()
}

private fun ResultRow.toTask(): Task = Task(
    id = this[TaskTable.id].value,
    userId = this[TaskTable.userId].value,
    title = this[TaskTable.title],
    description = this[TaskTable.description],
    date = this[TaskTable.taskDate],
    startTime = this[TaskTable.startTime],
    intervalStart = this[TaskTable.intervalStart],
    intervalEnd = this[TaskTable.intervalEnd],
    color = this[TaskTable.color],
    notificationOffset = this[TaskTable.notificationOffset],
    isCompleted = this[TaskTable.isCompleted],
    createdAt = this[TaskTable.createdAt],
    updatedAt = this[TaskTable.updatedAt],
)
