package dev.toothlonely.notesapp.feature.tasks.impl.domain.repository

import dev.toothlonely.notesapp.feature.tasks.impl.domain.model.NewTask
import dev.toothlonely.notesapp.feature.tasks.impl.domain.model.Task
import kotlinx.coroutines.flow.Flow

interface TasksRepository {
    fun observeTasks(): Flow<List<Task>>

    suspend fun createTask(task: NewTask)

    suspend fun setTaskCompleted(taskId: Long, isCompleted: Boolean): Boolean

    suspend fun updateTaskTitle(taskId: Long, title: String): Boolean

    suspend fun deleteTask(taskId: Long): Boolean
}
