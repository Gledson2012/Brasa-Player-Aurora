package com.example.data.datastore

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.model.AlbumArtStyle
import com.example.data.model.AppThemeType
import com.example.data.model.CustomThemeConfig
import com.example.data.model.ThemeConfig
import com.example.data.model.ThemeMode
import com.example.data.model.VisualizerStyle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

val Context.themeDataStore: DataStore<Preferences> by preferencesDataStore(name = "theme_preferences")

class ThemePreferencesDataStore(private val context: Context) {

    private object PreferencesKeys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val PRESET_THEME = stringPreferencesKey("preset_theme")
        val DYNAMIC_COLORS = booleanPreferencesKey("dynamic_colors")
        val VISUALIZER_STYLE = stringPreferencesKey("visualizer_style")
        val ALBUM_ART_STYLE = stringPreferencesKey("album_art_style")

        // Custom Theme Colors
        val CUSTOM_PRIMARY = longPreferencesKey("custom_primary_color")
        val CUSTOM_SECONDARY = longPreferencesKey("custom_secondary_color")
        val CUSTOM_TERTIARY = longPreferencesKey("custom_tertiary_color")
        val CUSTOM_SURFACE = longPreferencesKey("custom_surface_color")
        val CUSTOM_BACKGROUND = longPreferencesKey("custom_background_color")
        val CUSTOM_IS_DARK = booleanPreferencesKey("custom_is_dark")
    }

    val themeConfigFlow: Flow<ThemeConfig> = context.themeDataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val themeModeString = preferences[PreferencesKeys.THEME_MODE] ?: ThemeMode.SYSTEM.name
            val themeMode = try {
                ThemeMode.valueOf(themeModeString)
            } catch (e: Exception) {
                ThemeMode.SYSTEM
            }

            val presetThemeString = preferences[PreferencesKeys.PRESET_THEME] ?: AppThemeType.MIDNIGHT_OLED.name
            val presetTheme = try {
                AppThemeType.valueOf(presetThemeString)
            } catch (e: Exception) {
                AppThemeType.MIDNIGHT_OLED
            }

            val dynamicColors = preferences[PreferencesKeys.DYNAMIC_COLORS] ?: false

            val visualizerString = preferences[PreferencesKeys.VISUALIZER_STYLE] ?: VisualizerStyle.BARS.name
            val visualizerStyle = try {
                VisualizerStyle.valueOf(visualizerString)
            } catch (e: Exception) {
                VisualizerStyle.BARS
            }

            val albumArtString = preferences[PreferencesKeys.ALBUM_ART_STYLE] ?: AlbumArtStyle.VINYL_ROTATION.name
            val albumArtStyle = try {
                AlbumArtStyle.valueOf(albumArtString)
            } catch (e: Exception) {
                AlbumArtStyle.VINYL_ROTATION
            }

            val customPrimary = preferences[PreferencesKeys.CUSTOM_PRIMARY] ?: 0xFF9D4EDDL
            val customSecondary = preferences[PreferencesKeys.CUSTOM_SECONDARY] ?: 0xFF00F0FFL
            val customTertiary = preferences[PreferencesKeys.CUSTOM_TERTIARY] ?: 0xFFFF007FL
            val customSurface = preferences[PreferencesKeys.CUSTOM_SURFACE] ?: 0xFF140F22L
            val customBackground = preferences[PreferencesKeys.CUSTOM_BACKGROUND] ?: 0xFF080512L
            val customIsDark = preferences[PreferencesKeys.CUSTOM_IS_DARK] ?: true

            val customTheme = CustomThemeConfig(
                primaryColorVal = customPrimary,
                secondaryColorVal = customSecondary,
                tertiaryColorVal = customTertiary,
                surfaceColorVal = customSurface,
                backgroundColorVal = customBackground,
                isDark = customIsDark
            )

            ThemeConfig(
                themeMode = themeMode,
                presetTheme = presetTheme,
                customTheme = customTheme,
                dynamicColors = dynamicColors,
                visualizerStyle = visualizerStyle,
                albumArtStyle = albumArtStyle
            )
        }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.themeDataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_MODE] = mode.name
        }
    }

    suspend fun setPresetTheme(preset: AppThemeType) {
        context.themeDataStore.edit { preferences ->
            preferences[PreferencesKeys.PRESET_THEME] = preset.name
        }
    }

    suspend fun setDynamicColors(enabled: Boolean) {
        context.themeDataStore.edit { preferences ->
            preferences[PreferencesKeys.DYNAMIC_COLORS] = enabled
        }
    }

    suspend fun setVisualizerStyle(style: VisualizerStyle) {
        context.themeDataStore.edit { preferences ->
            preferences[PreferencesKeys.VISUALIZER_STYLE] = style.name
        }
    }

    suspend fun setAlbumArtStyle(style: AlbumArtStyle) {
        context.themeDataStore.edit { preferences ->
            preferences[PreferencesKeys.ALBUM_ART_STYLE] = style.name
        }
    }

    suspend fun setCustomTheme(
        primary: Color,
        secondary: Color,
        tertiary: Color = Color(0xFFFF007F),
        surface: Color,
        background: Color,
        isDark: Boolean = true
    ) {
        context.themeDataStore.edit { preferences ->
            preferences[PreferencesKeys.CUSTOM_PRIMARY] = primary.value.toLong()
            preferences[PreferencesKeys.CUSTOM_SECONDARY] = secondary.value.toLong()
            preferences[PreferencesKeys.CUSTOM_TERTIARY] = tertiary.value.toLong()
            preferences[PreferencesKeys.CUSTOM_SURFACE] = surface.value.toLong()
            preferences[PreferencesKeys.CUSTOM_BACKGROUND] = background.value.toLong()
            preferences[PreferencesKeys.CUSTOM_IS_DARK] = isDark
        }
    }

    suspend fun restore(config: ThemeConfig) {
        context.themeDataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_MODE] = config.themeMode.name
            preferences[PreferencesKeys.PRESET_THEME] = config.presetTheme.name
            preferences[PreferencesKeys.DYNAMIC_COLORS] = config.dynamicColors
            preferences[PreferencesKeys.VISUALIZER_STYLE] = config.visualizerStyle.name
            preferences[PreferencesKeys.ALBUM_ART_STYLE] = config.albumArtStyle.name
            preferences[PreferencesKeys.CUSTOM_PRIMARY] = config.customTheme.primaryColorVal
            preferences[PreferencesKeys.CUSTOM_SECONDARY] = config.customTheme.secondaryColorVal
            preferences[PreferencesKeys.CUSTOM_TERTIARY] = config.customTheme.tertiaryColorVal
            preferences[PreferencesKeys.CUSTOM_SURFACE] = config.customTheme.surfaceColorVal
            preferences[PreferencesKeys.CUSTOM_BACKGROUND] = config.customTheme.backgroundColorVal
            preferences[PreferencesKeys.CUSTOM_IS_DARK] = config.customTheme.isDark
        }
    }

    suspend fun resetToDefault() {
        context.themeDataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
