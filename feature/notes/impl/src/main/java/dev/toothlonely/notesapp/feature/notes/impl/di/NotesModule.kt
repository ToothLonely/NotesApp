package dev.toothlonely.notesapp.feature.notes.impl.di

import dev.toothlonely.notesapp.feature.notes.impl.R
import dev.toothlonely.notesapp.feature.notes.impl.data.image.AndroidNoteImageStorage
import dev.toothlonely.notesapp.feature.notes.impl.data.preferences.DataStoreNotesViewModeRepository
import dev.toothlonely.notesapp.feature.notes.impl.data.repository.RoomNotesRepository
import dev.toothlonely.notesapp.feature.notes.impl.data.repository.TimeProvider
import dev.toothlonely.notesapp.feature.notes.impl.domain.usecase.NoteTitleGenerator
import dev.toothlonely.notesapp.feature.notes.impl.domain.repository.NoteImageStorage
import dev.toothlonely.notesapp.feature.notes.impl.domain.usecase.NoteListProcessor
import dev.toothlonely.notesapp.feature.notes.impl.domain.repository.NotesRepository
import dev.toothlonely.notesapp.feature.notes.impl.domain.repository.NotesViewModeRepository
import dev.toothlonely.notesapp.feature.notes.impl.presentation.editor.model.NoteEditorArgs
import dev.toothlonely.notesapp.feature.notes.impl.presentation.editor.NoteEditorViewModel
import dev.toothlonely.notesapp.feature.notes.impl.presentation.image.NoteImageLoader
import dev.toothlonely.notesapp.feature.notes.impl.presentation.notes.NotesViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val notesModule = module {
    single<TimeProvider> { TimeProvider(System::currentTimeMillis) }
    single<NoteImageStorage> { AndroidNoteImageStorage(androidContext()) }
    single { NoteImageLoader(get()) }
    single<NotesRepository> { RoomNotesRepository(get(), get(), get()) }
    single<NotesViewModeRepository> { DataStoreNotesViewModeRepository(get()) }
    factory { NoteListProcessor() }
    factory {
        NoteTitleGenerator(
            generatedTitleFormat = androidContext().getString(R.string.note_generated_title_format),
        )
    }
    viewModel { NotesViewModel(get(), get(), get()) }
    viewModel { parameters ->
        NoteEditorViewModel(
            args = parameters.get<NoteEditorArgs>(),
            notesRepository = get(),
            noteTitleGenerator = get(),
            imageStorage = get(),
            speechRecognitionRepository = get(),
        )
    }
}
