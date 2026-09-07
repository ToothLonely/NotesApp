package dev.toothlonely.notesapp.feature.tasks.impl.domain.usecase

import dev.toothlonely.notesapp.feature.tasks.impl.domain.model.Task

class TaskListOrderer {
    fun order(tasks: List<Task>): List<Task> = tasks.sortedWith(
        compareBy<Task>(Task::isCompleted)
            .thenByDescending(Task::createdAtMillis)
            .thenByDescending(Task::id),
    )
}
