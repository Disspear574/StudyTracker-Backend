package com.studytracker

import com.studytracker.config.AuthJwt
import com.studytracker.config.configureSecurity
import com.studytracker.config.configureSerialization
import com.studytracker.config.configureStatusPages
import com.studytracker.shared.security.JwtService
import com.studytracker.shared.security.userId
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.server.testing.testApplication
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals

class SecurityRouteTest {
    private val jwt = JwtService("a-very-long-test-secret-0123456789", "test-iss", "test-aud", 3600)

    @Test
    fun `protected route rejects without token and accepts valid token`() = testApplication {
        application {
            configureSerialization()
            configureStatusPages()
            configureSecurity(jwt)
            routing {
                authenticate(AuthJwt) {
                    get("/protected") { call.respondText(call.userId().toString()) }
                }
            }
        }

        val unauthorized = client.get("/protected")
        assertEquals(HttpStatusCode.Unauthorized, unauthorized.status)

        val userId = UUID.randomUUID()
        val token = jwt.createAccessToken(userId).token
        val authorized = client.get("/protected") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        assertEquals(HttpStatusCode.OK, authorized.status)
        assertEquals(userId.toString(), authorized.bodyAsText())
    }
}
