package com.studytracker.shared.error

data class FieldError(val field: String, val message: String)

sealed class AppException(message: String) : RuntimeException(message) {
    class Validation(message: String, val details: List<FieldError> = emptyList()) : AppException(message)
    class Unauthorized(message: String = "Unauthorized") : AppException(message)
    class Forbidden(message: String = "Forbidden") : AppException(message)
    class NotFound(message: String = "Not found") : AppException(message)
    class Conflict(message: String) : AppException(message)
}
