package com.studytracker.config

import com.studytracker.config.di.appModule
import com.studytracker.feature.auth.domain.port.TokenIssuer
import com.studytracker.feature.auth.infrastructure.security.AuthTokenIssuer
import com.studytracker.shared.security.JwtService
import com.studytracker.shared.storage.FileStorage
import io.ktor.server.application.Application
import io.ktor.server.application.install
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

fun Application.configureKoin(jwtService: JwtService, fileStorage: FileStorage) {
    val refreshTtlSeconds = environment.config.property("jwt.refreshTtlSeconds").getString().toLong()
    val runtimeModule = module {
        single<TokenIssuer> { AuthTokenIssuer(jwtService, refreshTtlSeconds, get()) }
        single<FileStorage> { fileStorage }
    }
    install(Koin) {
        slf4jLogger()
        modules(appModule, runtimeModule)
    }
}
