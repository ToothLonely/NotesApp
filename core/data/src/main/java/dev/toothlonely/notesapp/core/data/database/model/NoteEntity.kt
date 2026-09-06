package dev.toothlonely.notesapp.core.data.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "notes",
    indices = [Index(value = ["generated_title_number"], unique = true)],
)
data class NoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val content: String,
    @ColumnInfo(name = "created_at_millis")
    val createdAtMillis: Long,
    @ColumnInfo(name = "generated_title_number")
    val generatedTitleNumber: Int?,
    @ColumnInfo(name = "updated_at_millis", defaultValue = "0")
    val updatedAtMillis: Long = createdAtMillis,
)
