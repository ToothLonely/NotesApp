package dev.toothlonely.notesapp.core.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import dev.toothlonely.notesapp.core.data.database.model.NoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NotesDao {
    @Query("SELECT * FROM notes ORDER BY created_at_millis DESC, id DESC")
    fun observeNotes(): Flow<List<NoteEntity>>

    @Insert
    suspend fun insert(note: NoteEntity): Long

    @Query("SELECT COALESCE(MAX(generated_title_number), 0) + 1 FROM notes")
    suspend fun nextGeneratedTitleNumber(): Int
}
