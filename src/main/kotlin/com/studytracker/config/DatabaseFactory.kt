package com.studytracker.config

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.Application
import io.ktor.server.config.ApplicationConfig
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.v1.jdbc.Database
import javax.sql.DataSource

fun Application.configureDatabase() {
    val dataSource = createDataSource(environment.config)
    runMigrations(dataSource)
    Database.connect(dataSource)
}

fun createDataSource(config: ApplicationConfig): HikariDataSource {
    val hikari = HikariConfig().apply {
        jdbcUrl = config.property("db.url").getString()
        username = config.property("db.user").getString()
        password = config.property("db.password").getString()
        maximumPoolSize = config.property("db.poolSize").getString().toInt()
        driverClassName = "org.postgresql.Driver"
        isAutoCommit = false
        connectionTimeout = 10_000
        validate()
    }
    return HikariDataSource(hikari)
}

private fun runMigrations(dataSource: DataSource) {
    Flyway.configure()
        .dataSource(dataSource)
        .locations("classpath:db/migration")
        .load()
        .migrate()
}
