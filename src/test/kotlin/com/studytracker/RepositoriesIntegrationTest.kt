package com.studytracker

import com.studytracker.feature.auth.infrastructure.persistence.ExposedRefreshTokenRepository
import com.studytracker.feature.auth.infrastructure.persistence.RefreshTokenTable
import com.studytracker.feature.stats.domain.model.DeviceInfo
import com.studytracker.feature.stats.domain.model.OsType
import com.studytracker.feature.stats.infrastructure.persistence.DeviceSessionTable
import com.studytracker.feature.stats.infrastructure.persistence.ExposedDeviceSessionRepository
import com.studytracker.feature.task.domain.model.NotificationOffset
import com.studytracker.feature.task.domain.model.TaskColor
import com.studytracker.feature.task.domain.model.TaskContent
import com.studytracker.feature.task.infrastructure.persistence.ExposedTaskRepository
import com.studytracker.feature.task.infrastructure.persistence.TaskTable
import com.studytracker.feature.user.domain.port.NewUser
import com.studytracker.feature.user.domain.port.ProfilePatch
import com.studytracker.feature.user.infrastructure.persistence.ExposedUserRepository
import com.studytracker.feature.user.infrastructure.persistence.UserTable
import com.studytracker.shared.db.ExposedTransactor
import com.studytracker.shared.error.AppException
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.deleteAll
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RepositoriesIntegrationTest {
    private val users = ExposedUserRepository()
    private val refresh = ExposedRefreshTokenRepository()
    private val tasks = ExposedTaskRepository()
    private val devices = ExposedDeviceSessionRepository()
    private val now = LocalDateTime.of(2026, 6, 1, 9, 0)
    private val day = LocalDate.of(2026, 6, 1)

    @BeforeTest
    fun setUp() {
        Database.connect("jdbc:h2:mem:repotest;DB_CLOSE_DELAY=-1;MODE=PostgreSQL", driver = "org.h2.Driver")
        transaction {
            SchemaUtils.create(UserTable, TaskTable, RefreshTokenTable, DeviceSessionTable)
            TaskTable.deleteAll()
            RefreshTokenTable.deleteAll()
            DeviceSessionTable.deleteAll()
            UserTable.deleteAll()
        }
    }

    @Test
    fun `user repo - create, find, credentials, profile, avatar, delete`() = runBlocking {
        val user = users.create(NewUser("a@b.c", "hash", "Иван", "Иванов", "Иванович"), now)

        assertEquals(user.id, users.findById(user.id)!!.id)
        assertTrue(users.existsByEmail("a@b.c"))
        assertEquals("hash", users.findCredentials("a@b.c")!!.passwordHash)
        assertEquals("Пётр", users.updateProfile(user.id, ProfilePatch(firstName = "Пётр"), now)!!.firstName)
        assertEquals("avatars/${user.id}/p", users.updateAvatarKey(user.id, "avatars/${user.id}/p", now)!!.avatarKey)
        assertTrue(users.deleteById(user.id))
        assertNull(users.findById(user.id))
    }

    @Test
    fun `refresh token repo - save, rotate with replacedBy, revoke-all`() = runBlocking {
        val user = users.create(NewUser("r@b.c", "h", "A", "B", null), now)
        refresh.save(user.id, "hash1", now.plusDays(30), now)

        val record = refresh.findByHash("hash1")!!
        assertNull(record.revokedAt)

        val newId = refresh.rotate(record.id, user.id, "hash2", now.plusDays(30), now)
        assertNotNull(newId)
        val old = refresh.findByHash("hash1")!!
        assertNotNull(old.revokedAt)
        assertEquals(newId, old.replacedBy)
        assertNull(refresh.findByHash("hash2")!!.revokedAt)

        assertNull(refresh.rotate(record.id, user.id, "hash3", now.plusDays(30), now))

        refresh.revokeAllForUser(user.id, now)
        assertTrue(refresh.findByHash("hash2")!!.revokedAt != null)
    }

    @Test
    fun `task repo - create, ordered list, owner scope, update, complete, delete`() = runBlocking {
        val owner = users.create(NewUser("o@b.c", "h", "A", "B", null), now).id
        val other = users.create(NewUser("x@b.c", "h", "C", "D", null), now).id

        tasks.create(owner, content("late", LocalTime.of(18, 0)), now)
        val early = tasks.create(owner, content("early", LocalTime.of(9, 0)), now)
        tasks.create(other, content("theirs", LocalTime.of(10, 0)), now)

        val list = tasks.listForUserByDate(owner, day)
        assertEquals(2, list.size)
        assertEquals("early", list.first().title)

        assertNull(tasks.findByIdForUser(early.id, other))
        assertEquals("renamed", tasks.update(early.id, owner, content("renamed", LocalTime.of(9, 0)), now)!!.title)
        assertTrue(tasks.setCompleted(early.id, owner, true, now)!!.isCompleted)
        assertFalse(tasks.deleteForUser(early.id, other))
        assertTrue(tasks.deleteForUser(early.id, owner))
    }

    @Test
    fun `device session repo - upsert keeps a single row per device`() = runBlocking {
        val user = users.create(NewUser("d@b.c", "h", "A", "B", null), now).id
        devices.record(user, DeviceInfo("dev-1", OsType.ANDROID, "15", "1.0.0", "Pixel"), now)
        devices.record(user, DeviceInfo("dev-1", OsType.ANDROID, "16", "1.1.0", "Pixel"), now.plusDays(1))

        val count = transaction {
            DeviceSessionTable.selectAll().where { DeviceSessionTable.userId eq user }.count()
        }
        assertEquals(1, count)
    }

    @Test
    fun `transactor rolls back nested repository writes when the block fails`() = runBlocking {
        val transactor = ExposedTransactor()
        assertFailsWith<IllegalStateException> {
            transactor.transaction {
                users.create(NewUser("tx@b.c", "h", "A", "B", null), now)
                error("boom after the write")
            }
        }
        assertFalse(users.existsByEmail("tx@b.c"))
    }

    @Test
    fun `duplicate email surfaces as Conflict, not a raw SQL exception`() = runBlocking {
        users.create(NewUser("dup@b.c", "h", "A", "B", null), now)
        assertFailsWith<AppException.Conflict> {
            users.create(NewUser("dup@b.c", "h2", "C", "D", null), now)
        }
        Unit
    }

    private fun content(title: String, time: LocalTime) =
        TaskContent(title, "описание", day, time, null, null, TaskColor.BLUE, NotificationOffset.TEN_MINUTES)
}
