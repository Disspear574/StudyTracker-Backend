package com.studytracker.shared.security

import com.studytracker.shared.error.AppException
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.principal
import io.ktor.server.auth.jwt.JWTPrincipal
import java.util.UUID

fun ApplicationCall.userId(): UUID {
    val principal = principal<JWTPrincipal>() ?: throw AppException.Unauthorized()
    val subject = principal.payload.subject ?: throw AppException.Unauthorized()
    return try {
        UUID.fromString(subject)
    } catch (_: IllegalArgumentException) {
        throw AppException.Unauthorized()
    }
}
