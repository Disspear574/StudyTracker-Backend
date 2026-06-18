package com.studytracker

import com.studytracker.config.buildMinioClient
import com.studytracker.config.configureDatabase
import com.studytracker.config.configureHttp
import com.studytracker.config.configureKoin
import com.studytracker.config.configureMonitoring
import com.studytracker.config.configureRateLimit
import com.studytracker.config.configureRouting
import com.studytracker.config.configureSecurity
import com.studytracker.config.configureSerialization
import com.studytracker.config.configureStatusPages
import com.studytracker.config.configureValidation
import com.studytracker.config.jwtServiceFrom
import com.studytracker.feature.user.infrastructure.storage.MinioFileStorage
import io.ktor.server.application.Application

fun Application.module() {
    val config = environment.config
    val jwtService = jwtServiceFrom(config)
    val fileStorage = MinioFileStorage(buildMinioClient(config), config.property("minio.avatarsBucket").getString())
        .also { it.ensureBucket() }

    configureKoin(jwtService, fileStorage)
    configureDatabase()
    configureMonitoring()
    configureSerialization()
    configureValidation()
    configureStatusPages()
    configureHttp()
    configureRateLimit()
    configureSecurity(jwtService)
    configureRouting()
}
