package dev.toothlonely.notesapp.core.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import dev.toothlonely.notesapp.core.data.database.model.NoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NotesDao {
    @Query("SELECT * FROM notes ORDER BY updated_at_millis DESC, id DESC")
    fun observeNotes(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE id = :noteId")
    fun observeNote(noteId: Long): Flow<NoteEntity?>

    @Insert
    suspend fun insert(note: NoteEntity): Long

    @Query(
        """
        UPDATE notes
        SET title = :title,
            content = :content,
            generated_title_number = :generatedTitleNumber,
            updated_at_millis = :updatedAtMillis
        WHERE id = :noteId
        """,
    )
    suspend fun update(
        noteId: Long,
        title: String,
        content: String,
        generatedTitleNumber: Int?,
        updatedAtMillis: Long,
    ): Int

    @Query("DELETE FROM notes WHERE id = :noteId")
    suspend fun delete(noteId: Long): Int

    @Query("SELECT COALESCE(MAX(generated_title_number), 0) + 1 FROM notes")
    suspend fun nextGeneratedTitleNumber(): Int
}
