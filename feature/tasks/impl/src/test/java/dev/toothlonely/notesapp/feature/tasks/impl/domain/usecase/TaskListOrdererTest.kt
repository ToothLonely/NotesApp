package dev.toothlonely.notesapp.feature.tasks.impl.domain.usecase

import dev.toothlonely.notesapp.feature.tasks.impl.domain.model.Task
import dev.toothlonely.notesapp.feature.tasks.impl.domain.model.TaskSortOrder
import dev.toothlonely.notesapp.feature.tasks.impl.domain.model.TaskStatusFilter
import org.junit.Assert.assertEquals
import org.junit.Test

class TaskListOrdererTest {
    private val orderer = TaskListOrderer()
    private val tasks = listOf(
        task(
            id = 1,
            title = "Купить молоко",
            isCompleted = true,
            createdAtMillis = 100,
            updatedAtMillis = 400,
        ),
        task(
            id = 2,
            title = "Купить хлеб",
            createdAtMillis = 400,
            updatedAtMillis = 100,
        ),
        task(
            id = 3,
            title = "Позвонить маме",
            createdAtMillis = 300,
            updatedAtMillis = 300,
        ),
        task(
            id = 4,
            title = "Купить билеты",
            isCompleted = true,
            createdAtMillis = 200,
            updatedAtMillis = 200,
        ),
    )

    @Test
    fun `newest sort uses creation time and ignores status and update time`() {
        assertEquals(
            listOf(2L, 3L, 4L, 1L),
            process(sortOrder = TaskSortOrder.NewestFirst).map(Task::id),
        )
    }

    @Test
    fun `oldest sort uses creation time and ignores status and update time`() {
        assertEquals(
            listOf(1L, 4L, 3L, 2L),
            process(sortOrder = TaskSortOrder.OldestFirst).map(Task::id),
        )
    }

    @Test
    fun `search is case insensitive trimmed and limited to title`() {
        assertEquals(
            listOf(2L, 4L, 1L),
            process(query = "  КУПИТЬ  ").map(Task::id),
        )
    }

    @Test
    fun `search filter and creation sort matrix composes`() {
        val expectations = mapOf(
            Triple(TaskStatusFilter.All, TaskSortOrder.NewestFirst, "Купить") to
                listOf(2L, 4L, 1L),
            Triple(TaskStatusFilter.All, TaskSortOrder.OldestFirst, "Купить") to
                listOf(1L, 4L, 2L),
            Triple(TaskStatusFilter.Active, TaskSortOrder.NewestFirst, "Купить") to
                listOf(2L),
            Triple(TaskStatusFilter.Completed, TaskSortOrder.NewestFirst, "Купить") to
                listOf(4L, 1L),
            Triple(TaskStatusFilter.Completed, TaskSortOrder.OldestFirst, "Купить") to
                listOf(1L, 4L),
        )

        expectations.forEach { (criteria, expectedIds) ->
            assertEquals(
                expectedIds,
                process(
                    query = criteria.third,
                    filter = criteria.first,
                    sortOrder = criteria.second,
                ).map(Task::id),
            )
        }
    }

    @Test
    fun `id resolves equal timestamps in selected direction`() {
        val tiedTasks = listOf(
            task(id = 1, title = "Первая", createdAtMillis = 100),
            task(id = 3, title = "Третья", createdAtMillis = 100),
            task(id = 2, title = "Вторая", createdAtMillis = 100),
        )

        assertEquals(
            listOf(3L, 2L, 1L),
            orderer.process(
                tasks = tiedTasks,
                appliedQuery = "",
                statusFilter = TaskStatusFilter.All,
                sortOrder = TaskSortOrder.NewestFirst,
            ).map(Task::id),
        )
        assertEquals(
            listOf(1L, 2L, 3L),
            orderer.process(
                tasks = tiedTasks,
                appliedQuery = "",
                statusFilter = TaskStatusFilter.All,
                sortOrder = TaskSortOrder.OldestFirst,
            ).map(Task::id),
        )
    }

    private fun process(
        query: String = "",
        filter: TaskStatusFilter = TaskStatusFilter.All,
        sortOrder: TaskSortOrder = TaskSortOrder.NewestFirst,
    ): List<Task> = orderer.process(
        tasks = tasks,
        appliedQuery = query,
        statusFilter = filter,
        sortOrder = sortOrder,
    )

    private fun task(
        id: Long,
        title: String,
        isCompleted: Boolean = false,
        createdAtMillis: Long,
        updatedAtMillis: Long = createdAtMillis,
    ) = Task(
        id = id,
        title = title,
        isCompleted = isCompleted,
        createdAtMillis = createdAtMillis,
        updatedAtMillis = updatedAtMillis,
    )
}
