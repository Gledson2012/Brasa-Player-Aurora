package com.example

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import com.example.data.model.EqualizerPreset
import com.example.data.model.EqualizerState
import com.example.ui.screens.EqualizerScreen
import com.example.ui.theme.MusicPlayerTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class EqualizerScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val defaultState = EqualizerState(
        isEnabled = true,
        currentPresetId = "flat",
        bandLevels = listOf(3, 0, -2, 4, 1),
        bassBoost = 30,
        virtualizer = 20,
        balance = 0.4f
    )

    @Test
    fun `EqualizerScreen renders all main components and test tags`() {
        composeTestRule.setContent {
            MusicPlayerTheme {
                EqualizerScreen(
                    equalizerState = defaultState,
                    isPlaying = true,
                    visualizerAmplitudes = FloatArray(30) { 0.5f },
                    onToggleEnabled = {},
                    onSelectPreset = {},
                    onBandGainChange = { _, _ -> },
                    onBassBoostChange = {},
                    onVirtualizerChange = {},
                    onBalanceChange = {},
                    onReset = {},
                    onSaveCustomPreset = {},
                    onDeleteCustomPreset = {}
                )
            }
        }

        // Header & Master switch
        composeTestRule.onNodeWithTag("equalizer_screen").assertExists()
        composeTestRule.onNodeWithText("Equalizador").assertIsDisplayed()
        composeTestRule.onNodeWithTag("equalizer_switch").assertIsDisplayed()
        composeTestRule.onNodeWithTag("clipping_protection_card").assertIsDisplayed()

        // Presets
        composeTestRule.onNodeWithTag("save_preset_button").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("presets_row").performScrollTo().assertIsDisplayed()

        // Frequency bands
        composeTestRule.onNodeWithTag("frequency_band_0").performScrollTo().assertExists()
        composeTestRule.onNodeWithTag("slider_band_0").performScrollTo().assertExists()
        composeTestRule.onNodeWithTag("frequency_band_4").performScrollTo().assertExists()

        // Sound effects
        composeTestRule.onNodeWithTag("bass_boost_slider").performScrollTo().assertExists()
        composeTestRule.onNodeWithTag("virtualizer_slider").performScrollTo().assertExists()
        composeTestRule.onNodeWithTag("balance_slider").performScrollTo().assertExists()

        // Reset button
        composeTestRule.onNodeWithTag("reset_equalizer_button").performScrollTo().assertExists()
    }

    @Test
    fun `EqualizerScreen toggle master switch calls onToggleEnabled`() {
        var toggledValue: Boolean? = null

        composeTestRule.setContent {
            MusicPlayerTheme {
                EqualizerScreen(
                    equalizerState = defaultState,
                    isPlaying = false,
                    visualizerAmplitudes = FloatArray(30),
                    onToggleEnabled = { toggledValue = it },
                    onSelectPreset = {},
                    onBandGainChange = { _, _ -> },
                    onBassBoostChange = {},
                    onVirtualizerChange = {},
                    onBalanceChange = {},
                    onReset = {},
                    onSaveCustomPreset = {},
                    onDeleteCustomPreset = {}
                )
            }
        }

        composeTestRule.onNodeWithTag("equalizer_switch").performClick()
        assertEquals(false, toggledValue)
    }

    @Test
    fun `EqualizerScreen selecting a preset calls onSelectPreset`() {
        var selectedPreset: EqualizerPreset? = null

        composeTestRule.setContent {
            MusicPlayerTheme {
                EqualizerScreen(
                    equalizerState = defaultState,
                    isPlaying = false,
                    visualizerAmplitudes = FloatArray(30),
                    onToggleEnabled = {},
                    onSelectPreset = { selectedPreset = it },
                    onBandGainChange = { _, _ -> },
                    onBassBoostChange = {},
                    onVirtualizerChange = {},
                    onBalanceChange = {},
                    onReset = {},
                    onSaveCustomPreset = {},
                    onDeleteCustomPreset = {}
                )
            }
        }

        // Scroll presets row vertically into view first
        composeTestRule.onNodeWithTag("presets_row").performScrollTo()
        // Click preset chip
        composeTestRule.onNodeWithTag("preset_chip_headphones").performClick()
        assertEquals("headphones", selectedPreset?.id)
    }

    @Test
    fun `EqualizerScreen clicking band 0 dB reset calls onBandGainChange with 0`() {
        var changedBand: Int? = null
        var changedGain: Int? = null

        composeTestRule.setContent {
            MusicPlayerTheme {
                EqualizerScreen(
                    equalizerState = defaultState, // bandLevels: [3, 0, -2, 4, 1]
                    isPlaying = false,
                    visualizerAmplitudes = FloatArray(30),
                    onToggleEnabled = {},
                    onSelectPreset = {},
                    onBandGainChange = { band, gain ->
                        changedBand = band
                        changedGain = gain
                    },
                    onBassBoostChange = {},
                    onVirtualizerChange = {},
                    onBalanceChange = {},
                    onReset = {},
                    onSaveCustomPreset = {},
                    onDeleteCustomPreset = {}
                )
            }
        }

        // Band 0 has +3 dB gain, so "0 dB" pill appears
        composeTestRule.onAllNodesWithText("0 dB")[0].performScrollTo().performClick()
        assertEquals(0, changedBand)
        assertEquals(0, changedGain)
    }

    @Test
    fun `EqualizerScreen micro step buttons increment and decrement band level`() {
        var changedBand: Int? = null
        var changedGain: Int? = null

        composeTestRule.setContent {
            MusicPlayerTheme {
                EqualizerScreen(
                    equalizerState = defaultState, // bandLevels: [3, 0, -2, 4, 1]
                    isPlaying = false,
                    visualizerAmplitudes = FloatArray(30),
                    onToggleEnabled = {},
                    onSelectPreset = {},
                    onBandGainChange = { band, gain ->
                        changedBand = band
                        changedGain = gain
                    },
                    onBassBoostChange = {},
                    onVirtualizerChange = {},
                    onBalanceChange = {},
                    onReset = {},
                    onSaveCustomPreset = {},
                    onDeleteCustomPreset = {}
                )
            }
        }

        // Test increment on band 0 (+1 dB -> from 3 to 4)
        composeTestRule.onAllNodesWithContentDescription("Aumentar 1 dB")[0].performScrollTo().performClick()
        assertEquals(0, changedBand)
        assertEquals(4, changedGain)

        // Test decrement on band 0 (-1 dB -> from 3 to 2)
        composeTestRule.onAllNodesWithContentDescription("Diminuir 1 dB")[0].performScrollTo().performClick()
        assertEquals(0, changedBand)
        assertEquals(2, changedGain)
    }

    @Test
    fun `EqualizerScreen balance centering button resets balance to 0`() {
        var newBalance: Float? = null

        composeTestRule.setContent {
            MusicPlayerTheme {
                EqualizerScreen(
                    equalizerState = defaultState, // balance = 0.4f
                    isPlaying = false,
                    visualizerAmplitudes = FloatArray(30),
                    onToggleEnabled = {},
                    onSelectPreset = {},
                    onBandGainChange = { _, _ -> },
                    onBassBoostChange = {},
                    onVirtualizerChange = {},
                    onBalanceChange = { newBalance = it },
                    onReset = {},
                    onSaveCustomPreset = {},
                    onDeleteCustomPreset = {}
                )
            }
        }

        // Balance is 0.4f, so "Centralizar" button should appear
        composeTestRule.onNodeWithText("Centralizar").performScrollTo().assertIsDisplayed().performClick()
        assertEquals(0f, newBalance)
    }

    @Test
    fun `EqualizerScreen reset button calls onReset`() {
        var resetCalled = false

        composeTestRule.setContent {
            MusicPlayerTheme {
                EqualizerScreen(
                    equalizerState = defaultState,
                    isPlaying = false,
                    visualizerAmplitudes = FloatArray(30),
                    onToggleEnabled = {},
                    onSelectPreset = {},
                    onBandGainChange = { _, _ -> },
                    onBassBoostChange = {},
                    onVirtualizerChange = {},
                    onBalanceChange = {},
                    onReset = { resetCalled = true },
                    onSaveCustomPreset = {},
                    onDeleteCustomPreset = {}
                )
            }
        }

        composeTestRule.onNodeWithTag("reset_equalizer_button").performScrollTo().performClick()
        assertTrue("onReset should have been called", resetCalled)
    }

    @Test
    fun `EqualizerScreen save preset button opens save dialog`() {
        composeTestRule.setContent {
            MusicPlayerTheme {
                EqualizerScreen(
                    equalizerState = defaultState,
                    isPlaying = false,
                    visualizerAmplitudes = FloatArray(30),
                    onToggleEnabled = {},
                    onSelectPreset = {},
                    onBandGainChange = { _, _ -> },
                    onBassBoostChange = {},
                    onVirtualizerChange = {},
                    onBalanceChange = {},
                    onReset = {},
                    onSaveCustomPreset = {},
                    onDeleteCustomPreset = {}
                )
            }
        }

        composeTestRule.onNodeWithTag("save_preset_button").performScrollTo().performClick()
        composeTestRule.onNodeWithTag("save_preset_dialog").assertIsDisplayed()
        composeTestRule.onNodeWithText("Salvar Preset Personalizado").assertIsDisplayed()
    }
}
