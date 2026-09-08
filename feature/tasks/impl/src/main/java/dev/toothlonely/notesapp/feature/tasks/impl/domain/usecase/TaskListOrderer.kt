package dev.toothlonely.notesapp.feature.tasks.impl.domain.usecase

import dev.toothlonely.notesapp.core.domain.search.FuzzySearch
import dev.toothlonely.notesapp.feature.tasks.impl.domain.model.Task
import dev.toothlonely.notesapp.feature.tasks.impl.domain.model.TaskSortOrder
import dev.toothlonely.notesapp.feature.tasks.impl.domain.model.TaskStatusFilter

class TaskListOrderer {
    fun process(
        tasks: List<Task>,
        appliedQuery: String,
        statusFilter: TaskStatusFilter,
        sortOrder: TaskSortOrder,
    ): List<Task> {
        val normalizedQuery = appliedQuery.trim().lowercase()
        val filteredTasks = tasks.filter { task ->
            val matchesQuery = FuzzySearch.matches(
                candidate = task.title,
                query = normalizedQuery,
            )
            val matchesStatus = when (statusFilter) {
                TaskStatusFilter.All -> true
                TaskStatusFilter.Active -> !task.isCompleted
                TaskStatusFilter.Completed -> task.isCompleted
            }
            matchesQuery && matchesStatus
        }
        val timeComparator = when (sortOrder) {
            TaskSortOrder.NewestFirst -> compareByDescending<Task>(Task::createdAtMillis)
                .thenByDescending(Task::id)

            TaskSortOrder.OldestFirst -> compareBy<Task>(Task::createdAtMillis)
                .thenBy(Task::id)
        }
        return filteredTasks.sortedWith(timeComparator)
    }
}
