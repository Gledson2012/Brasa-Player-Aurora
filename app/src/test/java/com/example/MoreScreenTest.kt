package com.example

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.ui.screens.MoreScreen
import com.example.ui.theme.MusicPlayerTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MoreScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `MoreScreen exposes secondary tools and opens selected destinations`() {
        var equalizerOpened = false
        var themesOpened = false
        var statisticsOpened = false
        var browserOpened = false

        composeTestRule.setContent {
            MusicPlayerTheme {
                MoreScreen(
                    onOpenEqualizer = { equalizerOpened = true },
                    onOpenThemes = { themesOpened = true },
                    onOpenStatistics = { statisticsOpened = true },
                    onOpenBrowser = { browserOpened = true }
                )
            }
        }

        composeTestRule.onNodeWithText("Mais").assertIsDisplayed()
        composeTestRule.onNodeWithText("Equalizador").performClick()
        composeTestRule.onNodeWithText("Aparência e configurações").performClick()
        composeTestRule.onNodeWithText("Estatísticas").performClick()
        composeTestRule.onNodeWithText("Artistas e álbuns").performClick()

        assertTrue(equalizerOpened)
        assertTrue(themesOpened)
        assertTrue(statisticsOpened)
        assertTrue(browserOpened)
    }
}
