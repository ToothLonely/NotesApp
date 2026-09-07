package dev.toothlonely.notesapp.core.data.di

import androidx.room.Room
import dev.toothlonely.notesapp.core.data.database.NotesAppDatabase
import dev.toothlonely.notesapp.core.data.database.notesMigration1To2
import dev.toothlonely.notesapp.core.data.database.notesMigration2To3
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

private const val NOTES_DATABASE_NAME = "notes-app.db"

val coreDataModule = module {
    single {
        Room.databaseBuilder(
            context = androidContext(),
            klass = NotesAppDatabase::class.java,
            name = NOTES_DATABASE_NAME,
        )
            .addMigrations(notesMigration1To2, notesMigration2To3)
            .build()
    }
    single { get<NotesAppDatabase>().notesDao() }
}
