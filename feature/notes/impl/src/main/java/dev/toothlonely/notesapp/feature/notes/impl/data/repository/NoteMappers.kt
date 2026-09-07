package dev.toothlonely.notesapp.feature.notes.impl.data.repository

import dev.toothlonely.notesapp.core.data.database.model.NoteEntity
import dev.toothlonely.notesapp.feature.notes.impl.domain.model.Note
import dev.toothlonely.notesapp.feature.notes.impl.domain.model.NewNote

internal fun NoteEntity.asDomainModel(): Note = Note(
    id = id,
    title = title,
    content = content,
    createdAtMillis = createdAtMillis,
    generatedTitleNumber = generatedTitleNumber,
    updatedAtMillis = updatedAtMillis,
    imageFileName = imageFileName,
)

internal fun NewNote.asEntity(
    createdAtMillis: Long,
    imageFileName: String?,
): NoteEntity = NoteEntity(
    title = title,
    content = content,
    createdAtMillis = createdAtMillis,
    generatedTitleNumber = generatedTitleNumber,
    updatedAtMillis = createdAtMillis,
    imageFileName = imageFileName,
)
