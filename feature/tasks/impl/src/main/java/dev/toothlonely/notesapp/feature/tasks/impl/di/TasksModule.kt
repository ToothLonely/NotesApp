package dev.toothlonely.notesapp.feature.tasks.impl.di

import dev.toothlonely.notesapp.feature.tasks.impl.data.repository.RoomTasksRepository
import dev.toothlonely.notesapp.feature.tasks.impl.data.repository.TaskTimeProvider
import dev.toothlonely.notesapp.feature.tasks.impl.domain.repository.TasksRepository
import dev.toothlonely.notesapp.feature.tasks.impl.domain.usecase.TaskListOrderer
import dev.toothlonely.notesapp.feature.tasks.impl.domain.usecase.TaskTitleValidator
import dev.toothlonely.notesapp.feature.tasks.impl.presentation.tasks.TasksViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val tasksModule = module {
    single<TaskTimeProvider> { TaskTimeProvider(System::currentTimeMillis) }
    single<TasksRepository> { RoomTasksRepository(get(), get()) }
    factory { TaskListOrderer() }
    factory { TaskTitleValidator() }
    viewModel { TasksViewModel(get(), get(), get()) }
}
