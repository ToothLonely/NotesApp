package dev.toothlonely.notesapp.di

import android.content.Context
import dev.toothlonely.notesapp.feature.notes.impl.presentation.editor.NoteEditorArgs
import dev.toothlonely.notesapp.feature.notes.impl.presentation.editor.NoteEditorViewModel
import org.junit.Test
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.test.verify.definition
import org.koin.test.verify.injectedParameters
import org.koin.test.verify.verify

@OptIn(KoinExperimentalAPI::class)
class AppModuleTest {
    @Test
    fun `app Koin graph is valid`() {
        appModule.verify(
            extraTypes = listOf(Context::class),
            injections = injectedParameters(
                definition<NoteEditorViewModel>(NoteEditorArgs::class),
            ),
        )
    }
}
