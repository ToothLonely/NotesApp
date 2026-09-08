package dev.toothlonely.notesapp.core.domain.search

object FuzzySearch {
    fun matches(
        candidate: String,
        query: String,
    ): Boolean {
        val normalizedQuery = query.trim().lowercase()
        if (normalizedQuery.isEmpty()) return true

        val normalizedCandidate = candidate.lowercase()
        if (normalizedCandidate.contains(normalizedQuery)) return true

        val queryWords = normalizedQuery.toWords()
        if (queryWords.isEmpty()) return false

        val candidateWords = normalizedCandidate.toWords()
        return queryWords.all { queryWord ->
            candidateWords.any { candidateWord ->
                candidateWord.contains(queryWord) ||
                    queryWord.length >= MINIMUM_FUZZY_QUERY_LENGTH &&
                    isSubsequence(queryWord = queryWord, candidateWord = candidateWord)
            }
        }
    }

    internal fun isSubsequence(
        queryWord: String,
        candidateWord: String,
    ): Boolean {
        if (queryWord.isEmpty()) return true

        var queryIndex = 0
        candidateWord.forEach { candidateCharacter ->
            if (queryWord[queryIndex] == candidateCharacter) {
                queryIndex += 1
                if (queryIndex == queryWord.length) return true
            }
        }
        return false
    }

    private fun String.toWords(): List<String> = WORD_REGEX
        .findAll(this)
        .map(MatchResult::value)
        .toList()

    private const val MINIMUM_FUZZY_QUERY_LENGTH = 3
    private val WORD_REGEX = Regex("[\\p{L}\\p{N}]+")
}
