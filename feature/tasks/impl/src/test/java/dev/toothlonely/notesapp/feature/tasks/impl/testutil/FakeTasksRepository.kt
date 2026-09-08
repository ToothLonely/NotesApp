package dev.toothlonely.notesapp.feature.tasks.impl.testutil

import dev.toothlonely.notesapp.feature.tasks.impl.domain.model.NewTask
import dev.toothlonely.notesapp.feature.tasks.impl.domain.model.Task
import dev.toothlonely.notesapp.feature.tasks.impl.domain.repository.TasksRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeTasksRepository : TasksRepository {
    val tasks = MutableStateFlow<List<Task>>(emptyList())
    val createdTasks = mutableListOf<NewTask>()
    val statusUpdates = mutableListOf<Pair<Long, Boolean>>()
    val titleUpdates = mutableListOf<Pair<Long, String>>()
    val deletedTaskIds = mutableListOf<Long>()
    var observeFailure: Throwable? = null
    var createFailure: Throwable? = null
    var statusFailure: Throwable? = null
    var updateFailure: Throwable? = null
    var deleteFailure: Throwable? = null
    var statusResult = true
    var updateResult = true
    var deleteResult = true
    var createGate: CompletableDeferred<Unit>? = null
    var statusGate: CompletableDeferred<Unit>? = null
    var updateGate: CompletableDeferred<Unit>? = null
    var deleteGate: CompletableDeferred<Unit>? = null
    var nextTaskId = 100L
    var currentTimeMillis = 1_000L

    override fun observeTasks(): Flow<List<Task>> {
        observeFailure?.let { throw it }
        return tasks
    }

    override suspend fun createTask(task: NewTask) {
        createGate?.await()
        createFailure?.let { throw it }
        createdTasks += task
        tasks.value = tasks.value + Task(
            id = nextTaskId++,
            title = task.title,
            isCompleted = false,
            createdAtMillis = currentTimeMillis++,
        )
    }

    override suspend fun setTaskCompleted(taskId: Long, isCompleted: Boolean): Boolean {
        statusGate?.await()
        statusFailure?.let { throw it }
        statusUpdates += taskId to isCompleted
        if (!statusResult || tasks.value.none { task -> task.id == taskId }) return false
        tasks.value = tasks.value.map { task ->
            if (task.id == taskId) task.copy(isCompleted = isCompleted) else task
        }
        return true
    }

    override suspend fun updateTaskTitle(taskId: Long, title: String): Boolean {
        updateGate?.await()
        updateFailure?.let { throw it }
        titleUpdates += taskId to title
        if (!updateResult || tasks.value.none { task -> task.id == taskId }) return false
        tasks.value = tasks.value.map { task ->
            if (task.id == taskId) {
                task.copy(title = title, updatedAtMillis = currentTimeMillis++)
            } else {
                task
            }
        }
        return true
    }

    override suspend fun deleteTask(taskId: Long): Boolean {
        deleteGate?.await()
        deleteFailure?.let { throw it }
        deletedTaskIds += taskId
        if (!deleteResult || tasks.value.none { task -> task.id == taskId }) return false
        tasks.value = tasks.value.filterNot { task -> task.id == taskId }
        return true
    }
}
