package dev.toothlonely.notesapp.feature.tasks.impl.presentation.tasks

import dev.toothlonely.notesapp.feature.tasks.impl.domain.model.Task

data class TasksUiState(
    val content: TasksContentState = TasksContentState.Loading,
    val editor: InlineTaskEditorUiState? = null,
    val pendingStatusTaskIds: Set<Long> = emptySet(),
    val statusUpdateError: TaskStatusUpdateError? = null,
)

sealed interface TasksContentState {
    data object Loading : TasksContentState

    data object Empty : TasksContentState

    data class Content(
        val tasks: List<Task>,
    ) : TasksContentState

    data object Error : TasksContentState
}

data class InlineTaskEditorUiState(
    val title: String = "",
    val isSaving: Boolean = false,
    val error: InlineTaskEditorError? = null,
)

enum class InlineTaskEditorError {
    EmptyTitle,
    Storage,
}

data class TaskStatusUpdateError(
    val taskId: Long,
    val requestedCompleted: Boolean,
)
