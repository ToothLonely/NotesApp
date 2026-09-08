package dev.toothlonely.notesapp.feature.tasks.impl.presentation.tasks.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppTheme
import dev.toothlonely.notesapp.core.designsystem.component.SortMenu
import dev.toothlonely.notesapp.feature.tasks.impl.domain.model.TaskSortOrder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksTopBar(
    title: String,
    sortOrder: TaskSortOrder,
    sortContentDescription: String,
    newestFirstLabel: String,
    oldestFirstLabel: String,
    onSortOrderChange: (TaskSortOrder) -> Unit,
    modifier: Modifier = Modifier,
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                modifier = Modifier.semantics { heading() },
            )
        },
        actions = {
            SortMenu(
                newestFirst = sortOrder == TaskSortOrder.NewestFirst,
                sortContentDescription = sortContentDescription,
                newestFirstLabel = newestFirstLabel,
                oldestFirstLabel = oldestFirstLabel,
                onNewestFirst = { onSortOrderChange(TaskSortOrder.NewestFirst) },
                onOldestFirst = { onSortOrderChange(TaskSortOrder.OldestFirst) },
            )
        },
        modifier = modifier,
    )
}

@Preview(showBackground = true)
@Composable
private fun TasksTopBarPreview() {
    NotesAppTheme {
        TasksTopBar(
            title = "Задачи",
            sortOrder = TaskSortOrder.NewestFirst,
            sortContentDescription = "Сортировать задачи",
            newestFirstLabel = "Сначала новые",
            oldestFirstLabel = "Сначала старые",
            onSortOrderChange = {},
        )
    }
}
