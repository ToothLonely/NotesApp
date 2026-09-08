package dev.toothlonely.notesapp.feature.tasks.impl.presentation.tasks

import dev.toothlonely.notesapp.feature.tasks.impl.domain.model.Task
import dev.toothlonely.notesapp.feature.tasks.impl.domain.model.TaskSortOrder
import dev.toothlonely.notesapp.feature.tasks.impl.domain.model.TaskStatusFilter

data class TasksUiState(
    val content: TasksContentState = TasksContentState.Loading,
    val editor: InlineTaskEditorUiState? = null,
    val draftQuery: String = "",
    val appliedQuery: String = "",
    val statusFilter: TaskStatusFilter = TaskStatusFilter.All,
    val sortOrder: TaskSortOrder = TaskSortOrder.NewestFirst,
    val pendingStatusTaskIds: Set<Long> = emptySet(),
    val statusUpdateError: TaskStatusUpdateError? = null,
    val deleteConfirmation: DeleteTaskConfirmationUiState? = null,
    val deleteError: TaskDeleteError? = null,
)

sealed interface TasksContentState {
    data object Loading : TasksContentState

    data object Empty : TasksContentState

    data object SearchEmpty : TasksContentState

    data class Content(
        val tasks: List<Task>,
    ) : TasksContentState

    data object Error : TasksContentState
}

data class InlineTaskEditorUiState(
    val taskId: Long? = null,
    val originalTitle: String? = null,
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

data class DeleteTaskConfirmationUiState(
    val taskId: Long,
    val taskTitle: String,
    val isDeleting: Boolean = false,
)

data class TaskDeleteError(
    val taskId: Long,
    val taskTitle: String,
)
