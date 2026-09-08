package dev.toothlonely.notesapp.core.domain.model

data class UserPreferences(
    val themeMode: ThemeMode = ThemeMode.System,
    val accentPreset: AccentPreset = AccentPreset.Indigo,
)

enum class ThemeMode {
    System,
    Light,
    Dark,
}

enum class AccentPreset {
    Indigo,
    Teal,
    Raspberry,
    Amber,
}
