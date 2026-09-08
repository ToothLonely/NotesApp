package dev.toothlonely.notesapp.core.data.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import dev.toothlonely.notesapp.core.data.database.NotesAppDatabase
import dev.toothlonely.notesapp.core.data.database.notesMigration1To2
import dev.toothlonely.notesapp.core.data.database.notesMigration2To3
import dev.toothlonely.notesapp.core.data.database.notesMigration3To4
import dev.toothlonely.notesapp.core.data.database.notesMigration4To5
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

private const val NOTES_DATABASE_NAME = "notes-app.db"
private const val USER_PREFERENCES_NAME = "notes-app-preferences"

private val Context.userPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(
    name = USER_PREFERENCES_NAME,
)

val coreDataModule = module {
    single<DataStore<Preferences>> { androidContext().userPreferencesDataStore }
    single {
        Room.databaseBuilder(
            context = androidContext(),
            klass = NotesAppDatabase::class.java,
            name = NOTES_DATABASE_NAME,
        )
            .addMigrations(
                notesMigration1To2,
                notesMigration2To3,
                notesMigration3To4,
                notesMigration4To5,
            )
            .build()
    }
    single { get<NotesAppDatabase>().notesDao() }
    single { get<NotesAppDatabase>().tasksDao() }
}
