package dev.toothlonely.notesapp.feature.tasks.impl.presentation.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.toothlonely.notesapp.core.domain.repository.GigaChatRepository
import dev.toothlonely.notesapp.core.domain.speech.SpeechRecognitionEvent
import dev.toothlonely.notesapp.core.domain.speech.SpeechRecognitionRepository
import dev.toothlonely.notesapp.feature.tasks.impl.domain.model.NewTask
import dev.toothlonely.notesapp.feature.tasks.impl.domain.model.Task
import dev.toothlonely.notesapp.feature.tasks.impl.domain.model.TaskSortOrder
import dev.toothlonely.notesapp.feature.tasks.impl.domain.model.TaskStatusFilter
import dev.toothlonely.notesapp.feature.tasks.impl.domain.repository.TasksRepository
import dev.toothlonely.notesapp.feature.tasks.impl.domain.usecase.TaskListOrderer
import dev.toothlonely.notesapp.feature.tasks.impl.domain.usecase.TaskTitleValidator
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TasksViewModel(
    private val tasksRepository: TasksRepository,
    private val taskTitleValidator: TaskTitleValidator,
    private val taskListOrderer: TaskListOrderer,
    private val speechRecognitionRepository: SpeechRecognitionRepository,
    private val gigaChatRepository: GigaChatRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(TasksUiState())
    val state: StateFlow<TasksUiState> = _state.asStateFlow()

    private val eventChannel = Channel<TasksEvent>(capacity = Channel.BUFFERED)
    val events: Flow<TasksEvent> = eventChannel.receiveAsFlow()

    private var observationJob: Job? = null
    private var voiceProcessingJob: Job? = null
    private var voiceTimerJob: Job? = null
    private var allTasks: List<Task> = emptyList()
    private var pendingStatusUpdates: Map<Long, Boolean> = emptyMap()

    init {
        observeSpeechRecognition()
        observeTasks()
    }

    fun retryLoading() {
        observeTasks()
    }

    fun updateDraftQuery(query: String) {
        _state.update { state -> state.copy(draftQuery = query) }
    }

    fun applySearch() {
        _state.update { state -> state.copy(appliedQuery = state.draftQuery.trim()) }
        refreshContent()
    }

    fun clearSearch() {
        _state.update { state -> state.copy(draftQuery = "", appliedQuery = "") }
        refreshContent()
    }

    fun changeStatusFilter(statusFilter: TaskStatusFilter) {
        _state.update { state -> state.copy(statusFilter = statusFilter) }
        refreshContent()
    }

    fun changeSortOrder(sortOrder: TaskSortOrder) {
        _state.update { state -> state.copy(sortOrder = sortOrder) }
        refreshContent()
    }

    fun resetSearchAndFilter() {
        _state.update { state ->
            state.copy(
                draftQuery = "",
                appliedQuery = "",
                statusFilter = TaskStatusFilter.All,
            )
        }
        refreshContent()
    }

    fun startCreatingTask() {
        _state.update { state ->
            if (state.editor != null || state.voiceInput.isBusy || !state.content.isLoaded()) {
                state
            } else {
                state.copy(editor = InlineTaskEditorUiState())
            }
        }
    }

    fun startVoiceInput() {
        val current = _state.value
        if (
            !current.content.isLoaded() ||
            current.editor != null ||
            current.deleteConfirmation != null ||
            current.voiceInput.isBusy
        ) {
            return
        }
        _state.value = current.copy(
            voiceInput = TasksVoiceInputUiState.Recording(),
        )
        startVoiceTimer()
        speechRecognitionRepository.start(VOICE_LOCALE_TAG)
    }

    fun stopVoiceInput() {
        val current = _state.value
        if (current.voiceInput !is TasksVoiceInputUiState.Recording) return
        stopVoiceTimer()
        _state.value = current.copy(voiceInput = TasksVoiceInputUiState.SpeechProcessing)
        speechRecognitionRepository.stop()
    }

    fun cancelVoiceInput() {
        if (!_state.value.voiceInput.isBusy) return
        stopVoiceTimer()
        voiceProcessingJob?.cancel()
        voiceProcessingJob = null
        speechRecognitionRepository.cancel()
        _state.update { state -> state.copy(voiceInput = TasksVoiceInputUiState.Idle) }
    }

    fun onMicrophonePermissionDenied(canRequestAgain: Boolean) {
        val current = _state.value
        if (current.editor != null || current.voiceInput.isBusy) return
        _state.value = current.copy(
            voiceInput = TasksVoiceInputUiState.PermissionDenied(canRequestAgain),
        )
    }

    fun dismissVoiceInputMessage() {
        _state.update { state ->
            if (state.voiceInput.isBusy) state
            else state.copy(voiceInput = TasksVoiceInputUiState.Idle)
        }
    }

    fun retryVoiceProcessing() {
        val error = _state.value.voiceInput as? TasksVoiceInputUiState.Error ?: return
        when (error.failure) {
            is TasksVoiceFailure.Speech -> Unit
            TasksVoiceFailure.GigaChat -> error.recognizedText?.let(::formulateAndSaveVoiceTask)
            TasksVoiceFailure.Storage -> error.formulatedTitle?.let(::saveFormulatedVoiceTask)
        }
    }

    fun startEditingTask(taskId: Long) {
        val task = allTasks.find { task -> task.id == taskId } ?: return
        _state.update { state ->
            if (
                state.editor != null ||
                state.deleteConfirmation != null ||
                state.voiceInput.isBusy
            ) {
                state
            } else {
                state.copy(
                    editor = InlineTaskEditorUiState(
                        taskId = task.id,
                        originalTitle = task.title,
                        title = task.title,
                    ),
                )
            }
        }
    }

    fun updateDraftTitle(title: String) {
        _state.update { state ->
            val editor = state.editor
            if (editor == null || editor.isSaving) {
                state
            } else {
                state.copy(editor = editor.copy(title = title, error = null))
            }
        }
    }

    fun cancelTaskEditor() {
        _state.update { state ->
            if (state.editor?.isSaving == true) state else state.copy(editor = null)
        }
    }

    fun confirmTaskEditor() {
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
            val succeeded = try {
                editor.taskId?.let { taskId ->
                    tasksRepository.updateTaskTitle(taskId, validatedTitle)
                } ?: run {
                    tasksRepository.createTask(NewTask(validatedTitle))
                    true
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Throwable) {
                false
            }
            if (succeeded) {
                editor.taskId?.let { taskId ->
                    allTasks = allTasks.map { task ->
                        if (task.id == taskId) task.copy(title = validatedTitle) else task
                    }
                    refreshContent()
                }
                _state.update { state -> state.copy(editor = null) }
            } else {
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

    fun requestTaskDeletion(taskId: Long) {
        val task = allTasks.find { task -> task.id == taskId } ?: return
        _state.update { state ->
            if (
                state.deleteConfirmation != null ||
                state.editor != null ||
                state.voiceInput.isBusy
            ) {
                state
            } else {
                state.copy(
                    deleteConfirmation = DeleteTaskConfirmationUiState(
                        taskId = task.id,
                        taskTitle = task.title,
                    ),
                    deleteError = null,
                )
            }
        }
    }

    fun cancelTaskDeletion() {
        _state.update { state ->
            if (state.deleteConfirmation?.isDeleting == true) {
                state
            } else {
                state.copy(deleteConfirmation = null)
            }
        }
    }

    fun confirmTaskDeletion() {
        val confirmation = _state.value.deleteConfirmation ?: return
        if (confirmation.isDeleting) return
        _state.update { state ->
            state.copy(
                deleteConfirmation = state.deleteConfirmation?.copy(isDeleting = true),
                deleteError = null,
            )
        }
        deleteTask(confirmation.taskId, confirmation.taskTitle)
    }

    fun retryTaskDeletion() {
        val failure = _state.value.deleteError ?: return
        _state.update { state ->
            state.copy(
                deleteConfirmation = DeleteTaskConfirmationUiState(
                    taskId = failure.taskId,
                    taskTitle = failure.taskTitle,
                    isDeleting = true,
                ),
                deleteError = null,
            )
        }
        deleteTask(failure.taskId, failure.taskTitle)
    }

    fun dismissTaskDeletionError() {
        _state.update { state -> state.copy(deleteError = null) }
    }

    fun toggleTaskStatus(taskId: Long) {
        if (_state.value.voiceInput.isBusy) return
        val task = allTasks.find { task -> task.id == taskId } ?: return
        requestTaskStatusUpdate(
            taskId = taskId,
            previousCompleted = task.isCompleted,
            requestedCompleted = !task.isCompleted,
        )
    }

    fun retryStatusUpdate() {
        val failedUpdate = _state.value.statusUpdateError ?: return
        val task = allTasks.find { task -> task.id == failedUpdate.taskId } ?: run {
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
                deleteConfirmation = null,
                deleteError = null,
            )
        }
        observationJob = viewModelScope.launch {
            try {
                tasksRepository.observeTasks().collect { storedTasks ->
                    val storedTasksById = storedTasks.associateBy(Task::id)
                    pendingStatusUpdates = pendingStatusUpdates.filter { (taskId, requested) ->
                        storedTasksById[taskId]?.isCompleted?.let { stored -> stored != requested } ==
                            true
                    }
                    allTasks = storedTasks.map { task ->
                        pendingStatusUpdates[task.id]?.let { requested ->
                            task.copy(isCompleted = requested)
                        } ?: task
                    }
                    _state.update { state ->
                        state.copy(
                            content = processedContent(state),
                            pendingStatusTaskIds = pendingStatusUpdates.keys,
                            editor = state.editor?.takeIf { editor ->
                                editor.taskId == null || storedTasksById.containsKey(editor.taskId)
                            },
                            deleteConfirmation = state.deleteConfirmation?.takeIf { confirmation ->
                                storedTasksById.containsKey(confirmation.taskId)
                            },
                        )
                    }
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Throwable) {
                allTasks = emptyList()
                pendingStatusUpdates = emptyMap()
                _state.update { state ->
                    state.copy(
                        content = TasksContentState.Error,
                        editor = null,
                        pendingStatusTaskIds = emptySet(),
                        statusUpdateError = null,
                        deleteConfirmation = null,
                        deleteError = null,
                    )
                }
            }
        }
    }

    private fun observeSpeechRecognition() {
        viewModelScope.launch {
            speechRecognitionRepository.events.collect(::handleSpeechRecognitionEvent)
        }
    }

    private fun handleSpeechRecognitionEvent(event: SpeechRecognitionEvent) {
        val current = _state.value
        if (!current.voiceInput.isBusy && event !is SpeechRecognitionEvent.Listening) return
        when (event) {
            SpeechRecognitionEvent.Listening -> Unit
            SpeechRecognitionEvent.Processing -> {
                stopVoiceTimer()
                _state.value = current.copy(
                    voiceInput = TasksVoiceInputUiState.SpeechProcessing,
                )
            }
            is SpeechRecognitionEvent.Result -> {
                stopVoiceTimer()
                formulateAndSaveVoiceTask(event.text)
            }
            is SpeechRecognitionEvent.Error -> {
                stopVoiceTimer()
                _state.value = current.copy(
                    voiceInput = TasksVoiceInputUiState.Error(
                        failure = TasksVoiceFailure.Speech(event.failure),
                    ),
                )
            }
            SpeechRecognitionEvent.Canceled -> {
                stopVoiceTimer()
                _state.value = current.copy(voiceInput = TasksVoiceInputUiState.Idle)
            }
        }
    }

    private fun formulateAndSaveVoiceTask(recognizedText: String) {
        if (voiceProcessingJob?.isActive == true) return
        val normalizedText = recognizedText.trim()
        if (normalizedText.isEmpty()) return
        _state.update { state ->
            state.copy(voiceInput = TasksVoiceInputUiState.GigaChatProcessing)
        }
        voiceProcessingJob = viewModelScope.launch {
            val title = try {
                gigaChatRepository.formulateTask(normalizedText)
            } catch (error: CancellationException) {
                throw error
            } catch (_: Throwable) {
                _state.update { state ->
                    state.copy(
                        voiceInput = TasksVoiceInputUiState.Error(
                            failure = TasksVoiceFailure.GigaChat,
                            recognizedText = normalizedText,
                        ),
                    )
                }
                return@launch
            }
            saveVoiceTaskInCurrentJob(title)
        }
    }

    private fun saveFormulatedVoiceTask(formulatedTitle: String) {
        if (voiceProcessingJob?.isActive == true) return
        _state.update { state ->
            state.copy(voiceInput = TasksVoiceInputUiState.GigaChatProcessing)
        }
        voiceProcessingJob = viewModelScope.launch {
            saveVoiceTaskInCurrentJob(formulatedTitle)
        }
    }

    private suspend fun saveVoiceTaskInCurrentJob(formulatedTitle: String) {
        try {
            tasksRepository.createTask(NewTask(formulatedTitle))
            _state.update { state ->
                state.copy(voiceInput = TasksVoiceInputUiState.Idle)
            }
        } catch (error: CancellationException) {
            throw error
        } catch (_: Throwable) {
            _state.update { state ->
                state.copy(
                    voiceInput = TasksVoiceInputUiState.Error(
                        failure = TasksVoiceFailure.Storage,
                        formulatedTitle = formulatedTitle,
                    ),
                )
            }
        }
    }

    private fun startVoiceTimer() {
        voiceTimerJob?.cancel()
        voiceTimerJob = viewModelScope.launch {
            while (true) {
                delay(VOICE_TIMER_TICK_MILLIS)
                _state.update { state ->
                    val recording = state.voiceInput as? TasksVoiceInputUiState.Recording
                        ?: return@update state
                    state.copy(
                        voiceInput = recording.copy(
                            durationSeconds = recording.durationSeconds + 1,
                        ),
                    )
                }
            }
        }
    }

    private fun stopVoiceTimer() {
        voiceTimerJob?.cancel()
        voiceTimerJob = null
    }

    private fun deleteTask(taskId: Long, taskTitle: String) {
        viewModelScope.launch {
            val succeeded = try {
                tasksRepository.deleteTask(taskId)
            } catch (error: CancellationException) {
                throw error
            } catch (error: Throwable) {
                false
            }
            if (succeeded) {
                allTasks = allTasks.filterNot { task -> task.id == taskId }
                refreshContent()
                _state.update { state -> state.copy(deleteConfirmation = null) }
                eventChannel.send(TasksEvent.TaskDeleted)
            } else {
                _state.update { state ->
                    state.copy(
                        deleteConfirmation = null,
                        deleteError = TaskDeleteError(taskId, taskTitle),
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
        allTasks = allTasks.map { task ->
            if (task.id == taskId) task.copy(isCompleted = requestedCompleted) else task
        }
        _state.update { state ->
            state.copy(
                content = processedContent(state),
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
                allTasks = allTasks.map { task ->
                    if (task.id == taskId) task.copy(isCompleted = previousCompleted) else task
                }
                _state.update { state ->
                    state.copy(
                        content = processedContent(state),
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

    private fun refreshContent() {
        _state.update { state -> state.copy(content = processedContent(state)) }
    }

    private fun processedContent(state: TasksUiState): TasksContentState {
        if (allTasks.isEmpty()) return TasksContentState.Empty
        val visibleTasks = taskListOrderer.process(
            tasks = allTasks,
            appliedQuery = state.appliedQuery,
            statusFilter = state.statusFilter,
            sortOrder = state.sortOrder,
        )
        return if (visibleTasks.isEmpty()) {
            TasksContentState.SearchEmpty
        } else {
            TasksContentState.Content(visibleTasks)
        }
    }

    private fun TasksContentState.isLoaded(): Boolean =
        this is TasksContentState.Empty ||
            this is TasksContentState.SearchEmpty ||
            this is TasksContentState.Content

    override fun onCleared() {
        stopVoiceTimer()
        voiceProcessingJob?.cancel()
        speechRecognitionRepository.close()
        super.onCleared()
    }

    private companion object {
        const val VOICE_LOCALE_TAG = "ru-RU"
        const val VOICE_TIMER_TICK_MILLIS = 1_000L
    }
}
