package com.studytracker.config

import com.studytracker.shared.error.ErrorBody
import com.studytracker.shared.error.ErrorResponse
import com.studytracker.shared.security.JwtService
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.config.ApplicationConfig
import io.ktor.server.response.respond

const val AuthJwt = "auth-jwt"

private const val INSECURE_DEFAULT_SECRET = "dev_secret_change_me_to_a_long_random_value"

fun jwtServiceFrom(config: ApplicationConfig): JwtService {
    val env = config.property("app.env").getString()
    val secret = config.property("jwt.secret").getString()
    check(env == "dev" || (secret != INSECURE_DEFAULT_SECRET && secret.length >= 32)) {
        "JWT_SECRET must be set to a strong value (>= 32 chars, not the bundled default) when APP_ENV != dev"
    }
    return JwtService(
        secret = secret,
        issuer = config.property("jwt.issuer").getString(),
        audience = config.property("jwt.audience").getString(),
        accessTtlSeconds = config.property("jwt.accessTtlSeconds").getString().toLong(),
    )
}

fun Application.configureSecurity(jwtService: JwtService) {
    install(Authentication) {
        jwt(AuthJwt) {
            realm = jwtService.realm
            verifier(jwtService.verifier())
            validate { credential ->
                if (credential.payload.subject != null) JWTPrincipal(credential.payload) else null
            }
            challenge { _, _ ->
                call.respond(
                    HttpStatusCode.Unauthorized,
                    ErrorResponse(ErrorBody("UNAUTHORIZED", "Authentication required")),
                )
            }
        }
    }
}
