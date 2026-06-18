package com.studytracker.config

import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.metrics.micrometer.MicrometerMetrics
import io.ktor.server.plugins.callid.CallId
import io.ktor.server.plugins.callid.callIdMdc
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.micrometer.prometheusmetrics.PrometheusConfig
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry
import org.slf4j.event.Level
import java.util.UUID

fun Application.configureMonitoring() {
    val registry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)

    install(CallId) {
        header("X-Request-Id")
        generate { UUID.randomUUID().toString() }
        verify { it.isNotBlank() }
    }

    install(CallLogging) {
        level = Level.INFO
        callIdMdc("requestId")
        filter { call -> !call.request.local.uri.startsWith("/metrics") }
    }

    install(MicrometerMetrics) {
        this.registry = registry
    }

    routing {
        get("/metrics") {
            call.respond(registry.scrape())
        }
    }
}
