package com.studytracker

import com.studytracker.config.AuthJwt
import com.studytracker.config.configureRateLimit
import com.studytracker.config.configureSecurity
import com.studytracker.config.configureSerialization
import com.studytracker.config.configureStatusPages
import com.studytracker.config.configureValidation
import com.studytracker.feature.auth.domain.usecase.LoginUseCase
import com.studytracker.feature.auth.domain.usecase.LogoutUseCase
import com.studytracker.feature.auth.domain.usecase.RefreshUseCase
import com.studytracker.feature.auth.domain.usecase.RegisterUseCase
import com.studytracker.feature.auth.infrastructure.security.AuthTokenIssuer
import com.studytracker.feature.auth.infrastructure.web.authRoutes
import com.studytracker.feature.auth.infrastructure.web.dto.AuthResponse
import com.studytracker.feature.auth.infrastructure.web.dto.DeviceDto
import com.studytracker.feature.auth.infrastructure.web.dto.RegisterRequest
import com.studytracker.feature.task.domain.usecase.CreateTaskUseCase
import com.studytracker.feature.task.domain.usecase.DeleteTaskUseCase
import com.studytracker.feature.task.domain.usecase.GetTaskUseCase
import com.studytracker.feature.task.domain.usecase.GetTasksByDateUseCase
import com.studytracker.feature.task.domain.usecase.ToggleCompleteUseCase
import com.studytracker.feature.task.domain.usecase.UpdateTaskUseCase
import com.studytracker.feature.task.infrastructure.web.dto.TaskListResponse
import com.studytracker.feature.task.infrastructure.web.dto.TaskRequest
import com.studytracker.feature.task.infrastructure.web.taskRoutes
import com.studytracker.feature.user.domain.usecase.ConfirmAvatarUseCase
import com.studytracker.feature.user.domain.usecase.DeleteAccountUseCase
import com.studytracker.feature.user.domain.usecase.GetMeUseCase
import com.studytracker.feature.user.domain.usecase.IssueAvatarUploadUrlUseCase
import com.studytracker.feature.user.domain.usecase.UpdateProfileUseCase
import com.studytracker.feature.user.infrastructure.web.userRoutes
import com.studytracker.shared.security.JwtService
import com.studytracker.support.FakeDeviceSessionRepository
import com.studytracker.support.FakeFileStorage
import com.studytracker.support.FakePasswordEncoder
import com.studytracker.support.FakeRefreshTokenRepository
import com.studytracker.support.FakeTaskRepository
import com.studytracker.support.FakeTransactor
import com.studytracker.support.FakeUserRepository
import com.studytracker.support.MutableTimeProvider
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.auth.authenticate
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertEquals

class ApiRoutesTest {
    private val time = MutableTimeProvider()
    private val jwt = JwtService("test-secret-0123456789abcdefghij", "test-iss", "test-aud", 3600)
    private val tokenIssuer = AuthTokenIssuer(jwt, 2_592_000, time)

    private val users = FakeUserRepository()
    private val refreshTokens = FakeRefreshTokenRepository()
    private val devices = FakeDeviceSessionRepository()
    private val tasks = FakeTaskRepository()
    private val storage = FakeFileStorage()
    private val encoder = FakePasswordEncoder()

    private fun Application.testModule() {
        configureSerialization()
        configureValidation()
        configureStatusPages()
        configureRateLimit()
        configureSecurity(jwt)
        routing {
            route("/api/v1") {
                authRoutes(
                    RegisterUseCase(users, refreshTokens, devices, encoder, tokenIssuer, time, FakeTransactor()),
                    LoginUseCase(users, refreshTokens, devices, encoder, tokenIssuer, time, FakeTransactor()),
                    RefreshUseCase(refreshTokens, tokenIssuer, time),
                    LogoutUseCase(refreshTokens, tokenIssuer, time),
                    storage,
                )
                authenticate(AuthJwt) {
                    userRoutes(
                        GetMeUseCase(users),
                        UpdateProfileUseCase(users, time),
                        DeleteAccountUseCase(users, storage),
                        IssueAvatarUploadUrlUseCase(storage, time),
                        ConfirmAvatarUseCase(users, storage, time),
                        storage,
                    )
                    taskRoutes(
                        CreateTaskUseCase(tasks, time),
                        GetTasksByDateUseCase(tasks),
                        GetTaskUseCase(tasks),
                        UpdateTaskUseCase(tasks, time),
                        ToggleCompleteUseCase(tasks, time),
                        DeleteTaskUseCase(tasks),
                    )
                }
            }
        }
    }

    private fun ApplicationTestBuilder.jsonClient() = createClient {
        install(ContentNegotiation) { json() }
    }

    private fun registerBody(email: String = "ivan@studytracker.app") = RegisterRequest(
        email = email,
        password = "password123",
        firstName = "Иван",
        lastName = "Иванов",
        middleName = "Иванович",
        device = DeviceDto("dev-1", "ANDROID", "15", "1.0.0", "Pixel"),
    )

    @Test
    fun `register then access protected routes with the issued token`() = testApplication {
        application { testModule() }
        val client = jsonClient()

        val registerResponse = client.post("/api/v1/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(registerBody())
        }
        assertEquals(HttpStatusCode.Created, registerResponse.status)
        val token = registerResponse.body<AuthResponse>().tokens.accessToken

        assertEquals(HttpStatusCode.Unauthorized, client.get("/api/v1/me").status)
        assertEquals(
            HttpStatusCode.OK,
            client.get("/api/v1/me") { header(HttpHeaders.Authorization, "Bearer $token") }.status,
        )

        val createResponse = client.post("/api/v1/tasks") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(
                TaskRequest(
                    title = "Экзамен по математике",
                    description = "Глава 5",
                    date = "2026-06-01",
                    startTime = "09:00",
                    color = "BLUE",
                    notificationOffset = "TEN_MINUTES",
                ),
            )
        }
        assertEquals(HttpStatusCode.Created, createResponse.status)

        val list = client.get("/api/v1/tasks?date=2026-06-01") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        assertEquals(HttpStatusCode.OK, list.status)
        assertEquals(1, list.body<TaskListResponse>().total)
    }

    @Test
    fun `register with invalid email returns 400 VALIDATION_ERROR`() = testApplication {
        application { testModule() }
        val client = jsonClient()

        val response = client.post("/api/v1/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(registerBody(email = "not-an-email"))
        }
        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertEquals(true, response.body<String>().contains("VALIDATION_ERROR"))
    }
}
