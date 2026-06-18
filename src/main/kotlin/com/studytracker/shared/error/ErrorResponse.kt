package com.studytracker.shared.error

import kotlinx.serialization.Serializable

@Serializable
data class ErrorResponse(val error: ErrorBody)

@Serializable
data class ErrorBody(
    val code: String,
    val message: String,
    val details: List<FieldErrorDto> = emptyList(),
)

@Serializable
data class FieldErrorDto(val field: String, val message: String)
