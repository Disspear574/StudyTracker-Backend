package com.studytracker

import com.studytracker.feature.task.domain.model.NotificationOffset
import com.studytracker.feature.task.domain.model.TaskColor
import com.studytracker.feature.task.domain.model.TaskContent
import com.studytracker.feature.task.domain.usecase.CreateTaskUseCase
import com.studytracker.feature.task.domain.usecase.DeleteTaskUseCase
import com.studytracker.feature.task.domain.usecase.GetTaskUseCase
import com.studytracker.feature.task.domain.usecase.GetTasksByDateUseCase
import com.studytracker.feature.task.domain.usecase.ToggleCompleteUseCase
import com.studytracker.feature.task.domain.usecase.UpdateTaskUseCase
import com.studytracker.shared.error.AppException
import com.studytracker.support.FakeTaskRepository
import com.studytracker.support.MutableTimeProvider
import kotlinx.coroutines.runBlocking
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class TaskUseCasesTest {
    private val time = MutableTimeProvider()
    private val tasks = FakeTaskRepository()

    private val create = CreateTaskUseCase(tasks, time)
    private val getByDate = GetTasksByDateUseCase(tasks)
    private val getOne = GetTaskUseCase(tasks)
    private val update = UpdateTaskUseCase(tasks, time)
    private val toggle = ToggleCompleteUseCase(tasks, time)
    private val delete = DeleteTaskUseCase(tasks)

    private val userA = UUID.randomUUID()
    private val userB = UUID.randomUUID()
    private val day = LocalDate.of(2026, 6, 1)

    private fun content(title: String = "Экзамен") =
        TaskContent(title, "описание", day, LocalTime.of(9, 0), null, null, TaskColor.BLUE, NotificationOffset.TEN_MINUTES)

    @Test
    fun `create returns a task owned by the user`() = runBlocking {
        val task = create(userA, content())
        assertEquals(userA, task.userId)
        assertEquals("Экзамен", task.title)
        assertEquals(false, task.isCompleted)
    }

    @Test
    fun `getByDate returns items with completed and total counts`() = runBlocking {
        val t1 = create(userA, content("A"))
        create(userA, content("B"))
        toggle(userA, t1.id, true)

        val result = getByDate(userA, day)

        assertEquals(2, result.total)
        assertEquals(1, result.completed)
        assertEquals(2, result.items.size)
    }

    @Test
    fun `getByDate returns only the owner's tasks`() = runBlocking {
        create(userA, content("mine"))
        create(userB, content("theirs"))
        val result = getByDate(userA, day)
        assertEquals(1, result.total)
        assertTrue(result.items.all { it.userId == userA })
    }

    @Test
    fun `getOne returns the task for its owner`() = runBlocking {
        val task = create(userA, content())
        assertEquals(task.id, getOne(userA, task.id).id)
    }

    @Test
    fun `getOne for another user's task throws NotFound`() = runBlocking {
        val task = create(userA, content())
        assertFailsWith<AppException.NotFound> { getOne(userB, task.id) }
        Unit
    }

    @Test
    fun `update changes the task content for its owner`() = runBlocking {
        val task = create(userA, content("old"))
        val updated = update(userA, task.id, content("new"))
        assertEquals("new", updated.title)
    }

    @Test
    fun `update of another user's task throws NotFound`() = runBlocking {
        val task = create(userA, content())
        assertFailsWith<AppException.NotFound> { update(userB, task.id, content("hack")) }
        Unit
    }

    @Test
    fun `toggle complete flips the flag`() = runBlocking {
        val task = create(userA, content())
        assertTrue(toggle(userA, task.id, true).isCompleted)
    }

    @Test
    fun `delete removes the owner's task`() = runBlocking {
        val task = create(userA, content())
        delete(userA, task.id)
        assertFailsWith<AppException.NotFound> { getOne(userA, task.id) }
        Unit
    }

    @Test
    fun `delete of another user's task throws NotFound`() = runBlocking {
        val task = create(userA, content())
        assertFailsWith<AppException.NotFound> { delete(userB, task.id) }
        Unit
    }
}
