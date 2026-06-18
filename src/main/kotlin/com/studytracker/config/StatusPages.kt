package com.studytracker.config

import com.studytracker.shared.error.AppException
import com.studytracker.shared.error.ErrorBody
import com.studytracker.shared.error.ErrorResponse
import com.studytracker.shared.error.FieldErrorDto
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.application.log
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.requestvalidation.RequestValidationException
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond

fun Application.configureStatusPages() {
    install(StatusPages) {
        exception<RequestValidationException> { call, cause ->
            val details = cause.reasons.map { reason ->
                val separator = reason.indexOf(": ")
                if (separator > 0) {
                    FieldErrorDto(reason.substring(0, separator), reason.substring(separator + 2))
                } else {
                    FieldErrorDto("", reason)
                }
            }
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse(ErrorBody("VALIDATION_ERROR", "Validation failed", details)),
            )
        }

        exception<BadRequestException> { call, _ ->
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse(ErrorBody("VALIDATION_ERROR", "Malformed request body")),
            )
        }

        exception<AppException> { call, cause ->
            val (status, code) = when (cause) {
                is AppException.Validation -> HttpStatusCode.BadRequest to "VALIDATION_ERROR"
                is AppException.Unauthorized -> HttpStatusCode.Unauthorized to "UNAUTHORIZED"
                is AppException.Forbidden -> HttpStatusCode.Forbidden to "FORBIDDEN"
                is AppException.NotFound -> HttpStatusCode.NotFound to "NOT_FOUND"
                is AppException.Conflict -> HttpStatusCode.Conflict to "CONFLICT"
            }
            val details = (cause as? AppException.Validation)
                ?.details
                ?.map { FieldErrorDto(it.field, it.message) }
                .orEmpty()
            call.respond(status, ErrorResponse(ErrorBody(code, cause.message ?: code, details)))
        }

        exception<Throwable> { call, cause ->
            call.application.log.error("Unhandled exception", cause)
            call.respond(
                HttpStatusCode.InternalServerError,
                ErrorResponse(ErrorBody("INTERNAL", "Internal server error")),
            )
        }

        status(HttpStatusCode.TooManyRequests) { call, status ->
            call.respond(status, ErrorResponse(ErrorBody("RATE_LIMITED", "Too many requests")))
        }
    }
}
