package dev.toothlonely.notesapp.feature.tasks.impl.data.repository

import dev.toothlonely.notesapp.core.data.database.TasksDao
import dev.toothlonely.notesapp.core.data.database.model.TaskEntity
import dev.toothlonely.notesapp.feature.tasks.impl.domain.model.NewTask
import dev.toothlonely.notesapp.feature.tasks.impl.domain.model.Task
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RoomTasksRepositoryTest {
    private val dao = FakeTasksDao()
    private var currentTimeMillis = 123L
    private val repository = RoomTasksRepository(
        tasksDao = dao,
        timeProvider = TaskTimeProvider { currentTimeMillis },
    )

    @Test
    fun `observed entities are mapped and updates remain reactive`() = runTest {
        dao.tasks.value = listOf(TaskEntity(1, "Первая", false, 100, 150))

        assertEquals(
            listOf(Task(1, "Первая", false, 100, 150)),
            repository.observeTasks().first(),
        )

        dao.tasks.value = listOf(TaskEntity(2, "Вторая", true, 200, 250))
        assertEquals(
            listOf(Task(2, "Вторая", true, 200, 250)),
            repository.observeTasks().first(),
        )
    }

    @Test
    fun `create supplies creation time and active status`() = runTest {
        repository.createTask(NewTask("Новая задача"))

        assertEquals(
            TaskEntity(
                title = "Новая задача",
                isCompleted = false,
                createdAtMillis = 123,
                updatedAtMillis = 123,
            ),
            dao.inserted.single(),
        )
    }

    @Test
    fun `status update is delegated and reports missing task`() = runTest {
        dao.tasks.value = listOf(TaskEntity(7, "Задача", false, 100))

        assertEquals(true, repository.setTaskCompleted(7, true))
        assertEquals(true, dao.tasks.value.single().isCompleted)
        assertEquals(false, repository.setTaskCompleted(404, true))
    }

    @Test
    fun `title update supplies update time and reports missing task`() = runTest {
        dao.tasks.value = listOf(TaskEntity(7, "Старое", false, 100))
        currentTimeMillis = 456

        assertEquals(true, repository.updateTaskTitle(7, "Новое"))
        assertEquals("Новое", dao.tasks.value.single().title)
        assertEquals(456, dao.tasks.value.single().updatedAtMillis)
        assertEquals(false, repository.updateTaskTitle(404, "Нет задачи"))
    }

    @Test
    fun `delete removes existing task and reports missing task`() = runTest {
        dao.tasks.value = listOf(TaskEntity(7, "Задача", false, 100))

        assertEquals(true, repository.deleteTask(7))
        assertTrue(dao.tasks.value.isEmpty())
        assertEquals(false, repository.deleteTask(404))
    }
}

private class FakeTasksDao : TasksDao {
    val tasks = MutableStateFlow<List<TaskEntity>>(emptyList())
    val inserted = mutableListOf<TaskEntity>()

    override fun observeTasks(): Flow<List<TaskEntity>> = tasks

    override suspend fun insert(task: TaskEntity): Long {
        inserted += task
        return inserted.size.toLong()
    }

    override suspend fun updateCompleted(taskId: Long, isCompleted: Boolean): Int {
        if (tasks.value.none { task -> task.id == taskId }) return 0
        tasks.value = tasks.value.map { task ->
            if (task.id == taskId) task.copy(isCompleted = isCompleted) else task
        }
        return 1
    }

    override suspend fun updateTitle(
        taskId: Long,
        title: String,
        updatedAtMillis: Long,
    ): Int {
        if (tasks.value.none { task -> task.id == taskId }) return 0
        tasks.value = tasks.value.map { task ->
            if (task.id == taskId) {
                task.copy(title = title, updatedAtMillis = updatedAtMillis)
            } else {
                task
            }
        }
        return 1
    }

    override suspend fun delete(taskId: Long): Int {
        if (tasks.value.none { task -> task.id == taskId }) return 0
        tasks.value = tasks.value.filterNot { task -> task.id == taskId }
        return 1
    }
}
