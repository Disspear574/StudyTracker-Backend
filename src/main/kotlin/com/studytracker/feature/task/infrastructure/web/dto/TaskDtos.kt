package com.studytracker.feature.task.infrastructure.web.dto

import com.studytracker.feature.task.domain.model.NotificationOffset
import com.studytracker.feature.task.domain.model.Task
import com.studytracker.feature.task.domain.model.TaskColor
import com.studytracker.feature.task.domain.model.TaskContent
import com.studytracker.feature.task.domain.model.TaskListResult
import com.studytracker.shared.error.AppException
import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeParseException

@Serializable
data class TaskRequest(
    val title: String,
    val description: String? = null,
    val date: String,
    val startTime: String,
    val intervalStart: String? = null,
    val intervalEnd: String? = null,
    val color: String,
    val notificationOffset: String? = null,
)

@Serializable
data class CompleteRequest(val isCompleted: Boolean)

@Serializable
data class TaskResponse(
    val id: String,
    val title: String,
    val description: String? = null,
    val date: String,
    val startTime: String,
    val intervalStart: String? = null,
    val intervalEnd: String? = null,
    val color: String,
    val notificationOffset: String? = null,
    val isCompleted: Boolean,
    val createdAt: String,
    val updatedAt: String,
)

@Serializable
data class TaskListResponse(val items: List<TaskResponse>, val completed: Int, val total: Int)

private fun parseDate(value: String): LocalDate = try {
    LocalDate.parse(value)
} catch (_: DateTimeParseException) {
    throw AppException.Validation("Invalid date: $value (expected YYYY-MM-DD)")
}

private fun parseTime(value: String): LocalTime = try {
    LocalTime.parse(value)
} catch (_: DateTimeParseException) {
    throw AppException.Validation("Invalid time: $value (expected HH:mm)")
}

private fun parseColor(value: String): TaskColor = try {
    TaskColor.valueOf(value.uppercase())
} catch (_: IllegalArgumentException) {
    throw AppException.Validation("Invalid color: $value")
}

private fun parseOffset(value: String?): NotificationOffset? = value?.let {
    try {
        NotificationOffset.valueOf(it.uppercase())
    } catch (_: IllegalArgumentException) {
        throw AppException.Validation("Invalid notificationOffset: $value")
    }
}

fun TaskRequest.toContent(): TaskContent = TaskContent(
    title = title,
    description = description,
    date = parseDate(date),
    startTime = parseTime(startTime),
    intervalStart = intervalStart?.let(::parseTime),
    intervalEnd = intervalEnd?.let(::parseTime),
    color = parseColor(color),
    notificationOffset = parseOffset(notificationOffset),
)

fun Task.toResponse(): TaskResponse = TaskResponse(
    id = id.toString(),
    title = title,
    description = description,
    date = date.toString(),
    startTime = startTime.toString(),
    intervalStart = intervalStart?.toString(),
    intervalEnd = intervalEnd?.toString(),
    color = color.name,
    notificationOffset = notificationOffset?.name,
    isCompleted = isCompleted,
    createdAt = createdAt.toString(),
    updatedAt = updatedAt.toString(),
)

fun TaskListResult.toResponse(): TaskListResponse =
    TaskListResponse(items.map { it.toResponse() }, completed, total)
