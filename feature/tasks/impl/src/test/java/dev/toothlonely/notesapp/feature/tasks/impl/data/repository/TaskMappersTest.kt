package dev.toothlonely.notesapp.feature.tasks.impl.data.repository

import dev.toothlonely.notesapp.core.data.database.model.TaskEntity
import dev.toothlonely.notesapp.feature.tasks.impl.domain.model.NewTask
import dev.toothlonely.notesapp.feature.tasks.impl.domain.model.Task
import org.junit.Assert.assertEquals
import org.junit.Test

class TaskMappersTest {
    @Test
    fun `entity maps to domain task`() {
        assertEquals(
            Task(
                id = 5,
                title = "Купить молоко",
                isCompleted = true,
                createdAtMillis = 123,
                updatedAtMillis = 456,
            ),
            TaskEntity(
                id = 5,
                title = "Купить молоко",
                isCompleted = true,
                createdAtMillis = 123,
                updatedAtMillis = 456,
            ).asDomainModel(),
        )
    }

    @Test
    fun `new task maps persistence metadata and starts active`() {
        assertEquals(
            TaskEntity(
                title = "Купить молоко",
                isCompleted = false,
                createdAtMillis = 456,
                updatedAtMillis = 456,
            ),
            NewTask("Купить молоко").asEntity(createdAtMillis = 456),
        )
    }
}
