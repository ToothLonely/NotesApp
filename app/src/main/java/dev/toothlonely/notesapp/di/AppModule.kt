package dev.toothlonely.notesapp.di

import dev.toothlonely.notesapp.core.data.di.coreDataModule
import dev.toothlonely.notesapp.feature.notes.impl.di.notesModule
import dev.toothlonely.notesapp.feature.tasks.impl.di.tasksModule
import org.koin.dsl.module

val appModule = module {
    includes(
        coreDataModule,
        notesModule,
        tasksModule,
    )
}
