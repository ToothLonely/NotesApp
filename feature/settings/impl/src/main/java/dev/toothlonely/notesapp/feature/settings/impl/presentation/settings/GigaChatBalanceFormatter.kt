package dev.toothlonely.notesapp.feature.settings.impl.presentation.settings

internal fun formatGigaChatTokenCount(tokenCount: Long): String =
    tokenCount.toString().replace(TOKEN_GROUP_SEPARATOR_REGEX, " ")

private val TOKEN_GROUP_SEPARATOR_REGEX = Regex("(?<=\\d)(?=(\\d{3})+$)")
