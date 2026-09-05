package dev.toothlonely.notesapp.core.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import dev.toothlonely.notesapp.core.data.database.model.NoteEntity

@Database(
    entities = [NoteEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class NotesAppDatabase : RoomDatabase() {
    abstract fun notesDao(): NotesDao
}
