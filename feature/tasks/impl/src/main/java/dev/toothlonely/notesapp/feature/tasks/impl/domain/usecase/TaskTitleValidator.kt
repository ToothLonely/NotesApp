package dev.toothlonely.notesapp.feature.tasks.impl.domain.usecase

class TaskTitleValidator {
    fun validate(title: String): String? = title.trim().takeIf(String::isNotEmpty)
}
