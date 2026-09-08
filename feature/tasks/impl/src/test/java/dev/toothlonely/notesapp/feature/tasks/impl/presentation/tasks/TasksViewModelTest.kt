package dev.toothlonely.notesapp.feature.tasks.impl.presentation.tasks

import dev.toothlonely.notesapp.core.domain.speech.SpeechRecognitionEvent
import dev.toothlonely.notesapp.core.domain.speech.SpeechRecognitionFailure
import dev.toothlonely.notesapp.feature.tasks.impl.domain.model.Task
import dev.toothlonely.notesapp.feature.tasks.impl.domain.model.TaskSortOrder
import dev.toothlonely.notesapp.feature.tasks.impl.domain.model.TaskStatusFilter
import dev.toothlonely.notesapp.feature.tasks.impl.domain.usecase.TaskListOrderer
import dev.toothlonely.notesapp.feature.tasks.impl.domain.usecase.TaskTitleValidator
import dev.toothlonely.notesapp.feature.tasks.impl.testutil.FakeTasksRepository
import dev.toothlonely.notesapp.feature.tasks.impl.testutil.FakeGigaChatRepository
import dev.toothlonely.notesapp.feature.tasks.impl.testutil.FakeSpeechRecognitionRepository
import dev.toothlonely.notesapp.feature.tasks.impl.testutil.MainDispatcherRule
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
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
    fun `observed tasks are ordered by creation time regardless of status`() = runTest {
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
            listOf(1L, 3L, 2L),
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
        viewModel.confirmTaskEditor()
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
        viewModel.confirmTaskEditor()
        viewModel.confirmTaskEditor()
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
        viewModel.confirmTaskEditor()
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
        viewModel.cancelTaskEditor()

        assertNull(viewModel.state.value.editor)
    }

    @Test
    fun `checkbox updates optimistically without moving task and remains persisted`() = runTest {
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
        assertEquals(listOf(1L, 2L), optimisticTasks.map(Task::id))
        assertTrue(optimisticTasks.first().isCompleted)
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

    @Test
    fun `draft query does not change results until explicit search and clear restores all`() =
        runTest {
            val repository = FakeTasksRepository().apply {
                tasks.value = listOf(
                    task(id = 1, title = "Купить молоко"),
                    task(id = 2, title = "Позвонить маме"),
                )
            }
            val viewModel = createViewModel(repository)
            runCurrent()

            viewModel.updateDraftQuery("молоко")
            assertEquals(
                listOf(2L, 1L),
                (viewModel.state.value.content as TasksContentState.Content).tasks.map(Task::id),
            )

            viewModel.applySearch()
            assertEquals(
                listOf(1L),
                (viewModel.state.value.content as TasksContentState.Content).tasks.map(Task::id),
            )

            viewModel.clearSearch()
            assertEquals("", viewModel.state.value.draftQuery)
            assertEquals("", viewModel.state.value.appliedQuery)
            assertEquals(
                listOf(2L, 1L),
                (viewModel.state.value.content as TasksContentState.Content).tasks.map(Task::id),
            )
        }

    @Test
    fun `search filter and oldest sort combine and expose search empty state`() = runTest {
        val repository = FakeTasksRepository().apply {
            tasks.value = listOf(
                task(
                    id = 1,
                    title = "Купить молоко",
                    isCompleted = true,
                    createdAtMillis = 400,
                    updatedAtMillis = 100,
                ),
                task(
                    id = 2,
                    title = "Купить хлеб",
                    createdAtMillis = 100,
                    updatedAtMillis = 400,
                ),
                task(
                    id = 3,
                    title = "Позвонить",
                    createdAtMillis = 300,
                    updatedAtMillis = 200,
                ),
                task(
                    id = 4,
                    title = "Купить билеты",
                    isCompleted = true,
                    createdAtMillis = 200,
                    updatedAtMillis = 300,
                ),
            )
        }
        val viewModel = createViewModel(repository)
        runCurrent()

        viewModel.updateDraftQuery("купить")
        viewModel.applySearch()
        viewModel.changeStatusFilter(TaskStatusFilter.Completed)
        viewModel.changeSortOrder(TaskSortOrder.OldestFirst)

        assertEquals(
            listOf(4L, 1L),
            (viewModel.state.value.content as TasksContentState.Content).tasks.map(Task::id),
        )

        viewModel.changeStatusFilter(TaskStatusFilter.Active)
        viewModel.updateDraftQuery("нет совпадений")
        viewModel.applySearch()
        assertEquals(TasksContentState.SearchEmpty, viewModel.state.value.content)

        viewModel.resetSearchAndFilter()
        assertEquals(TaskStatusFilter.All, viewModel.state.value.statusFilter)
        assertTrue(viewModel.state.value.content is TasksContentState.Content)
    }

    @Test
    fun `only one task edits inline and cancel restores original title`() = runTest {
        val repository = FakeTasksRepository().apply {
            tasks.value = listOf(
                task(id = 1, title = "Исходная"),
                task(id = 2, title = "Другая"),
            )
        }
        val viewModel = createViewModel(repository)
        runCurrent()

        viewModel.startEditingTask(1)
        viewModel.updateDraftTitle("Черновик")
        viewModel.startEditingTask(2)

        assertEquals(1L, viewModel.state.value.editor?.taskId)
        assertEquals("Исходная", viewModel.state.value.editor?.originalTitle)
        assertEquals("Черновик", viewModel.state.value.editor?.title)

        viewModel.cancelTaskEditor()
        assertNull(viewModel.state.value.editor)
        assertEquals("Исходная", repository.tasks.value.first { it.id == 1L }.title)
    }

    @Test
    fun `edit validates trims updates timestamp and closes after success`() = runTest {
        val repository = FakeTasksRepository().apply {
            tasks.value = listOf(task(id = 1, title = "Исходная", updatedAtMillis = 10))
            currentTimeMillis = 500
        }
        val viewModel = createViewModel(repository)
        runCurrent()

        viewModel.startEditingTask(1)
        viewModel.updateDraftTitle("   ")
        viewModel.confirmTaskEditor()
        assertEquals(InlineTaskEditorError.EmptyTitle, viewModel.state.value.editor?.error)

        viewModel.updateDraftTitle("  Обновлённая  ")
        viewModel.confirmTaskEditor()
        runCurrent()

        assertEquals(listOf(1L to "Обновлённая"), repository.titleUpdates)
        assertEquals(500, repository.tasks.value.single().updatedAtMillis)
        assertNull(viewModel.state.value.editor)
    }

    @Test
    fun `edit failure keeps original task and draft then retry succeeds`() = runTest {
        val repository = FakeTasksRepository().apply {
            tasks.value = listOf(task(id = 1, title = "Исходная"))
            updateFailure = IllegalStateException("Update failed")
        }
        val viewModel = createViewModel(repository)
        runCurrent()

        viewModel.startEditingTask(1)
        viewModel.updateDraftTitle("Новая")
        viewModel.confirmTaskEditor()
        runCurrent()

        assertEquals("Исходная", repository.tasks.value.single().title)
        assertEquals("Новая", viewModel.state.value.editor?.title)
        assertEquals(InlineTaskEditorError.Storage, viewModel.state.value.editor?.error)

        repository.updateFailure = null
        viewModel.confirmTaskEditor()
        runCurrent()
        assertEquals("Новая", repository.tasks.value.single().title)
        assertNull(viewModel.state.value.editor)
    }

    @Test
    fun `delete requires named confirmation and cancel preserves task`() = runTest {
        val repository = FakeTasksRepository().apply {
            tasks.value = listOf(task(id = 7, title = "Важная задача"))
        }
        val viewModel = createViewModel(repository)
        runCurrent()

        viewModel.requestTaskDeletion(7)

        assertEquals("Важная задача", viewModel.state.value.deleteConfirmation?.taskTitle)
        assertTrue(repository.deletedTaskIds.isEmpty())

        viewModel.cancelTaskDeletion()
        assertNull(viewModel.state.value.deleteConfirmation)
        assertEquals(listOf(7L), repository.tasks.value.map(Task::id))
    }

    @Test
    fun `confirmed delete removes task and emits success event`() = runTest {
        val repository = FakeTasksRepository().apply {
            tasks.value = listOf(task(id = 7, title = "Важная задача"))
        }
        val viewModel = createViewModel(repository)
        runCurrent()
        val event = async { viewModel.events.first() }

        viewModel.requestTaskDeletion(7)
        viewModel.confirmTaskDeletion()
        runCurrent()

        assertTrue(repository.tasks.value.isEmpty())
        assertEquals(TasksContentState.Empty, viewModel.state.value.content)
        assertNull(viewModel.state.value.deleteConfirmation)
        assertEquals(TasksEvent.TaskDeleted, event.await())
    }

    @Test
    fun `delete failure keeps task and retry removes it`() = runTest {
        val repository = FakeTasksRepository().apply {
            tasks.value = listOf(task(id = 7, title = "Важная задача"))
            deleteFailure = IllegalStateException("Delete failed")
        }
        val viewModel = createViewModel(repository)
        runCurrent()

        viewModel.requestTaskDeletion(7)
        viewModel.confirmTaskDeletion()
        runCurrent()

        assertEquals(listOf(7L), repository.tasks.value.map(Task::id))
        assertEquals(TaskDeleteError(7, "Важная задача"), viewModel.state.value.deleteError)
        assertNull(viewModel.state.value.deleteConfirmation)

        repository.deleteFailure = null
        viewModel.retryTaskDeletion()
        runCurrent()

        assertTrue(repository.tasks.value.isEmpty())
        assertNull(viewModel.state.value.deleteError)
        assertNull(viewModel.state.value.deleteConfirmation)
    }

    @Test
    fun `voice task waits for GigaChat and is saved exactly once`() = runTest {
        val gate = CompletableDeferred<Unit>()
        val repository = FakeTasksRepository()
        val speech = FakeSpeechRecognitionRepository()
        val gigaChat = FakeGigaChatRepository().apply {
            formulatedTask = "Купить молоко"
            formulationGate = gate
        }
        val viewModel = createViewModel(repository, speech, gigaChat)
        runCurrent()

        viewModel.startVoiceInput()
        viewModel.startVoiceInput()
        speech.emit(SpeechRecognitionEvent.Result("купи молоко вечером"))
        runCurrent()

        assertEquals(listOf("ru-RU"), speech.startedLocales)
        assertEquals(TasksVoiceInputUiState.GigaChatProcessing, viewModel.state.value.voiceInput)
        assertTrue(repository.createdTasks.isEmpty())

        speech.emit(SpeechRecognitionEvent.Result("дублирующий результат"))
        runCurrent()
        gate.complete(Unit)
        runCurrent()

        assertEquals(listOf("купи молоко вечером"), gigaChat.formulationRequests)
        assertEquals(listOf("Купить молоко"), repository.createdTasks.map { task -> task.title })
        assertEquals(TasksVoiceInputUiState.Idle, viewModel.state.value.voiceInput)
    }

    @Test
    fun `GigaChat failure keeps recognized text and retry does not duplicate task`() = runTest {
        val repository = FakeTasksRepository()
        val speech = FakeSpeechRecognitionRepository()
        val gigaChat = FakeGigaChatRepository().apply {
            formulationFailure = IllegalStateException("Network failed")
            formulatedTask = "Позвонить врачу"
        }
        val viewModel = createViewModel(repository, speech, gigaChat)
        runCurrent()
        viewModel.startVoiceInput()

        speech.emit(SpeechRecognitionEvent.Result("надо позвонить врачу"))
        runCurrent()

        assertEquals(
            TasksVoiceInputUiState.Error(
                failure = TasksVoiceFailure.GigaChat,
                recognizedText = "надо позвонить врачу",
            ),
            viewModel.state.value.voiceInput,
        )
        assertTrue(repository.createdTasks.isEmpty())

        gigaChat.formulationFailure = null
        viewModel.retryVoiceProcessing()
        runCurrent()

        assertEquals(2, gigaChat.formulationRequests.size)
        assertEquals(listOf("Позвонить врачу"), repository.createdTasks.map { task -> task.title })
        assertEquals(TasksVoiceInputUiState.Idle, viewModel.state.value.voiceInput)
    }

    @Test
    fun `storage failure retries formulated title without another GigaChat request`() = runTest {
        val repository = FakeTasksRepository().apply {
            createFailure = IllegalStateException("Database failed")
        }
        val speech = FakeSpeechRecognitionRepository()
        val gigaChat = FakeGigaChatRepository().apply {
            formulatedTask = "Отправить отчёт"
        }
        val viewModel = createViewModel(repository, speech, gigaChat)
        runCurrent()
        viewModel.startVoiceInput()

        speech.emit(SpeechRecognitionEvent.Result("отправь отчет"))
        runCurrent()

        assertEquals(
            TasksVoiceInputUiState.Error(
                failure = TasksVoiceFailure.Storage,
                formulatedTitle = "Отправить отчёт",
            ),
            viewModel.state.value.voiceInput,
        )

        repository.createFailure = null
        viewModel.retryVoiceProcessing()
        runCurrent()

        assertEquals(1, gigaChat.formulationRequests.size)
        assertEquals(listOf("Отправить отчёт"), repository.createdTasks.map { task -> task.title })
    }

    @Test
    fun `cancel during GigaChat processing prevents local task creation`() = runTest {
        val gate = CompletableDeferred<Unit>()
        val repository = FakeTasksRepository()
        val speech = FakeSpeechRecognitionRepository()
        val gigaChat = FakeGigaChatRepository().apply {
            formulatedTask = "Не сохранять"
            formulationGate = gate
        }
        val viewModel = createViewModel(repository, speech, gigaChat)
        runCurrent()
        viewModel.startVoiceInput()
        speech.emit(SpeechRecognitionEvent.Result("не сохраняй"))
        runCurrent()

        viewModel.cancelVoiceInput()
        gate.complete(Unit)
        runCurrent()

        assertEquals(TasksVoiceInputUiState.Idle, viewModel.state.value.voiceInput)
        assertTrue(repository.createdTasks.isEmpty())
    }

    @Test
    fun `speech failure does not call GigaChat and exposes retryable input state`() = runTest {
        val speech = FakeSpeechRecognitionRepository()
        val gigaChat = FakeGigaChatRepository()
        val viewModel = createViewModel(FakeTasksRepository(), speech, gigaChat)
        runCurrent()
        viewModel.startVoiceInput()

        speech.emit(SpeechRecognitionEvent.Error(SpeechRecognitionFailure.NoSpeech))
        runCurrent()

        assertEquals(
            TasksVoiceInputUiState.Error(
                failure = TasksVoiceFailure.Speech(SpeechRecognitionFailure.NoSpeech),
            ),
            viewModel.state.value.voiceInput,
        )
        assertTrue(gigaChat.formulationRequests.isEmpty())
    }

    private fun createViewModel(
        repository: FakeTasksRepository,
        speechRecognitionRepository: FakeSpeechRecognitionRepository =
            FakeSpeechRecognitionRepository(),
        gigaChatRepository: FakeGigaChatRepository = FakeGigaChatRepository(),
    ) = TasksViewModel(
        tasksRepository = repository,
        taskTitleValidator = TaskTitleValidator(),
        taskListOrderer = TaskListOrderer(),
        speechRecognitionRepository = speechRecognitionRepository,
        gigaChatRepository = gigaChatRepository,
    )

    private fun task(
        id: Long,
        title: String = "Задача $id",
        isCompleted: Boolean = false,
        createdAtMillis: Long = id,
        updatedAtMillis: Long = createdAtMillis,
    ) = Task(
        id = id,
        title = title,
        isCompleted = isCompleted,
        createdAtMillis = createdAtMillis,
        updatedAtMillis = updatedAtMillis,
    )
}
