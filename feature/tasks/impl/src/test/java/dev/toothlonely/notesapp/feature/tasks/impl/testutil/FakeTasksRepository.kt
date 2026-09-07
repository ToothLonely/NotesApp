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
    var observeFailure: Throwable? = null
    var createFailure: Throwable? = null
    var statusFailure: Throwable? = null
    var statusResult = true
    var createGate: CompletableDeferred<Unit>? = null
    var statusGate: CompletableDeferred<Unit>? = null
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
}
