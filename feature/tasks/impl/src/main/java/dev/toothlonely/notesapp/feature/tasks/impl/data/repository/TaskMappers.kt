package dev.toothlonely.notesapp.feature.tasks.impl.data.repository

import dev.toothlonely.notesapp.core.data.database.model.TaskEntity
import dev.toothlonely.notesapp.feature.tasks.impl.domain.model.NewTask
import dev.toothlonely.notesapp.feature.tasks.impl.domain.model.Task

internal fun TaskEntity.asDomainModel(): Task = Task(
    id = id,
    title = title,
    isCompleted = isCompleted,
    createdAtMillis = createdAtMillis,
    updatedAtMillis = updatedAtMillis,
)

internal fun NewTask.asEntity(createdAtMillis: Long): TaskEntity = TaskEntity(
    title = title,
    createdAtMillis = createdAtMillis,
    updatedAtMillis = createdAtMillis,
)
