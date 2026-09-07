package dev.toothlonely.notesapp.feature.tasks.impl.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import dev.toothlonely.notesapp.feature.tasks.api.TasksRoute
import dev.toothlonely.notesapp.feature.tasks.impl.presentation.tasks.screens.TasksPlaceholderScreen

fun EntryProviderScope<NavKey>.tasksEntries(
    bottomNavigationPadding: PaddingValues,
) {
    entry<TasksRoute> {
        TasksPlaceholderScreen(bottomNavigationPadding = bottomNavigationPadding)
    }
}
