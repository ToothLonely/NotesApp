package dev.toothlonely.notesapp.feature.notes.impl.domain.usecase

import dev.toothlonely.notesapp.core.domain.search.FuzzySearch
import dev.toothlonely.notesapp.feature.notes.impl.domain.model.Note
import dev.toothlonely.notesapp.feature.notes.impl.domain.model.NotesSortOrder

class NoteListProcessor {
    fun process(
        notes: List<Note>,
        appliedQuery: String,
        sortOrder: NotesSortOrder,
    ): List<Note> {
        val comparator = when (sortOrder) {
            NotesSortOrder.NewestFirst -> compareByDescending<Note>(Note::updatedAtMillis)
                .thenByDescending(Note::id)

            NotesSortOrder.OldestFirst -> compareBy<Note>(Note::updatedAtMillis)
                .thenBy(Note::id)
        }
        val normalizedQuery = appliedQuery.trim().lowercase()
        if (normalizedQuery.isBlank()) return notes.sortedWith(comparator)

        val exactTitleMatches = mutableListOf<Note>()
        val fuzzyTitleMatches = mutableListOf<Note>()
        val exactContentMatches = mutableListOf<Note>()
        val fuzzyContentMatches = mutableListOf<Note>()
        notes.forEach { note ->
            val normalizedTitle = note.title.lowercase()
            val normalizedContent = note.content.lowercase()
            when {
                normalizedTitle.contains(normalizedQuery) -> exactTitleMatches += note
                FuzzySearch.matches(normalizedTitle, normalizedQuery) -> fuzzyTitleMatches += note
                normalizedContent.contains(normalizedQuery) -> exactContentMatches += note
                FuzzySearch.matches(normalizedContent, normalizedQuery) -> fuzzyContentMatches += note
            }
        }
        return buildList(notes.size) {
            addAll(exactTitleMatches.sortedWith(comparator))
            addAll(fuzzyTitleMatches.sortedWith(comparator))
            addAll(exactContentMatches.sortedWith(comparator))
            addAll(fuzzyContentMatches.sortedWith(comparator))
        }
    }

}
