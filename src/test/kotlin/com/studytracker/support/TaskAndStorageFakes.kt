package com.studytracker.support

import com.studytracker.feature.task.domain.model.Task
import com.studytracker.feature.task.domain.model.TaskContent
import com.studytracker.feature.task.domain.port.TaskRepository
import com.studytracker.shared.storage.FileStorage
import com.studytracker.shared.storage.StoredObject
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

class FakeFileStorage : FileStorage {
    val deleted = mutableListOf<String>()

    var nextStat: StoredObject? = StoredObject("image/png", 1024)

    override fun presignedPutUrl(key: String, ttlSeconds: Long): String = "https://minio.local/put/$key?ttl=$ttlSeconds"
    override fun presignedGetUrl(key: String, ttlSeconds: Long): String = "https://minio.local/get/$key?ttl=$ttlSeconds"
    override suspend fun delete(key: String) { deleted.add(key) }
    override suspend fun stat(key: String): StoredObject? = nextStat
}

class FakeTaskRepository : TaskRepository {
    val byId = mutableMapOf<UUID, Task>()
    private var seq = 0

    override suspend fun create(userId: UUID, content: TaskContent, now: LocalDateTime): Task {
        val id = UUID.nameUUIDFromBytes("task-${++seq}".toByteArray())
        val task = Task(
            id = id, userId = userId, title = content.title, description = content.description,
            date = content.date, startTime = content.startTime, intervalStart = content.intervalStart,
            intervalEnd = content.intervalEnd, color = content.color, notificationOffset = content.notificationOffset,
            isCompleted = false, createdAt = now, updatedAt = now,
        )
        byId[id] = task
        return task
    }

    override suspend fun findByIdForUser(id: UUID, userId: UUID): Task? =
        byId[id]?.takeIf { it.userId == userId }

    override suspend fun listForUserByDate(userId: UUID, date: LocalDate): List<Task> =
        byId.values.filter { it.userId == userId && it.date == date }.sortedBy { it.startTime }

    override suspend fun update(id: UUID, userId: UUID, content: TaskContent, now: LocalDateTime): Task? {
        val existing = findByIdForUser(id, userId) ?: return null
        val updated = existing.copy(
            title = content.title, description = content.description, date = content.date,
            startTime = content.startTime, intervalStart = content.intervalStart, intervalEnd = content.intervalEnd,
            color = content.color, notificationOffset = content.notificationOffset, updatedAt = now,
        )
        byId[id] = updated
        return updated
    }

    override suspend fun setCompleted(id: UUID, userId: UUID, completed: Boolean, now: LocalDateTime): Task? {
        val existing = findByIdForUser(id, userId) ?: return null
        val updated = existing.copy(isCompleted = completed, updatedAt = now)
        byId[id] = updated
        return updated
    }

    override suspend fun deleteForUser(id: UUID, userId: UUID): Boolean {
        if (findByIdForUser(id, userId) == null) return false
        byId.remove(id)
        return true
    }
}
