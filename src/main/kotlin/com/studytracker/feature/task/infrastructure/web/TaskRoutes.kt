package com.studytracker.feature.task.infrastructure.web

import com.studytracker.feature.task.domain.usecase.CreateTaskUseCase
import com.studytracker.feature.task.domain.usecase.DeleteTaskUseCase
import com.studytracker.feature.task.domain.usecase.GetTaskUseCase
import com.studytracker.feature.task.domain.usecase.GetTasksByDateUseCase
import com.studytracker.feature.task.domain.usecase.ToggleCompleteUseCase
import com.studytracker.feature.task.domain.usecase.UpdateTaskUseCase
import com.studytracker.feature.task.infrastructure.web.dto.CompleteRequest
import com.studytracker.feature.task.infrastructure.web.dto.TaskRequest
import com.studytracker.feature.task.infrastructure.web.dto.toContent
import com.studytracker.feature.task.infrastructure.web.dto.toResponse
import com.studytracker.shared.error.AppException
import com.studytracker.shared.security.userId
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import java.time.LocalDate
import java.time.format.DateTimeParseException
import java.util.UUID

fun Route.taskRoutes(
    create: CreateTaskUseCase,
    getByDate: GetTasksByDateUseCase,
    getOne: GetTaskUseCase,
    update: UpdateTaskUseCase,
    toggleComplete: ToggleCompleteUseCase,
    delete: DeleteTaskUseCase,
) {
    route("/tasks") {
        get {
            val dateParam = call.request.queryParameters["date"]
                ?: throw AppException.Validation("Query parameter 'date' is required (YYYY-MM-DD)")
            val date = try {
                LocalDate.parse(dateParam)
            } catch (_: DateTimeParseException) {
                throw AppException.Validation("Invalid date: $dateParam (expected YYYY-MM-DD)")
            }
            call.respond(getByDate(call.userId(), date).toResponse())
        }
        post {
            val request = call.receive<TaskRequest>()
            call.respond(HttpStatusCode.Created, create(call.userId(), request.toContent()).toResponse())
        }
        get("/{id}") {
            call.respond(getOne(call.userId(), call.taskId()).toResponse())
        }
        patch("/{id}") {
            val request = call.receive<TaskRequest>()
            call.respond(update(call.userId(), call.taskId(), request.toContent()).toResponse())
        }
        patch("/{id}/complete") {
            val request = call.receive<CompleteRequest>()
            call.respond(toggleComplete(call.userId(), call.taskId(), request.isCompleted).toResponse())
        }
        delete("/{id}") {
            delete(call.userId(), call.taskId())
            call.respond(HttpStatusCode.NoContent)
        }
    }
}

private fun ApplicationCall.taskId(): UUID {
    val raw = parameters["id"] ?: throw AppException.NotFound("Task not found")
    return try {
        UUID.fromString(raw)
    } catch (_: IllegalArgumentException) {
        throw AppException.NotFound("Task not found")
    }
}
