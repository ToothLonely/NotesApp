package dev.toothlonely.notesapp.feature.notes.impl.di

import dev.toothlonely.notesapp.feature.notes.impl.R
import dev.toothlonely.notesapp.feature.notes.impl.data.RoomNotesRepository
import dev.toothlonely.notesapp.feature.notes.impl.data.TimeProvider
import dev.toothlonely.notesapp.feature.notes.impl.domain.NoteTitleGenerator
import dev.toothlonely.notesapp.feature.notes.impl.domain.NotesRepository
import dev.toothlonely.notesapp.feature.notes.impl.presentation.editor.NoteEditorArgs
import dev.toothlonely.notesapp.feature.notes.impl.presentation.editor.NoteEditorViewModel
import dev.toothlonely.notesapp.feature.notes.impl.presentation.notes.NotesViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val notesModule = module {
    single<TimeProvider> { TimeProvider(System::currentTimeMillis) }
    single<NotesRepository> { RoomNotesRepository(get(), get()) }
    factory {
        NoteTitleGenerator(
            generatedTitleFormat = androidContext().getString(R.string.note_generated_title_format),
        )
    }
    viewModel { NotesViewModel(get()) }
    viewModel { parameters ->
        NoteEditorViewModel(
            args = parameters.get<NoteEditorArgs>(),
            notesRepository = get(),
            noteTitleGenerator = get(),
        )
    }
}
