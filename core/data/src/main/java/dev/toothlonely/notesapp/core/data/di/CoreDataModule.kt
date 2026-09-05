package dev.toothlonely.notesapp.core.data.di

import androidx.room.Room
import dev.toothlonely.notesapp.core.data.database.NotesAppDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

private const val NOTES_DATABASE_NAME = "notes-app.db"

val coreDataModule = module {
    single {
        Room.databaseBuilder(
            context = androidContext(),
            klass = NotesAppDatabase::class.java,
            name = NOTES_DATABASE_NAME,
        ).build()
    }
    single { get<NotesAppDatabase>().notesDao() }
}
