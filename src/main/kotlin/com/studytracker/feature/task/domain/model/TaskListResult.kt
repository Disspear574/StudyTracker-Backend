package com.studytracker.feature.task.domain.model

data class TaskListResult(
    val items: List<Task>,
    val completed: Int,
    val total: Int,
)
