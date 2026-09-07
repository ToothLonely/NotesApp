package dev.toothlonely.notesapp.feature.notes.impl.domain.usecase

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

        val queryWords = normalizedQuery.toWords()
        val exactTitleMatches = mutableListOf<Note>()
        val fuzzyTitleMatches = mutableListOf<Note>()
        val exactContentMatches = mutableListOf<Note>()
        val fuzzyContentMatches = mutableListOf<Note>()
        notes.forEach { note ->
            val normalizedTitle = note.title.lowercase()
            val normalizedContent = note.content.lowercase()
            when {
                normalizedTitle.contains(normalizedQuery) -> exactTitleMatches += note
                normalizedTitle.fuzzyContains(queryWords) -> fuzzyTitleMatches += note
                normalizedContent.contains(normalizedQuery) -> exactContentMatches += note
                normalizedContent.fuzzyContains(queryWords) -> fuzzyContentMatches += note
            }
        }
        return buildList(notes.size) {
            addAll(exactTitleMatches.sortedWith(comparator))
            addAll(fuzzyTitleMatches.sortedWith(comparator))
            addAll(exactContentMatches.sortedWith(comparator))
            addAll(fuzzyContentMatches.sortedWith(comparator))
        }
    }

    private fun String.fuzzyContains(queryWords: List<String>): Boolean {
        if (queryWords.isEmpty()) return false

        val candidateWords = toWords()
        return queryWords.all { queryWord ->
            candidateWords.any { candidateWord ->
                candidateWord.contains(queryWord) ||
                    queryWord.length >= MINIMUM_FUZZY_QUERY_LENGTH &&
                    isFuzzySubsequence(queryWord, candidateWord)
            }
        }
    }

    private fun String.toWords(): List<String> = WORD_REGEX
        .findAll(this)
        .map(MatchResult::value)
        .toList()

    private companion object {
        const val MINIMUM_FUZZY_QUERY_LENGTH = 3
        val WORD_REGEX = Regex("[\\p{L}\\p{N}]+")
    }
}
