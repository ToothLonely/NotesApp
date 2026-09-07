package dev.toothlonely.notesapp.feature.notes.impl.domain.usecase

internal fun isFuzzySubsequence(
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
