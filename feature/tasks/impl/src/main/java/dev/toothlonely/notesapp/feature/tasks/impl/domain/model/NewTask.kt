package dev.toothlonely.notesapp.feature.tasks.impl.domain.model

data class NewTask(
    val title: String,
) {
    init {
        require(title.isNotBlank() && title == title.trim()) {
            "A task title must be non-blank and trimmed"
        }
    }
}
