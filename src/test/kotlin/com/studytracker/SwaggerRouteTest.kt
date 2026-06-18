package com.studytracker

import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.plugins.swagger.swaggerUI
import io.ktor.server.routing.routing
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SwaggerRouteTest {
    @Test
    fun `swagger UI is served and the spec is reachable`() = testApplication {
        application {
            routing {
                swaggerUI(path = "swagger", swaggerFile = "openapi/documentation.yaml")
            }
        }

        val ui = client.get("/swagger")
        assertEquals(HttpStatusCode.OK, ui.status)
        assertTrue(ui.bodyAsText().contains("swagger", ignoreCase = true), "expected Swagger UI html")

        val spec = client.get("/swagger/documentation.yaml")
        assertEquals(HttpStatusCode.OK, spec.status)
        assertTrue(spec.bodyAsText().contains("StudyTracker API"), "expected the OpenAPI spec body")
    }
}
