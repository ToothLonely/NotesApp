package dev.toothlonely.notesapp.feature.tasks.impl.presentation.tasks.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppSpacing
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppTheme
import dev.toothlonely.notesapp.feature.tasks.impl.domain.model.Task
import dev.toothlonely.notesapp.feature.tasks.impl.presentation.tasks.InlineTaskEditorUiState
import dev.toothlonely.notesapp.feature.tasks.impl.presentation.tasks.components.InlineTaskEditor
import dev.toothlonely.notesapp.feature.tasks.impl.presentation.tasks.components.TaskRow

@Composable
fun TasksListScreen(
    tasks: List<Task>,
    editor: InlineTaskEditorUiState?,
    pendingStatusTaskIds: Set<Long>,
    taskStatusContentDescription: (Task) -> String,
    activeStateDescription: String,
    completedStateDescription: String,
    editorPlaceholder: String,
    editorModeDescription: String,
    editorErrorMessage: String?,
    confirmTaskDescription: String,
    cancelTaskDescription: String,
    savingTaskDescription: String,
    onDraftTitleChange: (String) -> Unit,
    onConfirmTask: () -> Unit,
    onCancelTask: () -> Unit,
    onToggleTaskStatus: (Long) -> Unit,
    bottomContentPadding: Dp,
    state: LazyListState,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        state = state,
        contentPadding = PaddingValues(
            top = NotesAppSpacing.space3,
            bottom = bottomContentPadding + NotesAppSpacing.space4,
        ),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(
            NotesAppSpacing.space2,
        ),
    ) {
        editor?.let { editorState ->
            item(key = "inline-task-editor") {
                InlineTaskEditor(
                    title = editorState.title,
                    placeholder = editorPlaceholder,
                    modeDescription = editorModeDescription,
                    errorMessage = editorErrorMessage,
                    confirmDescription = confirmTaskDescription,
                    cancelDescription = cancelTaskDescription,
                    savingDescription = savingTaskDescription,
                    isSaving = editorState.isSaving,
                    onTitleChange = onDraftTitleChange,
                    onConfirm = onConfirmTask,
                    onCancel = onCancelTask,
                )
            }
        }
        items(
            items = tasks,
            key = Task::id,
        ) { task ->
            TaskRow(
                task = task,
                statusContentDescription = taskStatusContentDescription(task),
                statusStateDescription = if (task.isCompleted) {
                    completedStateDescription
                } else {
                    activeStateDescription
                },
                isStatusSaving = task.id in pendingStatusTaskIds,
                onToggleStatus = { onToggleTaskStatus(task.id) },
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TasksListScreenPreview() {
    NotesAppTheme {
        TasksListScreen(
            tasks = listOf(
                Task(1, "Подготовить презентацию", false, 2),
                Task(2, "Позвонить маме", true, 1),
            ),
            editor = null,
            pendingStatusTaskIds = emptySet(),
            taskStatusContentDescription = { task -> task.title },
            activeStateDescription = "Активная",
            completedStateDescription = "Выполненная",
            editorPlaceholder = "Название задачи",
            editorModeDescription = "Создание задачи",
            editorErrorMessage = null,
            confirmTaskDescription = "Сохранить задачу",
            cancelTaskDescription = "Отменить создание задачи",
            savingTaskDescription = "Сохраняем задачу",
            onDraftTitleChange = {},
            onConfirmTask = {},
            onCancelTask = {},
            onToggleTaskStatus = {},
            bottomContentPadding = 0.dp,
            state = rememberLazyListState(),
        )
    }
}
