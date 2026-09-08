package dev.toothlonely.notesapp.core.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.preferencesOf
import dev.toothlonely.notesapp.core.data.preferences.UserPreferencesDataSource.Companion.ACCENT_PRESET_AMBER
import dev.toothlonely.notesapp.core.data.preferences.UserPreferencesDataSource.Companion.ACCENT_PRESET_KEY
import dev.toothlonely.notesapp.core.data.preferences.UserPreferencesDataSource.Companion.THEME_MODE_DARK
import dev.toothlonely.notesapp.core.data.preferences.UserPreferencesDataSource.Companion.THEME_MODE_KEY
import dev.toothlonely.notesapp.core.domain.model.AccentPreset
import dev.toothlonely.notesapp.core.domain.model.ThemeMode
import dev.toothlonely.notesapp.core.domain.model.UserPreferences
import java.io.IOException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DataStoreUserPreferencesRepositoryTest {
    @Test
    fun `theme is observed while its durable write is pending`() = runTest {
        val writeGate = CompletableDeferred<Unit>()
        val dataStore = ControllablePreferencesDataStore(emptyPreferences()).apply {
            gate = writeGate
        }
        val repository = DataStoreUserPreferencesRepository(
            UserPreferencesDataSource(dataStore),
        )

        val write = async { repository.setThemeMode(ThemeMode.Dark) }
        runCurrent()

        assertEquals(ThemeMode.Dark, repository.observeUserPreferences().first().themeMode)
        assertFalse(write.isCompleted)

        writeGate.complete(Unit)
        write.await()

        assertEquals(THEME_MODE_DARK, dataStore.data.first()[THEME_MODE_KEY])
        assertEquals(ThemeMode.Dark, repository.observeUserPreferences().first().themeMode)
    }

    @Test
    fun `accent write failure rolls observed value back`() = runTest {
        val expectedFailure = IOException("Write failed")
        val writeGate = CompletableDeferred<Unit>()
        val dataStore = ControllablePreferencesDataStore(emptyPreferences()).apply {
            gate = writeGate
            failure = expectedFailure
        }
        val repository = DataStoreUserPreferencesRepository(
            UserPreferencesDataSource(dataStore),
        )

        val write = async { runCatching { repository.setAccentPreset(AccentPreset.Teal) } }
        runCurrent()

        assertEquals(AccentPreset.Teal, repository.observeUserPreferences().first().accentPreset)
        assertFalse(write.isCompleted)

        writeGate.complete(Unit)
        val failure = write.await().exceptionOrNull()

        assertTrue(failure === expectedFailure)
        assertEquals(UserPreferences(), repository.observeUserPreferences().first())
    }

    @Test
    fun `reset is observed while its durable write is pending`() = runTest {
        val writeGate = CompletableDeferred<Unit>()
        val dataStore = ControllablePreferencesDataStore(
            preferencesOf(
                THEME_MODE_KEY to THEME_MODE_DARK,
                ACCENT_PRESET_KEY to ACCENT_PRESET_AMBER,
            ),
        ).apply {
            gate = writeGate
        }
        val repository = DataStoreUserPreferencesRepository(
            UserPreferencesDataSource(dataStore),
        )

        val write = async { repository.resetAppearance() }
        runCurrent()

        assertEquals(UserPreferences(), repository.observeUserPreferences().first())
        assertFalse(write.isCompleted)

        writeGate.complete(Unit)
        write.await()

        assertEquals(UserPreferences(), repository.observeUserPreferences().first())
    }

    @Test
    fun `reset write failure restores both observed values`() = runTest {
        val originalPreferences = UserPreferences(ThemeMode.Dark, AccentPreset.Amber)
        val expectedFailure = IOException("Reset failed")
        val writeGate = CompletableDeferred<Unit>()
        val dataStore = ControllablePreferencesDataStore(
            preferencesOf(
                THEME_MODE_KEY to THEME_MODE_DARK,
                ACCENT_PRESET_KEY to ACCENT_PRESET_AMBER,
            ),
        ).apply {
            gate = writeGate
            failure = expectedFailure
        }
        val repository = DataStoreUserPreferencesRepository(
            UserPreferencesDataSource(dataStore),
        )

        val write = async { runCatching { repository.resetAppearance() } }
        runCurrent()

        assertEquals(UserPreferences(), repository.observeUserPreferences().first())
        assertFalse(write.isCompleted)

        writeGate.complete(Unit)
        val failure = write.await().exceptionOrNull()

        assertTrue(failure === expectedFailure)
        assertEquals(originalPreferences, repository.observeUserPreferences().first())
    }

    private class ControllablePreferencesDataStore(
        initialPreferences: Preferences,
    ) : DataStore<Preferences> {
        private val state = MutableStateFlow(initialPreferences)

        var gate: CompletableDeferred<Unit>? = null
        var failure: Throwable? = null

        override val data: Flow<Preferences> = state

        override suspend fun updateData(
            transform: suspend (t: Preferences) -> Preferences,
        ): Preferences {
            gate?.await()
            failure?.let { throw it }
            return transform(state.value).also { updatedPreferences ->
                state.value = updatedPreferences
            }
        }
    }
}
