package dev.toothlonely.notesapp.core.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.preferencesOf
import androidx.datastore.preferences.core.stringPreferencesKey
import dev.toothlonely.notesapp.core.data.preferences.UserPreferencesDataSource.Companion.ACCENT_PRESET_AMBER
import dev.toothlonely.notesapp.core.data.preferences.UserPreferencesDataSource.Companion.ACCENT_PRESET_INDIGO
import dev.toothlonely.notesapp.core.data.preferences.UserPreferencesDataSource.Companion.ACCENT_PRESET_KEY
import dev.toothlonely.notesapp.core.data.preferences.UserPreferencesDataSource.Companion.ACCENT_PRESET_RASPBERRY
import dev.toothlonely.notesapp.core.data.preferences.UserPreferencesDataSource.Companion.ACCENT_PRESET_TEAL
import dev.toothlonely.notesapp.core.data.preferences.UserPreferencesDataSource.Companion.THEME_MODE_DARK
import dev.toothlonely.notesapp.core.data.preferences.UserPreferencesDataSource.Companion.THEME_MODE_KEY
import dev.toothlonely.notesapp.core.data.preferences.UserPreferencesDataSource.Companion.THEME_MODE_LIGHT
import dev.toothlonely.notesapp.core.data.preferences.UserPreferencesDataSource.Companion.THEME_MODE_SYSTEM
import dev.toothlonely.notesapp.core.domain.model.AccentPreset
import dev.toothlonely.notesapp.core.domain.model.ThemeMode
import dev.toothlonely.notesapp.core.domain.model.UserPreferences
import java.io.File
import java.io.IOException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class UserPreferencesDataSourceTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun `missing and unsupported values use appearance defaults`() = runTest {
        val dataStore = FakePreferencesDataStore(emptyPreferences())
        val dataSource = UserPreferencesDataSource(dataStore)

        assertEquals(UserPreferences(), dataSource.userPreferences.first())

        dataStore.updateData {
            preferencesOf(
                THEME_MODE_KEY to "unsupported-theme",
                ACCENT_PRESET_KEY to "unsupported-accent",
            )
        }

        assertEquals(UserPreferences(), dataSource.userPreferences.first())
    }

    @Test
    fun `all supported stored values map to domain models`() = runTest {
        val dataStore = FakePreferencesDataStore(emptyPreferences())
        val dataSource = UserPreferencesDataSource(dataStore)
        val expectedThemeModes = mapOf(
            THEME_MODE_SYSTEM to ThemeMode.System,
            THEME_MODE_LIGHT to ThemeMode.Light,
            THEME_MODE_DARK to ThemeMode.Dark,
        )
        val expectedAccentPresets = mapOf(
            ACCENT_PRESET_INDIGO to AccentPreset.Indigo,
            ACCENT_PRESET_TEAL to AccentPreset.Teal,
            ACCENT_PRESET_RASPBERRY to AccentPreset.Raspberry,
            ACCENT_PRESET_AMBER to AccentPreset.Amber,
        )

        expectedThemeModes.forEach { (storedValue, expected) ->
            dataStore.updateData { preferencesOf(THEME_MODE_KEY to storedValue) }
            assertEquals(expected, dataSource.userPreferences.first().themeMode)
        }
        expectedAccentPresets.forEach { (storedValue, expected) ->
            dataStore.updateData { preferencesOf(ACCENT_PRESET_KEY to storedValue) }
            assertEquals(expected, dataSource.userPreferences.first().accentPreset)
        }
    }

    @Test
    fun `setting appearance writes stable storage values`() = runTest {
        val dataStore = FakePreferencesDataStore(emptyPreferences())
        val dataSource = UserPreferencesDataSource(dataStore)

        ThemeMode.entries.forEach { themeMode ->
            dataSource.setThemeMode(themeMode)
            val expected = when (themeMode) {
                ThemeMode.System -> THEME_MODE_SYSTEM
                ThemeMode.Light -> THEME_MODE_LIGHT
                ThemeMode.Dark -> THEME_MODE_DARK
            }
            assertEquals(expected, dataStore.data.first()[THEME_MODE_KEY])
        }
        AccentPreset.entries.forEach { accentPreset ->
            dataSource.setAccentPreset(accentPreset)
            val expected = when (accentPreset) {
                AccentPreset.Indigo -> ACCENT_PRESET_INDIGO
                AccentPreset.Teal -> ACCENT_PRESET_TEAL
                AccentPreset.Raspberry -> ACCENT_PRESET_RASPBERRY
                AccentPreset.Amber -> ACCENT_PRESET_AMBER
            }
            assertEquals(expected, dataStore.data.first()[ACCENT_PRESET_KEY])
        }
    }

    @Test
    fun `reset removes only appearance keys`() = runTest {
        val unrelatedKey = stringPreferencesKey("notes_view_mode")
        val dataStore = FakePreferencesDataStore(
            preferencesOf(
                THEME_MODE_KEY to THEME_MODE_DARK,
                ACCENT_PRESET_KEY to ACCENT_PRESET_AMBER,
                unrelatedKey to "grid",
            ),
        )
        val dataSource = UserPreferencesDataSource(dataStore)

        dataSource.resetAppearance()

        val stored = dataStore.data.first()
        assertEquals(UserPreferences(), dataSource.userPreferences.first())
        assertEquals("grid", stored[unrelatedKey])
    }

    @Test
    fun `read failure is exposed to repository consumers`() = runTest {
        val expected = IOException("Preferences unavailable")
        val dataStore = FailingPreferencesDataStore(expected)
        val dataSource = UserPreferencesDataSource(dataStore)

        val failure = runCatching { dataSource.userPreferences.first() }.exceptionOrNull()

        assertTrue(failure === expected)
    }

    @Test
    fun `theme persists when real data store is recreated`() = runTest {
        val preferencesFile = File(temporaryFolder.root, "user-preferences.preferences_pb")
        val firstStoreJob = SupervisorJob()
        val firstDataSource = UserPreferencesDataSource(
            PreferenceDataStoreFactory.create(
                scope = CoroutineScope(Dispatchers.IO + firstStoreJob),
                produceFile = { preferencesFile },
            ),
        )

        try {
            firstDataSource.setThemeMode(ThemeMode.Dark)
        } finally {
            firstStoreJob.cancelAndJoin()
        }

        val secondStoreJob = SupervisorJob()
        val secondDataSource = UserPreferencesDataSource(
            PreferenceDataStoreFactory.create(
                scope = CoroutineScope(Dispatchers.IO + secondStoreJob),
                produceFile = { preferencesFile },
            ),
        )

        try {
            assertEquals(
                UserPreferences(themeMode = ThemeMode.Dark),
                secondDataSource.userPreferences.first(),
            )
        } finally {
            secondStoreJob.cancelAndJoin()
        }
    }

    private class FakePreferencesDataStore(
        initialPreferences: Preferences,
    ) : DataStore<Preferences> {
        private val state = MutableStateFlow(initialPreferences)

        override val data: Flow<Preferences> = state

        override suspend fun updateData(
            transform: suspend (t: Preferences) -> Preferences,
        ): Preferences = transform(state.value).also { updatedPreferences ->
            state.value = updatedPreferences
        }
    }

    private class FailingPreferencesDataStore(
        failure: Throwable,
    ) : DataStore<Preferences> {
        override val data: Flow<Preferences> = flow { throw failure }

        override suspend fun updateData(
            transform: suspend (t: Preferences) -> Preferences,
        ): Preferences = error("Not used")
    }
}
