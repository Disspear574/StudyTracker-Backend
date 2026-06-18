package com.studytracker.config

import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.ratelimit.RateLimit
import io.ktor.server.plugins.ratelimit.RateLimitName
import kotlin.time.Duration.Companion.seconds

val AuthRateLimit = RateLimitName("auth")

fun Application.configureRateLimit() {
    install(RateLimit) {
        register(AuthRateLimit) {
            rateLimiter(limit = 10, refillPeriod = 60.seconds)
        }
    }
}
