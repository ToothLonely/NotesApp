package dev.toothlonely.notesapp.feature.tasks.impl.presentation.tasks

sealed interface TasksEvent {
    data object TaskDeleted : TasksEvent
}
