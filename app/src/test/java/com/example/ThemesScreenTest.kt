package com.example

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.example.data.model.AlbumArtStyle
import com.example.data.model.AppThemeType
import com.example.data.model.CustomThemeConfig
import com.example.data.model.ThemeConfig
import com.example.data.model.ThemeMode
import com.example.data.model.VisualizerStyle
import com.example.ui.screens.ThemesScreen
import com.example.ui.theme.MusicPlayerTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ThemesScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val defaultThemeConfig = ThemeConfig(
        themeMode = ThemeMode.SYSTEM,
        presetTheme = AppThemeType.MIDNIGHT_OLED,
        customTheme = CustomThemeConfig(),
        dynamicColors = false,
        visualizerStyle = VisualizerStyle.BARS,
        albumArtStyle = AlbumArtStyle.VINYL_ROTATION
    )

    @Test
    fun `ThemesScreen renders all header and initial controls`() {
        composeTestRule.setContent {
            MusicPlayerTheme {
                ThemesScreen(
                    themeConfig = defaultThemeConfig,
                    crossfadeSeconds = 3,
                    scanStatusMessage = null,
                    onSelectThemeMode = {},
                    onSelectPresetTheme = {},
                    onSaveCustomTheme = { _, _, _, _, _, _ -> },
                    onToggleDynamicColors = {},
                    onSelectVisualizerStyle = {},
                    onSelectAlbumArtStyle = {},
                    onSetCrossfadeSeconds = {},
                    onResetDefaults = {},
                    onScanLocalStorage = {},
                    onImportAudioFile = { _, _, _ -> },
                    onImportAudioFolder = {},
                    onOpenLastFm = {},
                    onBackup = {},
                    onRestore = {}
                )
            }
        }

        // Header & Active theme summary
        composeTestRule.onNodeWithTag("themes_screen").assertExists()
        composeTestRule.onNodeWithText("Temas & Estilo").assertIsDisplayed()
        composeTestRule.onNodeWithTag("reset_theme_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("active_theme_summary").assertIsDisplayed()

        // Appearance Mode selection cards
        composeTestRule.onNodeWithTag("theme_mode_system").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("theme_mode_light").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("theme_mode_dark").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("theme_mode_custom").performScrollTo().assertIsDisplayed()

        // Dynamic colors card
        composeTestRule.onNodeWithTag("dynamic_colors_card").performScrollTo().assertIsDisplayed()

        // Scan button
        composeTestRule.onNodeWithTag("scan_storage_button").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun `ThemesScreen clicking theme modes triggers onSelectThemeMode`() {
        var selectedMode: ThemeMode? = null

        composeTestRule.setContent {
            MusicPlayerTheme {
                ThemesScreen(
                    themeConfig = defaultThemeConfig,
                    crossfadeSeconds = 3,
                    scanStatusMessage = null,
                    onSelectThemeMode = { selectedMode = it },
                    onSelectPresetTheme = {},
                    onSaveCustomTheme = { _, _, _, _, _, _ -> },
                    onToggleDynamicColors = {},
                    onSelectVisualizerStyle = {},
                    onSelectAlbumArtStyle = {},
                    onSetCrossfadeSeconds = {},
                    onResetDefaults = {},
                    onScanLocalStorage = {},
                    onImportAudioFile = { _, _, _ -> },
                    onImportAudioFolder = {},
                    onOpenLastFm = {},
                    onBackup = {},
                    onRestore = {}
                )
            }
        }

        composeTestRule.onNodeWithTag("theme_mode_dark").performScrollTo().performClick()
        assertEquals(ThemeMode.DARK, selectedMode)

        composeTestRule.onNodeWithTag("theme_mode_light").performScrollTo().performClick()
        assertEquals(ThemeMode.LIGHT, selectedMode)

        composeTestRule.onNodeWithTag("theme_mode_custom").performScrollTo().performClick()
        assertEquals(ThemeMode.CUSTOM, selectedMode)

        composeTestRule.onNodeWithTag("theme_mode_system").performScrollTo().performClick()
        assertEquals(ThemeMode.SYSTEM, selectedMode)
    }

    @Test
    fun `ThemesScreen selecting preset theme triggers onSelectPresetTheme`() {
        var selectedTheme: AppThemeType? = null

        composeTestRule.setContent {
            MusicPlayerTheme {
                ThemesScreen(
                    themeConfig = defaultThemeConfig,
                    crossfadeSeconds = 3,
                    scanStatusMessage = null,
                    onSelectThemeMode = {},
                    onSelectPresetTheme = { selectedTheme = it },
                    onSaveCustomTheme = { _, _, _, _, _, _ -> },
                    onToggleDynamicColors = {},
                    onSelectVisualizerStyle = {},
                    onSelectAlbumArtStyle = {},
                    onSetCrossfadeSeconds = {},
                    onResetDefaults = {},
                    onScanLocalStorage = {},
                    onImportAudioFile = { _, _, _ -> },
                    onImportAudioFolder = {},
                    onOpenLastFm = {},
                    onBackup = {},
                    onRestore = {}
                )
            }
        }

        composeTestRule.onNodeWithTag("theme_option_${AppThemeType.CYBERPUNK_NEON.name}")
            .performScrollTo()
            .performClick()

        assertEquals(AppThemeType.CYBERPUNK_NEON, selectedTheme)
    }

    @Test
    fun `ThemesScreen toggling dynamic colors triggers onToggleDynamicColors`() {
        var dynamicToggled: Boolean? = null

        composeTestRule.setContent {
            MusicPlayerTheme {
                ThemesScreen(
                    themeConfig = defaultThemeConfig.copy(dynamicColors = false),
                    crossfadeSeconds = 0,
                    scanStatusMessage = null,
                    onSelectThemeMode = {},
                    onSelectPresetTheme = {},
                    onSaveCustomTheme = { _, _, _, _, _, _ -> },
                    onToggleDynamicColors = { dynamicToggled = it },
                    onSelectVisualizerStyle = {},
                    onSelectAlbumArtStyle = {},
                    onSetCrossfadeSeconds = {},
                    onResetDefaults = {},
                    onScanLocalStorage = {},
                    onImportAudioFile = { _, _, _ -> },
                    onImportAudioFolder = {},
                    onOpenLastFm = {},
                    onBackup = {},
                    onRestore = {}
                )
            }
        }

        composeTestRule.onNodeWithTag("dynamic_colors_switch")
            .performScrollTo()
            .performClick()

        assertEquals(true, dynamicToggled)
    }

    @Test
    fun `ThemesScreen selecting crossfade seconds calls onSetCrossfadeSeconds`() {
        var chosenSeconds: Int? = null

        composeTestRule.setContent {
            MusicPlayerTheme {
                ThemesScreen(
                    themeConfig = defaultThemeConfig,
                    crossfadeSeconds = 0,
                    scanStatusMessage = null,
                    onSelectThemeMode = {},
                    onSelectPresetTheme = {},
                    onSaveCustomTheme = { _, _, _, _, _, _ -> },
                    onToggleDynamicColors = {},
                    onSelectVisualizerStyle = {},
                    onSelectAlbumArtStyle = {},
                    onSetCrossfadeSeconds = { chosenSeconds = it },
                    onResetDefaults = {},
                    onScanLocalStorage = {},
                    onImportAudioFile = { _, _, _ -> },
                    onImportAudioFolder = {},
                    onOpenLastFm = {},
                    onBackup = {},
                    onRestore = {}
                )
            }
        }

        composeTestRule.onNodeWithText("5s").performScrollTo().performClick()
        assertEquals(5, chosenSeconds)
    }

    @Test
    fun `ThemesScreen selecting visualizer style calls onSelectVisualizerStyle`() {
        var selectedStyle: VisualizerStyle? = null

        composeTestRule.setContent {
            MusicPlayerTheme {
                ThemesScreen(
                    themeConfig = defaultThemeConfig,
                    crossfadeSeconds = 0,
                    scanStatusMessage = null,
                    onSelectThemeMode = {},
                    onSelectPresetTheme = {},
                    onSaveCustomTheme = { _, _, _, _, _, _ -> },
                    onToggleDynamicColors = {},
                    onSelectVisualizerStyle = { selectedStyle = it },
                    onSelectAlbumArtStyle = {},
                    onSetCrossfadeSeconds = {},
                    onResetDefaults = {},
                    onScanLocalStorage = {},
                    onImportAudioFile = { _, _, _ -> },
                    onImportAudioFolder = {},
                    onOpenLastFm = {},
                    onBackup = {},
                    onRestore = {}
                )
            }
        }

        composeTestRule.onNodeWithText(VisualizerStyle.WAVEFORM.title)
            .performScrollTo()
            .performClick()

        assertEquals(VisualizerStyle.WAVEFORM, selectedStyle)
    }

    @Test
    fun `ThemesScreen selecting album art style calls onSelectAlbumArtStyle`() {
        var selectedStyle: AlbumArtStyle? = null

        composeTestRule.setContent {
            MusicPlayerTheme {
                ThemesScreen(
                    themeConfig = defaultThemeConfig,
                    crossfadeSeconds = 0,
                    scanStatusMessage = null,
                    onSelectThemeMode = {},
                    onSelectPresetTheme = {},
                    onSaveCustomTheme = { _, _, _, _, _, _ -> },
                    onToggleDynamicColors = {},
                    onSelectVisualizerStyle = {},
                    onSelectAlbumArtStyle = { selectedStyle = it },
                    onSetCrossfadeSeconds = {},
                    onResetDefaults = {},
                    onScanLocalStorage = {},
                    onImportAudioFile = { _, _, _ -> },
                    onImportAudioFolder = {},
                    onOpenLastFm = {},
                    onBackup = {},
                    onRestore = {}
                )
            }
        }

        composeTestRule.onNodeWithText(AlbumArtStyle.CARD_ROUNDED.title)
            .performScrollTo()
            .performClick()

        assertEquals(AlbumArtStyle.CARD_ROUNDED, selectedStyle)
    }

    @Test
    fun `ThemesScreen in CUSTOM mode displays custom studio and saves custom theme`() {
        var savedCustomTheme: Boolean = false
        var savedPrimary: Color? = null
        var isDarkTheme: Boolean? = null

        composeTestRule.setContent {
            MusicPlayerTheme {
                ThemesScreen(
                    themeConfig = defaultThemeConfig.copy(themeMode = ThemeMode.CUSTOM),
                    crossfadeSeconds = 0,
                    scanStatusMessage = null,
                    onSelectThemeMode = {},
                    onSelectPresetTheme = {},
                    onSaveCustomTheme = { primary, _, _, _, _, isDark ->
                        savedCustomTheme = true
                        savedPrimary = primary
                        isDarkTheme = isDark
                    },
                    onToggleDynamicColors = {},
                    onSelectVisualizerStyle = {},
                    onSelectAlbumArtStyle = {},
                    onSetCrossfadeSeconds = {},
                    onResetDefaults = {},
                    onScanLocalStorage = {},
                    onImportAudioFile = { _, _, _ -> },
                    onImportAudioFolder = {},
                    onOpenLastFm = {},
                    onBackup = {},
                    onRestore = {}
                )
            }
        }

        // Studio card should be visible
        composeTestRule.onNodeWithTag("custom_theme_studio_card").performScrollTo().assertIsDisplayed()

        // Click Save Custom Theme
        composeTestRule.onNodeWithTag("apply_custom_theme_button").performScrollTo().performClick()

        assertTrue(savedCustomTheme)
        assertNotNull(savedPrimary)
        assertEquals(true, isDarkTheme)
    }

    @Test
    fun `ThemesScreen clicking reset defaults calls onResetDefaults`() {
        var resetCalled = false

        composeTestRule.setContent {
            MusicPlayerTheme {
                ThemesScreen(
                    themeConfig = defaultThemeConfig,
                    crossfadeSeconds = 3,
                    scanStatusMessage = null,
                    onSelectThemeMode = {},
                    onSelectPresetTheme = {},
                    onSaveCustomTheme = { _, _, _, _, _, _ -> },
                    onToggleDynamicColors = {},
                    onSelectVisualizerStyle = {},
                    onSelectAlbumArtStyle = {},
                    onSetCrossfadeSeconds = {},
                    onResetDefaults = { resetCalled = true },
                    onScanLocalStorage = {},
                    onImportAudioFile = { _, _, _ -> },
                    onImportAudioFolder = {},
                    onOpenLastFm = {},
                    onBackup = {},
                    onRestore = {}
                )
            }
        }

        composeTestRule.onNodeWithTag("reset_theme_button").performClick()
        assertTrue(resetCalled)
    }

    @Test
    fun `ThemesScreen clicking scan storage calls onScanLocalStorage`() {
        var scanCalled = false

        composeTestRule.setContent {
            MusicPlayerTheme {
                ThemesScreen(
                    themeConfig = defaultThemeConfig,
                    crossfadeSeconds = 3,
                    scanStatusMessage = null,
                    onSelectThemeMode = {},
                    onSelectPresetTheme = {},
                    onSaveCustomTheme = { _, _, _, _, _, _ -> },
                    onToggleDynamicColors = {},
                    onSelectVisualizerStyle = {},
                    onSelectAlbumArtStyle = {},
                    onSetCrossfadeSeconds = {},
                    onResetDefaults = {},
                    onScanLocalStorage = { scanCalled = true },
                    onImportAudioFile = { _, _, _ -> },
                    onImportAudioFolder = {},
                    onOpenLastFm = {},
                    onBackup = {},
                    onRestore = {}
                )
            }
        }

        composeTestRule.onNodeWithTag("scan_storage_button").performScrollTo().performClick()
        assertTrue(scanCalled)
    }

    @Test
    fun `ThemesScreen displays scanStatusMessage when present`() {
        val testStatus = "35 músicas importadas da pasta"

        composeTestRule.setContent {
            MusicPlayerTheme {
                ThemesScreen(
                    themeConfig = defaultThemeConfig,
                    crossfadeSeconds = 3,
                    scanStatusMessage = testStatus,
                    onSelectThemeMode = {},
                    onSelectPresetTheme = {},
                    onSaveCustomTheme = { _, _, _, _, _, _ -> },
                    onToggleDynamicColors = {},
                    onSelectVisualizerStyle = {},
                    onSelectAlbumArtStyle = {},
                    onSetCrossfadeSeconds = {},
                    onResetDefaults = {},
                    onScanLocalStorage = {},
                    onImportAudioFile = { _, _, _ -> },
                    onImportAudioFolder = {},
                    onOpenLastFm = {},
                    onBackup = {},
                    onRestore = {}
                )
            }
        }

        composeTestRule.onNodeWithText(testStatus).performScrollTo().assertIsDisplayed()
    }

    @Test
    fun `ThemesScreen clicking LastFM and Backup and Restore calls callbacks`() {
        var lastFmCalled = false
        var backupCalled = false
        var restoreCalled = false

        composeTestRule.setContent {
            MusicPlayerTheme {
                ThemesScreen(
                    themeConfig = defaultThemeConfig,
                    crossfadeSeconds = 3,
                    scanStatusMessage = null,
                    onSelectThemeMode = {},
                    onSelectPresetTheme = {},
                    onSaveCustomTheme = { _, _, _, _, _, _ -> },
                    onToggleDynamicColors = {},
                    onSelectVisualizerStyle = {},
                    onSelectAlbumArtStyle = {},
                    onSetCrossfadeSeconds = {},
                    onResetDefaults = {},
                    onScanLocalStorage = {},
                    onImportAudioFile = { _, _, _ -> },
                    onImportAudioFolder = {},
                    onOpenLastFm = { lastFmCalled = true },
                    onBackup = { backupCalled = true },
                    onRestore = { restoreCalled = true }
                )
            }
        }

        composeTestRule.onNodeWithText("Configurar Conta Last.fm").performScrollTo().performClick()
        assertTrue(lastFmCalled)

        composeTestRule.onNodeWithText("Exportar").performScrollTo().performClick()
        assertTrue(backupCalled)

        composeTestRule.onNodeWithText("Restaurar").performScrollTo().performClick()
        assertTrue(restoreCalled)
    }
}
