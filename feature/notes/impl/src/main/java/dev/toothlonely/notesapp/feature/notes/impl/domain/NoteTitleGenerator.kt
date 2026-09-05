package dev.toothlonely.notesapp.feature.notes.impl.domain

class NoteTitleGenerator(
    private val generatedTitleFormat: String,
) {
    fun suggestedTitle(number: Int): String = generatedTitleFormat.format(number)

    fun resolve(input: String, number: Int): ResolvedNoteTitle {
        val trimmedTitle = input.trim()
        return if (trimmedTitle.isNotEmpty()) {
            ResolvedNoteTitle(
                value = trimmedTitle,
                generatedTitleNumber = null,
            )
        } else {
            ResolvedNoteTitle(
                value = suggestedTitle(number),
                generatedTitleNumber = number,
            )
        }
    }
}

data class ResolvedNoteTitle(
    val value: String,
    val generatedTitleNumber: Int?,
)
