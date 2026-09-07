package dev.toothlonely.notesapp.core.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import dev.toothlonely.notesapp.core.data.database.model.NoteEntity
import dev.toothlonely.notesapp.core.data.database.model.TaskEntity

@Database(
    entities = [
        NoteEntity::class,
        TaskEntity::class,
    ],
    version = 4,
    exportSchema = true,
)
abstract class NotesAppDatabase : RoomDatabase() {
    abstract fun notesDao(): NotesDao

    abstract fun tasksDao(): TasksDao
}
