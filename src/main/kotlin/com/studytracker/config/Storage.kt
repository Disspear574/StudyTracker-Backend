package com.studytracker.config

import io.ktor.server.config.ApplicationConfig
import io.minio.MinioClient

fun buildMinioClient(config: ApplicationConfig): MinioClient =
    MinioClient.builder()
        .endpoint(config.property("minio.endpoint").getString())
        .credentials(
            config.property("minio.accessKey").getString(),
            config.property("minio.secretKey").getString(),
        )
        .build()
