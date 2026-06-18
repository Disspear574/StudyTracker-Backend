package com.studytracker.feature.task.infrastructure.persistence

import com.studytracker.feature.task.domain.model.NotificationOffset
import com.studytracker.feature.task.domain.model.TaskColor
import com.studytracker.feature.user.infrastructure.persistence.UserTable
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.java.UUIDTable
import org.jetbrains.exposed.v1.javatime.date
import org.jetbrains.exposed.v1.javatime.datetime
import org.jetbrains.exposed.v1.javatime.time

object TaskTable : UUIDTable("tasks") {
    val userId = reference("user_id", UserTable, onDelete = ReferenceOption.CASCADE)
    val title = varchar("title", 200)
    val description = text("description").nullable()
    val taskDate = date("task_date")
    val startTime = time("start_time")
    val intervalStart = time("interval_start").nullable()
    val intervalEnd = time("interval_end").nullable()
    val color = enumerationByName("color", 20, TaskColor::class)
    val notificationOffset = enumerationByName("notification_offset", 20, NotificationOffset::class).nullable()
    val isCompleted = bool("is_completed").default(false)
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")

    init {
        index(false, userId, taskDate)
    }
}
