package com.studytracker.feature.task.domain.model

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.UUID

data class Task(
    val id: UUID,
    val userId: UUID,
    val title: String,
    val description: String?,
    val date: LocalDate,
    val startTime: LocalTime,
    val intervalStart: LocalTime?,
    val intervalEnd: LocalTime?,
    val color: TaskColor,
    val notificationOffset: NotificationOffset?,
    val isCompleted: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
)

data class TaskContent(
    val title: String,
    val description: String?,
    val date: LocalDate,
    val startTime: LocalTime,
    val intervalStart: LocalTime?,
    val intervalEnd: LocalTime?,
    val color: TaskColor,
    val notificationOffset: NotificationOffset?,
)
