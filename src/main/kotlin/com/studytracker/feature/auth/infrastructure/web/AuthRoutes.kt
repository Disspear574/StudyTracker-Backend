package com.studytracker.feature.auth.infrastructure.web

import com.studytracker.config.AuthRateLimit
import com.studytracker.feature.auth.domain.usecase.LoginUseCase
import com.studytracker.feature.auth.domain.usecase.LogoutUseCase
import com.studytracker.feature.auth.domain.usecase.RefreshUseCase
import com.studytracker.feature.auth.domain.usecase.RegisterUseCase
import com.studytracker.feature.auth.infrastructure.web.dto.ForgotPasswordRequest
import com.studytracker.feature.auth.infrastructure.web.dto.LoginRequest
import com.studytracker.feature.auth.infrastructure.web.dto.LogoutRequest
import com.studytracker.feature.auth.infrastructure.web.dto.RefreshRequest
import com.studytracker.feature.auth.infrastructure.web.dto.RegisterRequest
import com.studytracker.feature.auth.infrastructure.web.dto.toCommand
import com.studytracker.feature.auth.infrastructure.web.dto.toResponse
import com.studytracker.shared.storage.FileStorage
import io.ktor.http.HttpStatusCode
import io.ktor.server.plugins.ratelimit.rateLimit
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route

fun Route.authRoutes(
    register: RegisterUseCase,
    login: LoginUseCase,
    refresh: RefreshUseCase,
    logout: LogoutUseCase,
    fileStorage: FileStorage,
) {
    route("/auth") {
        rateLimit(AuthRateLimit) {
            post("/register") {
                val request = call.receive<RegisterRequest>()
                call.respond(HttpStatusCode.Created, register(request.toCommand()).toResponse(fileStorage))
            }
            post("/login") {
                val request = call.receive<LoginRequest>()
                call.respond(HttpStatusCode.OK, login(request.toCommand()).toResponse(fileStorage))
            }
            post("/refresh") {
                val request = call.receive<RefreshRequest>()
                call.respond(HttpStatusCode.OK, refresh(request.refreshToken).toResponse())
            }
            post("/logout") {
                val request = call.receive<LogoutRequest>()
                logout(request.refreshToken)
                call.respond(HttpStatusCode.NoContent)
            }
            post("/forgot-password") {
                call.receive<ForgotPasswordRequest>()
                call.respond(HttpStatusCode.OK)
            }
        }
    }
}
