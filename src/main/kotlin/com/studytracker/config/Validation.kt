package com.studytracker.config

import com.studytracker.feature.auth.infrastructure.web.dto.LoginRequest
import com.studytracker.feature.auth.infrastructure.web.dto.RegisterRequest
import com.studytracker.feature.task.infrastructure.web.dto.TaskRequest
import com.studytracker.feature.user.infrastructure.web.dto.UpdateProfileRequest
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.requestvalidation.RequestValidation
import io.ktor.server.plugins.requestvalidation.ValidationResult

private val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

fun Application.configureValidation() {
    install(RequestValidation) {
        validate<RegisterRequest> { req ->
            val errors = buildList {
                if (!EMAIL_REGEX.matches(req.email)) add("email: invalid email format")
                if (req.password.length < 8) add("password: must be at least 8 characters")
                if (req.firstName.isBlank()) add("firstName: must not be blank")
                if (req.lastName.isBlank()) add("lastName: must not be blank")
            }
            if (errors.isEmpty()) ValidationResult.Valid else ValidationResult.Invalid(errors)
        }
        validate<LoginRequest> { req ->
            if (req.email.isBlank() || req.password.isBlank()) {
                ValidationResult.Invalid("email and password are required")
            } else {
                ValidationResult.Valid
            }
        }
        validate<TaskRequest> { req ->
            val errors = buildList {
                if (req.title.isBlank()) add("title: must not be blank")
                if (req.title.length > 200) add("title: must be at most 200 characters")
            }
            if (errors.isEmpty()) ValidationResult.Valid else ValidationResult.Invalid(errors)
        }
        validate<UpdateProfileRequest> { req ->
            val errors = buildList {
                if (req.email != null && !EMAIL_REGEX.matches(req.email)) add("email: invalid email format")
                if (req.firstName != null && req.firstName.isBlank()) add("firstName: must not be blank")
                if (req.lastName != null && req.lastName.isBlank()) add("lastName: must not be blank")
            }
            if (errors.isEmpty()) ValidationResult.Valid else ValidationResult.Invalid(errors)
        }
    }
}
