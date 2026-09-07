package dev.toothlonely.notesapp.feature.tasks.impl.domain.usecase

import dev.toothlonely.notesapp.feature.tasks.impl.domain.model.Task
import org.junit.Assert.assertEquals
import org.junit.Test

class TaskListOrdererTest {
    private val orderer = TaskListOrderer()

    @Test
    fun `active tasks precede completed tasks and each group is newest first`() {
        val tasks = listOf(
            task(id = 1, isCompleted = true, createdAtMillis = 400),
            task(id = 2, isCompleted = false, createdAtMillis = 100),
            task(id = 3, isCompleted = false, createdAtMillis = 300),
            task(id = 4, isCompleted = true, createdAtMillis = 200),
        )

        assertEquals(listOf(3L, 2L, 1L, 4L), orderer.order(tasks).map(Task::id))
    }

    @Test
    fun `id is a stable newest-first tie breaker`() {
        val tasks = listOf(
            task(id = 1, isCompleted = false, createdAtMillis = 100),
            task(id = 3, isCompleted = false, createdAtMillis = 100),
            task(id = 2, isCompleted = false, createdAtMillis = 100),
        )

        assertEquals(listOf(3L, 2L, 1L), orderer.order(tasks).map(Task::id))
    }

    private fun task(
        id: Long,
        isCompleted: Boolean,
        createdAtMillis: Long,
    ) = Task(
        id = id,
        title = "Задача $id",
        isCompleted = isCompleted,
        createdAtMillis = createdAtMillis,
    )
}
