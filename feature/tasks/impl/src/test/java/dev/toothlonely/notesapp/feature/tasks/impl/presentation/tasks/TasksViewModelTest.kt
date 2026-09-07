package dev.toothlonely.notesapp.feature.tasks.impl.presentation.tasks

import dev.toothlonely.notesapp.feature.tasks.impl.domain.model.Task
import dev.toothlonely.notesapp.feature.tasks.impl.domain.usecase.TaskListOrderer
import dev.toothlonely.notesapp.feature.tasks.impl.domain.usecase.TaskTitleValidator
import dev.toothlonely.notesapp.feature.tasks.impl.testutil.FakeTasksRepository
import dev.toothlonely.notesapp.feature.tasks.impl.testutil.MainDispatcherRule
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TasksViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `initial state is loading then empty flow is shown`() = runTest {
        val viewModel = createViewModel(FakeTasksRepository())

        assertEquals(TasksUiState(), viewModel.state.value)
        runCurrent()
        assertEquals(TasksContentState.Empty, viewModel.state.value.content)
    }

    @Test
    fun `observed tasks are ordered with active tasks before completed tasks`() = runTest {
        val repository = FakeTasksRepository().apply {
            tasks.value = listOf(
                task(id = 1, isCompleted = true, createdAtMillis = 300),
                task(id = 2, isCompleted = false, createdAtMillis = 100),
                task(id = 3, isCompleted = false, createdAtMillis = 200),
            )
        }
        val viewModel = createViewModel(repository)

        runCurrent()

        assertEquals(
            listOf(3L, 2L, 1L),
            (viewModel.state.value.content as TasksContentState.Content).tasks.map(Task::id),
        )
    }

    @Test
    fun `flow failure shows storage error and retry subscribes again`() = runTest {
        val repository = FakeTasksRepository().apply {
            observeFailure = IllegalStateException("Database unavailable")
        }
        val viewModel = createViewModel(repository)

        runCurrent()
        assertEquals(TasksContentState.Error, viewModel.state.value.content)

        repository.observeFailure = null
        viewModel.retryLoading()
        runCurrent()

        assertEquals(TasksContentState.Empty, viewModel.state.value.content)
    }

    @Test
    fun `blank title stays inline and shows validation error`() = runTest {
        val repository = FakeTasksRepository()
        val viewModel = createViewModel(repository)
        runCurrent()

        viewModel.startCreatingTask()
        viewModel.updateDraftTitle("  \n ")
        viewModel.confirmTaskCreation()
        runCurrent()

        assertEquals(InlineTaskEditorError.EmptyTitle, viewModel.state.value.editor?.error)
        assertTrue(repository.createdTasks.isEmpty())
    }

    @Test
    fun `confirmed title is trimmed saved once and appears first`() = runTest {
        val gate = CompletableDeferred<Unit>()
        val repository = FakeTasksRepository().apply {
            tasks.value = listOf(task(id = 1, createdAtMillis = 10))
            currentTimeMillis = 20
            createGate = gate
        }
        val viewModel = createViewModel(repository)
        runCurrent()

        viewModel.startCreatingTask()
        viewModel.updateDraftTitle("  Купить молоко  ")
        viewModel.confirmTaskCreation()
        viewModel.confirmTaskCreation()
        runCurrent()

        assertTrue(viewModel.state.value.editor?.isSaving == true)
        gate.complete(Unit)
        runCurrent()

        assertEquals(listOf("Купить молоко"), repository.createdTasks.map { it.title })
        assertNull(viewModel.state.value.editor)
        assertEquals(
            listOf(100L, 1L),
            (viewModel.state.value.content as TasksContentState.Content).tasks.map(Task::id),
        )
    }

    @Test
    fun `create failure keeps draft and exposes inline storage error`() = runTest {
        val repository = FakeTasksRepository().apply {
            createFailure = IllegalStateException("Insert failed")
        }
        val viewModel = createViewModel(repository)
        runCurrent()

        viewModel.startCreatingTask()
        viewModel.updateDraftTitle("Важная задача")
        viewModel.confirmTaskCreation()
        runCurrent()

        assertEquals("Важная задача", viewModel.state.value.editor?.title)
        assertFalse(viewModel.state.value.editor?.isSaving == true)
        assertEquals(InlineTaskEditorError.Storage, viewModel.state.value.editor?.error)
    }

    @Test
    fun `cancel removes unsaved inline task`() = runTest {
        val viewModel = createViewModel(FakeTasksRepository())
        runCurrent()

        viewModel.startCreatingTask()
        viewModel.updateDraftTitle("Черновик")
        viewModel.cancelTaskCreation()

        assertNull(viewModel.state.value.editor)
    }

    @Test
    fun `checkbox updates optimistically moves task and remains persisted`() = runTest {
        val gate = CompletableDeferred<Unit>()
        val repository = FakeTasksRepository().apply {
            tasks.value = listOf(
                task(id = 1, isCompleted = false, createdAtMillis = 200),
                task(id = 2, isCompleted = false, createdAtMillis = 100),
            )
            statusGate = gate
        }
        val viewModel = createViewModel(repository)
        runCurrent()

        viewModel.toggleTaskStatus(1)
        runCurrent()

        val optimisticTasks =
            (viewModel.state.value.content as TasksContentState.Content).tasks
        assertEquals(listOf(2L, 1L), optimisticTasks.map(Task::id))
        assertTrue(optimisticTasks.last().isCompleted)
        assertEquals(setOf(1L), viewModel.state.value.pendingStatusTaskIds)

        gate.complete(Unit)
        runCurrent()

        assertEquals(listOf(1L to true), repository.statusUpdates)
        assertTrue(repository.tasks.value.first { it.id == 1L }.isCompleted)
        assertTrue(viewModel.state.value.pendingStatusTaskIds.isEmpty())
    }

    @Test
    fun `status failure rolls back and retry persists requested status`() = runTest {
        val repository = FakeTasksRepository().apply {
            tasks.value = listOf(task(id = 7, isCompleted = false, createdAtMillis = 100))
            statusFailure = IllegalStateException("Update failed")
        }
        val viewModel = createViewModel(repository)
        runCurrent()

        viewModel.toggleTaskStatus(7)
        runCurrent()

        val rolledBackTask =
            (viewModel.state.value.content as TasksContentState.Content).tasks.single()
        assertFalse(rolledBackTask.isCompleted)
        assertEquals(TaskStatusUpdateError(7, true), viewModel.state.value.statusUpdateError)

        repository.statusFailure = null
        viewModel.retryStatusUpdate()
        runCurrent()

        assertTrue(repository.tasks.value.single().isCompleted)
        assertNull(viewModel.state.value.statusUpdateError)
        assertTrue(viewModel.state.value.pendingStatusTaskIds.isEmpty())
    }

    private fun createViewModel(repository: FakeTasksRepository) = TasksViewModel(
        tasksRepository = repository,
        taskTitleValidator = TaskTitleValidator(),
        taskListOrderer = TaskListOrderer(),
    )

    private fun task(
        id: Long,
        isCompleted: Boolean = false,
        createdAtMillis: Long = id,
    ) = Task(
        id = id,
        title = "Задача $id",
        isCompleted = isCompleted,
        createdAtMillis = createdAtMillis,
    )
}
