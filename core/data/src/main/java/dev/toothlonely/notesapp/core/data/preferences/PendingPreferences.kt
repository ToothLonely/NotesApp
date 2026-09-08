package dev.toothlonely.notesapp.core.data.preferences

import dev.toothlonely.notesapp.core.domain.model.AccentPreset
import dev.toothlonely.notesapp.core.domain.model.ThemeMode

internal data class PendingPreferences(
    val themeMode: PendingValue<ThemeMode>? = null,
    val accentPreset: PendingValue<AccentPreset>? = null,
)
