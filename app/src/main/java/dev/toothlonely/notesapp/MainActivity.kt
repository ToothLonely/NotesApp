package dev.toothlonely.notesapp

import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dev.toothlonely.notesapp.ui.AppThemeViewModel
import dev.toothlonely.notesapp.ui.NotesApp
import dev.toothlonely.notesapp.ui.shouldUseDarkTheme
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : ComponentActivity() {
    private val appThemeViewModel: AppThemeViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        observeThemeForSystemBars()
        setContent {
            NotesApp(appThemeViewModel = appThemeViewModel)
        }
    }

    private fun observeThemeForSystemBars() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                appThemeViewModel.state
                    .map { preferences ->
                        preferences.themeMode.shouldUseDarkTheme(
                            systemInDarkTheme = resources.configuration.isSystemInDarkTheme,
                        )
                    }
                    .distinctUntilChanged()
                    .collect(::applyEdgeToEdgeForTheme)
            }
        }
    }

    private fun applyEdgeToEdgeForTheme(useDarkTheme: Boolean) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
                lightScrim = Color.TRANSPARENT,
                darkScrim = Color.TRANSPARENT,
            ) { useDarkTheme },
            navigationBarStyle = SystemBarStyle.auto(
                lightScrim = LIGHT_NAVIGATION_BAR_SCRIM,
                darkScrim = DARK_NAVIGATION_BAR_SCRIM,
            ) { useDarkTheme },
        )
    }
}

private val Configuration.isSystemInDarkTheme: Boolean
    get() = uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES

private val LIGHT_NAVIGATION_BAR_SCRIM = Color.argb(0xE6, 0xFF, 0xFF, 0xFF)
private val DARK_NAVIGATION_BAR_SCRIM = Color.argb(0x80, 0x1B, 0x1B, 0x1B)
