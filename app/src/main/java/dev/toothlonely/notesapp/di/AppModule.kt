package dev.toothlonely.notesapp.di

import dev.toothlonely.notesapp.core.data.di.coreDataModule
import dev.toothlonely.notesapp.feature.notes.impl.di.notesModule
import dev.toothlonely.notesapp.feature.settings.impl.di.settingsModule
import dev.toothlonely.notesapp.feature.tasks.impl.di.tasksModule
import dev.toothlonely.notesapp.ui.AppThemeViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    includes(
        coreDataModule,
        notesModule,
        settingsModule,
        tasksModule,
    )
    viewModel { AppThemeViewModel(get()) }
}
