package dev.toothlonely.notesapp.feature.tasks.impl.presentation.tasks.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppSizes
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppSpacing
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppTheme
import dev.toothlonely.notesapp.feature.tasks.impl.domain.model.Task

private const val COMPLETED_TASK_ALPHA = 0.70f

@Composable
fun TaskRow(
    task: Task,
    statusContentDescription: String,
    statusStateDescription: String,
    actionsContentDescription: String,
    editLabel: String,
    deleteLabel: String,
    editContentDescription: String,
    deleteContentDescription: String,
    expandTitleDescription: String,
    collapseTitleDescription: String,
    isStatusSaving: Boolean,
    areActionsEnabled: Boolean,
    onToggleStatus: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var isTitleExpanded by rememberSaveable(task.id, task.title) { mutableStateOf(false) }
    var isTitleOverflowing by rememberSaveable(task.id, task.title) { mutableStateOf(false) }
    val taskShape = MaterialTheme.shapes.large
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = NotesAppSizes.taskRowMinimumHeight)
            .clip(taskShape)
            .clickable(
                enabled = isTitleOverflowing || isTitleExpanded,
                onClickLabel = if (isTitleExpanded) {
                    collapseTitleDescription
                } else {
                    expandTitleDescription
                },
                onClick = { isTitleExpanded = !isTitleExpanded },
            ),
        shape = taskShape,
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = NotesAppSpacing.space3,
                vertical = NotesAppSpacing.space2,
            ),
            horizontalArrangement = Arrangement.spacedBy(NotesAppSpacing.space2),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(
                checked = task.isCompleted,
                onCheckedChange = { onToggleStatus() },
                modifier = Modifier
                    .size(NotesAppSizes.minimumTouchTarget)
                    .semantics {
                        contentDescription = statusContentDescription
                        stateDescription = statusStateDescription
                    },
                enabled = !isStatusSaving,
            )
            Text(
                text = task.title,
                modifier = Modifier
                    .weight(1f)
                    .alpha(if (task.isCompleted) COMPLETED_TASK_ALPHA else 1f),
                color = if (task.isCompleted) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                textDecoration = if (task.isCompleted) {
                    TextDecoration.LineThrough
                } else {
                    TextDecoration.None
                },
                maxLines = if (isTitleExpanded) Int.MAX_VALUE else COLLAPSED_TITLE_MAX_LINES,
                overflow = TextOverflow.Ellipsis,
                onTextLayout = { result ->
                    if (!isTitleExpanded) {
                        isTitleOverflowing = result.hasVisualOverflow
                    }
                },
                style = MaterialTheme.typography.bodyLarge,
            )
            TaskOverflowMenu(
                menuContentDescription = actionsContentDescription,
                editLabel = editLabel,
                deleteLabel = deleteLabel,
                editContentDescription = editContentDescription,
                deleteContentDescription = deleteContentDescription,
                onEdit = onEdit,
                onDelete = onDelete,
                enabled = areActionsEnabled && !isStatusSaving,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TaskRowPreview() {
    NotesAppTheme {
        TaskRow(
            task = Task(
                id = 1,
                title = "Подготовить презентацию",
                isCompleted = false,
                createdAtMillis = 1,
            ),
            statusContentDescription = "Подготовить презентацию, активная",
            statusStateDescription = "Активная",
            actionsContentDescription = "Действия задачи «Подготовить презентацию»",
            editLabel = "Редактировать",
            deleteLabel = "Удалить",
            editContentDescription = "Редактировать задачу «Подготовить презентацию»",
            deleteContentDescription = "Удалить задачу «Подготовить презентацию»",
            expandTitleDescription = "Показать название задачи полностью",
            collapseTitleDescription = "Свернуть название задачи",
            isStatusSaving = false,
            areActionsEnabled = true,
            onToggleStatus = {},
            onEdit = {},
            onDelete = {},
        )
    }
}

private const val COLLAPSED_TITLE_MAX_LINES = 1
