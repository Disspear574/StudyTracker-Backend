package com.studytracker

import com.studytracker.feature.stats.domain.model.OsType
import com.studytracker.feature.stats.infrastructure.persistence.DeviceSessionTable
import com.studytracker.feature.auth.infrastructure.persistence.RefreshTokenTable
import com.studytracker.feature.task.domain.model.TaskColor
import com.studytracker.feature.task.infrastructure.persistence.TaskTable
import com.studytracker.feature.user.infrastructure.persistence.UserTable
import com.studytracker.shared.ports.UuidV7
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class DatabaseSchemaTest {

    @BeforeTest
    fun setUp() {
        Database.connect(
            url = "jdbc:h2:mem:studytracker;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
            driver = "org.h2.Driver",
        )
    }

    @Test
    fun `schema creates and round-trips on H2`() {
        transaction {
            SchemaUtils.create(UserTable, TaskTable, RefreshTokenTable, DeviceSessionTable)

            val ownerId = UuidV7.next()
            UserTable.insert {
                it[id] = ownerId
                it[email] = "owner@studytracker.app"
                it[passwordHash] = "hash"
                it[firstName] = "Иван"
                it[lastName] = "Иванов"
                it[createdAt] = LocalDateTime.now()
                it[updatedAt] = LocalDateTime.now()
            }

            TaskTable.insert {
                it[id] = UuidV7.next()
                it[userId] = ownerId
                it[title] = "Экзамен по математике"
                it[taskDate] = LocalDate.now()
                it[startTime] = LocalTime.of(9, 0)
                it[color] = TaskColor.BLUE
                it[isCompleted] = false
                it[createdAt] = LocalDateTime.now()
                it[updatedAt] = LocalDateTime.now()
            }

            DeviceSessionTable.insert {
                it[id] = UuidV7.next()
                it[userId] = ownerId
                it[deviceUuid] = "device-1"
                it[os] = OsType.ANDROID
                it[osVersion] = "15"
                it[appVersion] = "1.0.0"
                it[createdAt] = LocalDateTime.now()
                it[lastSeenAt] = LocalDateTime.now()
            }

            assertEquals(1, UserTable.selectAll().count())
            assertEquals(1, TaskTable.selectAll().count())
            assertEquals(1, DeviceSessionTable.selectAll().count())
        }
    }
}
