package com.studytracker.config

import com.studytracker.feature.auth.domain.usecase.LoginUseCase
import com.studytracker.feature.auth.domain.usecase.LogoutUseCase
import com.studytracker.feature.auth.domain.usecase.RefreshUseCase
import com.studytracker.feature.auth.domain.usecase.RegisterUseCase
import com.studytracker.feature.auth.infrastructure.web.authRoutes
import com.studytracker.feature.health.healthRoutes
import com.studytracker.feature.task.domain.usecase.CreateTaskUseCase
import com.studytracker.feature.task.domain.usecase.DeleteTaskUseCase
import com.studytracker.feature.task.domain.usecase.GetTaskUseCase
import com.studytracker.feature.task.domain.usecase.GetTasksByDateUseCase
import com.studytracker.feature.task.domain.usecase.ToggleCompleteUseCase
import com.studytracker.feature.task.domain.usecase.UpdateTaskUseCase
import com.studytracker.feature.task.infrastructure.web.taskRoutes
import com.studytracker.feature.user.domain.usecase.ConfirmAvatarUseCase
import com.studytracker.feature.user.domain.usecase.DeleteAccountUseCase
import com.studytracker.feature.user.domain.usecase.GetMeUseCase
import com.studytracker.feature.user.domain.usecase.IssueAvatarUploadUrlUseCase
import com.studytracker.feature.user.domain.usecase.UpdateProfileUseCase
import com.studytracker.feature.user.infrastructure.web.userRoutes
import com.studytracker.shared.storage.FileStorage
import io.ktor.server.application.Application
import io.ktor.server.auth.authenticate
import io.ktor.server.plugins.swagger.swaggerUI
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import org.koin.ktor.ext.inject

fun Application.configureRouting() {
    val register by inject<RegisterUseCase>()
    val login by inject<LoginUseCase>()
    val refresh by inject<RefreshUseCase>()
    val logout by inject<LogoutUseCase>()
    val getMe by inject<GetMeUseCase>()
    val updateProfile by inject<UpdateProfileUseCase>()
    val deleteAccount by inject<DeleteAccountUseCase>()
    val issueAvatarUploadUrl by inject<IssueAvatarUploadUrlUseCase>()
    val confirmAvatar by inject<ConfirmAvatarUseCase>()
    val createTask by inject<CreateTaskUseCase>()
    val getTasksByDate by inject<GetTasksByDateUseCase>()
    val getTask by inject<GetTaskUseCase>()
    val updateTask by inject<UpdateTaskUseCase>()
    val toggleComplete by inject<ToggleCompleteUseCase>()
    val deleteTask by inject<DeleteTaskUseCase>()
    val fileStorage by inject<FileStorage>()

    routing {
        swaggerUI(path = "swagger", swaggerFile = "openapi/documentation.yaml")
        healthRoutes()
        route("/api/v1") {
            authRoutes(register, login, refresh, logout, fileStorage)
            authenticate(AuthJwt) {
                userRoutes(getMe, updateProfile, deleteAccount, issueAvatarUploadUrl, confirmAvatar, fileStorage)
                taskRoutes(createTask, getTasksByDate, getTask, updateTask, toggleComplete, deleteTask)
            }
        }
    }
}
