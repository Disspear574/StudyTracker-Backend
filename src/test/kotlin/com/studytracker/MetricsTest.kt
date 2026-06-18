package com.studytracker

import com.studytracker.config.configureMonitoring
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MetricsTest {
    @Test
    fun `metrics endpoint exposes prometheus output`() = testApplication {
        application { configureMonitoring() }

        val response = client.get("/metrics")

        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains("jvm"), "expected JVM metrics in /metrics output")
    }
}
