package com.studytracker.config.di

import com.studytracker.feature.auth.domain.port.RefreshTokenRepository
import com.studytracker.feature.auth.domain.usecase.LoginUseCase
import com.studytracker.feature.auth.domain.usecase.LogoutUseCase
import com.studytracker.feature.auth.domain.usecase.RefreshUseCase
import com.studytracker.feature.auth.domain.usecase.RegisterUseCase
import com.studytracker.feature.auth.infrastructure.persistence.ExposedRefreshTokenRepository
import com.studytracker.feature.stats.domain.port.DeviceSessionRepository
import com.studytracker.feature.stats.infrastructure.persistence.ExposedDeviceSessionRepository
import com.studytracker.feature.task.domain.port.TaskRepository
import com.studytracker.feature.task.domain.usecase.CreateTaskUseCase
import com.studytracker.feature.task.domain.usecase.DeleteTaskUseCase
import com.studytracker.feature.task.domain.usecase.GetTaskUseCase
import com.studytracker.feature.task.domain.usecase.GetTasksByDateUseCase
import com.studytracker.feature.task.domain.usecase.ToggleCompleteUseCase
import com.studytracker.feature.task.domain.usecase.UpdateTaskUseCase
import com.studytracker.feature.task.infrastructure.persistence.ExposedTaskRepository
import com.studytracker.feature.user.domain.port.UserRepository
import com.studytracker.feature.user.domain.usecase.ConfirmAvatarUseCase
import com.studytracker.feature.user.domain.usecase.DeleteAccountUseCase
import com.studytracker.feature.user.domain.usecase.GetMeUseCase
import com.studytracker.feature.user.domain.usecase.IssueAvatarUploadUrlUseCase
import com.studytracker.feature.user.domain.usecase.UpdateProfileUseCase
import com.studytracker.feature.user.infrastructure.persistence.ExposedUserRepository
import com.studytracker.shared.db.ExposedTransactor
import com.studytracker.shared.ports.SystemTimeProvider
import com.studytracker.shared.ports.TimeProvider
import com.studytracker.shared.ports.Transactor
import com.studytracker.shared.security.BcryptPasswordEncoder
import com.studytracker.shared.security.PasswordEncoder
import org.koin.dsl.module

val appModule = module {
    single<TimeProvider> { SystemTimeProvider() }
    single<PasswordEncoder> { BcryptPasswordEncoder() }
    single<Transactor> { ExposedTransactor() }

    single<UserRepository> { ExposedUserRepository() }
    single<RefreshTokenRepository> { ExposedRefreshTokenRepository() }
    single<TaskRepository> { ExposedTaskRepository() }
    single<DeviceSessionRepository> { ExposedDeviceSessionRepository() }

    single { RegisterUseCase(get(), get(), get(), get(), get(), get(), get()) }
    single { LoginUseCase(get(), get(), get(), get(), get(), get(), get()) }
    single { RefreshUseCase(get(), get(), get()) }
    single { LogoutUseCase(get(), get(), get()) }

    single { GetMeUseCase(get()) }
    single { UpdateProfileUseCase(get(), get()) }
    single { DeleteAccountUseCase(get(), get()) }
    single { IssueAvatarUploadUrlUseCase(get(), get()) }
    single { ConfirmAvatarUseCase(get(), get(), get()) }

    single { CreateTaskUseCase(get(), get()) }
    single { GetTasksByDateUseCase(get()) }
    single { GetTaskUseCase(get()) }
    single { UpdateTaskUseCase(get(), get()) }
    single { ToggleCompleteUseCase(get(), get()) }
    single { DeleteTaskUseCase(get()) }
}
