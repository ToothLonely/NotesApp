package dev.toothlonely.notesapp.di

import dev.toothlonely.notesapp.BuildConfig
import dev.toothlonely.notesapp.core.data.di.coreDataModule
import dev.toothlonely.notesapp.core.data.network.gigachat.GigaChatCredentials
import dev.toothlonely.notesapp.feature.notes.impl.di.notesModule
import dev.toothlonely.notesapp.feature.settings.impl.di.settingsModule
import dev.toothlonely.notesapp.feature.tasks.impl.di.tasksModule
import dev.toothlonely.notesapp.ui.AppThemeViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single {
        GigaChatCredentials(
            initialAccessToken = BuildConfig.GIGACHAT_ACCESS_TOKEN,
            authorizationKey = BuildConfig.GIGACHAT_AUTHORIZATION_KEY,
            scope = BuildConfig.GIGACHAT_SCOPE,
        )
    }
    includes(
        coreDataModule,
        notesModule,
        settingsModule,
        tasksModule,
    )
    viewModel { AppThemeViewModel(get()) }
}
