package dev.toothlonely.notesapp.feature.notes.impl.data

import dev.toothlonely.notesapp.core.data.database.model.NoteEntity
import dev.toothlonely.notesapp.feature.notes.impl.domain.Note
import dev.toothlonely.notesapp.feature.notes.impl.domain.NewNote

internal fun NoteEntity.asDomainModel(): Note = Note(
    id = id,
    title = title,
    content = content,
    createdAtMillis = createdAtMillis,
    generatedTitleNumber = generatedTitleNumber,
    updatedAtMillis = updatedAtMillis,
)

internal fun NewNote.asEntity(createdAtMillis: Long): NoteEntity = NoteEntity(
    title = title,
    content = content,
    createdAtMillis = createdAtMillis,
    generatedTitleNumber = generatedTitleNumber,
    updatedAtMillis = createdAtMillis,
)
