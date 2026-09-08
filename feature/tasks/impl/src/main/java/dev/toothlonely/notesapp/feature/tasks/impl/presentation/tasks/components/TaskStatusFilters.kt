package dev.toothlonely.notesapp.feature.tasks.impl.presentation.tasks.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppSpacing
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppTheme
import dev.toothlonely.notesapp.feature.tasks.impl.domain.model.TaskStatusFilter

@Composable
fun TaskStatusFilters(
    selectedFilter: TaskStatusFilter,
    allLabel: String,
    activeLabel: String,
    completedLabel: String,
    onFilterSelected: (TaskStatusFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(NotesAppSpacing.space2),
    ) {
        TaskStatusFilter.entries.forEach { filter ->
            FilterChip(
                selected = selectedFilter == filter,
                onClick = { onFilterSelected(filter) },
                label = {
                    Text(
                        text = when (filter) {
                            TaskStatusFilter.All -> allLabel
                            TaskStatusFilter.Active -> activeLabel
                            TaskStatusFilter.Completed -> completedLabel
                        },
                    )
                },
                modifier = Modifier.semantics { role = Role.RadioButton },
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TaskStatusFiltersPreview() {
    NotesAppTheme {
        TaskStatusFilters(
            selectedFilter = TaskStatusFilter.All,
            allLabel = "Все",
            activeLabel = "Активные",
            completedLabel = "Выполненные",
            onFilterSelected = {},
        )
    }
}
