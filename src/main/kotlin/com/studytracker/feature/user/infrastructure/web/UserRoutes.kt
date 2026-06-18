package com.studytracker.feature.user.infrastructure.web

import com.studytracker.feature.user.domain.usecase.ConfirmAvatarUseCase
import com.studytracker.feature.user.domain.usecase.DeleteAccountUseCase
import com.studytracker.feature.user.domain.usecase.GetMeUseCase
import com.studytracker.feature.user.domain.usecase.IssueAvatarUploadUrlUseCase
import com.studytracker.feature.user.domain.usecase.UpdateProfileUseCase
import com.studytracker.feature.user.infrastructure.web.dto.AvatarUploadUrlRequest
import com.studytracker.feature.user.infrastructure.web.dto.ConfirmAvatarRequest
import com.studytracker.feature.user.infrastructure.web.dto.UpdateProfileRequest
import com.studytracker.feature.user.infrastructure.web.dto.toPatch
import com.studytracker.feature.user.infrastructure.web.dto.toResponse
import com.studytracker.shared.security.userId
import com.studytracker.shared.storage.FileStorage
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route

fun Route.userRoutes(
    getMe: GetMeUseCase,
    updateProfile: UpdateProfileUseCase,
    deleteAccount: DeleteAccountUseCase,
    issueAvatarUploadUrl: IssueAvatarUploadUrlUseCase,
    confirmAvatar: ConfirmAvatarUseCase,
    fileStorage: FileStorage,
) {
    route("/me") {
        get {
            call.respond(getMe(call.userId()).toResponse(fileStorage))
        }
        patch {
            val request = call.receive<UpdateProfileRequest>()
            call.respond(updateProfile(call.userId(), request.toPatch()).toResponse(fileStorage))
        }
        delete {
            deleteAccount(call.userId())
            call.respond(HttpStatusCode.NoContent)
        }
        post("/avatar/upload-url") {
            val request = call.receive<AvatarUploadUrlRequest>()
            call.respond(issueAvatarUploadUrl(call.userId(), request.contentType).toResponse())
        }
        put("/avatar") {
            val request = call.receive<ConfirmAvatarRequest>()
            call.respond(confirmAvatar(call.userId(), request.objectKey).toResponse(fileStorage))
        }
    }
}
