package dev.toothlonely.notesapp.feature.tasks.impl.data.repository

import dev.toothlonely.notesapp.core.data.database.TasksDao
import dev.toothlonely.notesapp.feature.tasks.impl.domain.model.NewTask
import dev.toothlonely.notesapp.feature.tasks.impl.domain.model.Task
import dev.toothlonely.notesapp.feature.tasks.impl.domain.repository.TasksRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomTasksRepository(
    private val tasksDao: TasksDao,
    private val timeProvider: TaskTimeProvider,
) : TasksRepository {
    override fun observeTasks(): Flow<List<Task>> = tasksDao.observeTasks().map { tasks ->
        tasks.map { task -> task.asDomainModel() }
    }

    override suspend fun createTask(task: NewTask) {
        tasksDao.insert(
            task.asEntity(createdAtMillis = timeProvider.currentTimeMillis()),
        )
    }

    override suspend fun setTaskCompleted(taskId: Long, isCompleted: Boolean): Boolean =
        tasksDao.updateCompleted(taskId, isCompleted) > 0
}
