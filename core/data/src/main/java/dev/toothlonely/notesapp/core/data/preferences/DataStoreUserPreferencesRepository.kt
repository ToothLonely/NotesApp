package dev.toothlonely.notesapp.core.data.preferences

import dev.toothlonely.notesapp.core.domain.model.AccentPreset
import dev.toothlonely.notesapp.core.domain.model.ThemeMode
import dev.toothlonely.notesapp.core.domain.model.UserPreferences
import dev.toothlonely.notesapp.core.domain.repository.UserPreferencesRepository
import java.util.concurrent.atomic.AtomicLong
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update

class DataStoreUserPreferencesRepository(
    private val dataSource: UserPreferencesDataSource,
) : UserPreferencesRepository {
    private val pendingPreferences = MutableStateFlow(PendingPreferences())
    private val operationIds = AtomicLong()

    override fun observeUserPreferences(): Flow<UserPreferences> = combine(
        dataSource.userPreferences,
        pendingPreferences,
    ) { storedPreferences, pending ->
        UserPreferences(
            themeMode = pending.themeMode?.value ?: storedPreferences.themeMode,
            accentPreset = pending.accentPreset?.value ?: storedPreferences.accentPreset,
        )
    }.distinctUntilChanged()

    override suspend fun setThemeMode(themeMode: ThemeMode) {
        val pendingValue = PendingValue(operationIds.incrementAndGet(), themeMode)
        pendingPreferences.update { current -> current.copy(themeMode = pendingValue) }
        try {
            dataSource.setThemeMode(themeMode)
        } finally {
            pendingPreferences.update { current ->
                current.copy(
                    themeMode = current.themeMode.unlessOperation(pendingValue.operationId),
                )
            }
        }
    }

    override suspend fun setAccentPreset(accentPreset: AccentPreset) {
        val pendingValue = PendingValue(operationIds.incrementAndGet(), accentPreset)
        pendingPreferences.update { current -> current.copy(accentPreset = pendingValue) }
        try {
            dataSource.setAccentPreset(accentPreset)
        } finally {
            pendingPreferences.update { current ->
                current.copy(
                    accentPreset = current.accentPreset.unlessOperation(pendingValue.operationId),
                )
            }
        }
    }

    override suspend fun resetAppearance() {
        val operationId = operationIds.incrementAndGet()
        val pendingThemeMode = PendingValue(operationId, ThemeMode.System)
        val pendingAccentPreset = PendingValue(operationId, AccentPreset.Indigo)
        pendingPreferences.value = PendingPreferences(
            themeMode = pendingThemeMode,
            accentPreset = pendingAccentPreset,
        )
        try {
            dataSource.resetAppearance()
        } finally {
            pendingPreferences.update { current ->
                current.copy(
                    themeMode = current.themeMode.unlessOperation(operationId),
                    accentPreset = current.accentPreset.unlessOperation(operationId),
                )
            }
        }
    }
}

private data class PendingPreferences(
    val themeMode: PendingValue<ThemeMode>? = null,
    val accentPreset: PendingValue<AccentPreset>? = null,
)

private data class PendingValue<T>(
    val operationId: Long,
    val value: T,
)

private fun <T> PendingValue<T>?.unlessOperation(operationId: Long): PendingValue<T>? =
    if (this?.operationId == operationId) null else this
