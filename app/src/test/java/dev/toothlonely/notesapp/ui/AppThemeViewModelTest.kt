package dev.toothlonely.notesapp.ui

import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppAccentPalette
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppThemeMode
import dev.toothlonely.notesapp.core.domain.model.AccentPreset
import dev.toothlonely.notesapp.core.domain.model.ThemeMode
import dev.toothlonely.notesapp.core.domain.model.UserPreferences
import dev.toothlonely.notesapp.core.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AppThemeViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `stored preferences reactively update app theme state`() = runTest(dispatcher) {
        val repository = FakeUserPreferencesRepository()
        val viewModel = AppThemeViewModel(repository)
        runCurrent()

        repository.preferences.value = UserPreferences(
            themeMode = ThemeMode.Dark,
            accentPreset = AccentPreset.Raspberry,
        )
        runCurrent()

        assertEquals(
            UserPreferences(ThemeMode.Dark, AccentPreset.Raspberry),
            viewModel.state.value,
        )
    }

    @Test
    fun `all domain appearance values map to design system values`() {
        assertEquals(NotesAppThemeMode.System, ThemeMode.System.toDesignSystemThemeMode())
        assertEquals(NotesAppThemeMode.Light, ThemeMode.Light.toDesignSystemThemeMode())
        assertEquals(NotesAppThemeMode.Dark, ThemeMode.Dark.toDesignSystemThemeMode())
        assertEquals(
            NotesAppAccentPalette.Indigo,
            AccentPreset.Indigo.toDesignSystemAccentPalette(),
        )
        assertEquals(
            NotesAppAccentPalette.Teal,
            AccentPreset.Teal.toDesignSystemAccentPalette(),
        )
        assertEquals(
            NotesAppAccentPalette.Raspberry,
            AccentPreset.Raspberry.toDesignSystemAccentPalette(),
        )
        assertEquals(
            NotesAppAccentPalette.Amber,
            AccentPreset.Amber.toDesignSystemAccentPalette(),
        )
    }

    @Test
    fun `theme mode resolves system bar appearance against system theme`() {
        assertFalse(ThemeMode.System.shouldUseDarkTheme(systemInDarkTheme = false))
        assertTrue(ThemeMode.System.shouldUseDarkTheme(systemInDarkTheme = true))
        assertFalse(ThemeMode.Light.shouldUseDarkTheme(systemInDarkTheme = false))
        assertFalse(ThemeMode.Light.shouldUseDarkTheme(systemInDarkTheme = true))
        assertTrue(ThemeMode.Dark.shouldUseDarkTheme(systemInDarkTheme = false))
        assertTrue(ThemeMode.Dark.shouldUseDarkTheme(systemInDarkTheme = true))
    }

    private class FakeUserPreferencesRepository : UserPreferencesRepository {
        val preferences = MutableStateFlow(UserPreferences())

        override fun observeUserPreferences(): Flow<UserPreferences> = preferences

        override suspend fun setThemeMode(themeMode: ThemeMode) = Unit

        override suspend fun setAccentPreset(accentPreset: AccentPreset) = Unit

        override suspend fun resetAppearance() = Unit
    }
}
