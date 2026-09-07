package dev.toothlonely.notesapp.feature.tasks.impl.presentation.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.toothlonely.notesapp.feature.tasks.impl.domain.model.NewTask
import dev.toothlonely.notesapp.feature.tasks.impl.domain.model.Task
import dev.toothlonely.notesapp.feature.tasks.impl.domain.repository.TasksRepository
import dev.toothlonely.notesapp.feature.tasks.impl.domain.usecase.TaskListOrderer
import dev.toothlonely.notesapp.feature.tasks.impl.domain.usecase.TaskTitleValidator
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TasksViewModel(
    private val tasksRepository: TasksRepository,
    private val taskTitleValidator: TaskTitleValidator,
    private val taskListOrderer: TaskListOrderer,
) : ViewModel() {
    private val _state = MutableStateFlow(TasksUiState())
    val state: StateFlow<TasksUiState> = _state.asStateFlow()

    private var observationJob: Job? = null
    private var pendingStatusUpdates: Map<Long, Boolean> = emptyMap()

    init {
        observeTasks()
    }

    fun retryLoading() {
        observeTasks()
    }

    fun startCreatingTask() {
        _state.update { state ->
            if (state.editor != null || !state.content.isLoaded()) {
                state
            } else {
                state.copy(editor = InlineTaskEditorUiState())
            }
        }
    }

    fun updateDraftTitle(title: String) {
        _state.update { state ->
            val editor = state.editor
            if (editor == null || editor.isSaving) {
                state
            } else {
                state.copy(
                    editor = editor.copy(
                        title = title,
                        error = null,
                    ),
                )
            }
        }
    }

    fun cancelTaskCreation() {
        _state.update { state ->
            if (state.editor?.isSaving == true) state else state.copy(editor = null)
        }
    }

    fun confirmTaskCreation() {
        val editor = _state.value.editor ?: return
        if (editor.isSaving) return

        val validatedTitle = taskTitleValidator.validate(editor.title)
        if (validatedTitle == null) {
            _state.update { state ->
                state.copy(editor = state.editor?.copy(error = InlineTaskEditorError.EmptyTitle))
            }
            return
        }

        _state.update { state ->
            state.copy(editor = state.editor?.copy(isSaving = true, error = null))
        }
        viewModelScope.launch {
            try {
                tasksRepository.createTask(NewTask(validatedTitle))
                _state.update { state -> state.copy(editor = null) }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Throwable) {
                _state.update { state ->
                    state.copy(
                        editor = state.editor?.copy(
                            isSaving = false,
                            error = InlineTaskEditorError.Storage,
                        ),
                    )
                }
            }
        }
    }

    fun toggleTaskStatus(taskId: Long) {
        val task = _state.value.content.tasksOrEmpty().find { task -> task.id == taskId }
            ?: return
        requestTaskStatusUpdate(
            taskId = taskId,
            previousCompleted = task.isCompleted,
            requestedCompleted = !task.isCompleted,
        )
    }

    fun retryStatusUpdate() {
        val failedUpdate = _state.value.statusUpdateError ?: return
        val task = _state.value.content.tasksOrEmpty()
            .find { task -> task.id == failedUpdate.taskId }
            ?: run {
                dismissStatusUpdateError()
                return
            }
        requestTaskStatusUpdate(
            taskId = task.id,
            previousCompleted = task.isCompleted,
            requestedCompleted = failedUpdate.requestedCompleted,
        )
    }

    fun dismissStatusUpdateError() {
        _state.update { state -> state.copy(statusUpdateError = null) }
    }

    private fun observeTasks() {
        observationJob?.cancel()
        pendingStatusUpdates = emptyMap()
        _state.update { state ->
            state.copy(
                content = TasksContentState.Loading,
                editor = null,
                pendingStatusTaskIds = emptySet(),
                statusUpdateError = null,
            )
        }
        observationJob = viewModelScope.launch {
            try {
                tasksRepository.observeTasks().collect { storedTasks ->
                    val storedTasksById = storedTasks.associateBy(Task::id)
                    pendingStatusUpdates = pendingStatusUpdates.filter { (taskId, requested) ->
                        storedTasksById[taskId]?.isCompleted?.let { stored ->
                            stored != requested
                        } == true
                    }
                    val visibleTasks = taskListOrderer.order(
                        storedTasks.map { task ->
                            pendingStatusUpdates[task.id]?.let { requested ->
                                task.copy(isCompleted = requested)
                            } ?: task
                        },
                    )
                    _state.update { state ->
                        state.copy(
                            content = visibleTasks.toContentState(),
                            pendingStatusTaskIds = pendingStatusUpdates.keys,
                        )
                    }
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Throwable) {
                pendingStatusUpdates = emptyMap()
                _state.update { state ->
                    state.copy(
                        content = TasksContentState.Error,
                        editor = null,
                        pendingStatusTaskIds = emptySet(),
                        statusUpdateError = null,
                    )
                }
            }
        }
    }

    private fun requestTaskStatusUpdate(
        taskId: Long,
        previousCompleted: Boolean,
        requestedCompleted: Boolean,
    ) {
        if (taskId in pendingStatusUpdates) return

        pendingStatusUpdates = pendingStatusUpdates + (taskId to requestedCompleted)
        _state.update { state ->
            state.copy(
                content = state.content.withTaskStatus(taskId, requestedCompleted),
                pendingStatusTaskIds = pendingStatusUpdates.keys,
                statusUpdateError = null,
            )
        }
        viewModelScope.launch {
            val updateSucceeded = try {
                tasksRepository.setTaskCompleted(taskId, requestedCompleted)
            } catch (error: CancellationException) {
                throw error
            } catch (error: Throwable) {
                false
            }
            if (!updateSucceeded && pendingStatusUpdates[taskId] == requestedCompleted) {
                pendingStatusUpdates = pendingStatusUpdates - taskId
                _state.update { state ->
                    state.copy(
                        content = state.content.withTaskStatus(taskId, previousCompleted),
                        pendingStatusTaskIds = pendingStatusUpdates.keys,
                        statusUpdateError = TaskStatusUpdateError(
                            taskId = taskId,
                            requestedCompleted = requestedCompleted,
                        ),
                    )
                }
            }
        }
    }

    private fun TasksContentState.withTaskStatus(
        taskId: Long,
        isCompleted: Boolean,
    ): TasksContentState = when (this) {
        is TasksContentState.Content -> TasksContentState.Content(
            tasks = taskListOrderer.order(
                tasks.map { task ->
                    if (task.id == taskId) task.copy(isCompleted = isCompleted) else task
                },
            ),
        )

        else -> this
    }

    private fun TasksContentState.isLoaded(): Boolean =
        this is TasksContentState.Empty || this is TasksContentState.Content

    private fun TasksContentState.tasksOrEmpty(): List<Task> =
        (this as? TasksContentState.Content)?.tasks.orEmpty()

    private fun List<Task>.toContentState(): TasksContentState =
        if (isEmpty()) TasksContentState.Empty else TasksContentState.Content(this)
}
