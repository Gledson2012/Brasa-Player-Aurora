package com.example

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.ui.components.AuroraNavBar
import com.example.ui.theme.MusicPlayerTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class AuroraNavBarTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `AuroraNavBar renders all 6 navigation tabs and main container`() {
        composeTestRule.setContent {
            MusicPlayerTheme {
                AuroraNavBar(
                    selectedTab = 0,
                    onTabSelected = {},
                    isPlayingSong = false,
                    isPlayingRadio = false
                )
            }
        }

        // Main bar container
        composeTestRule.onNodeWithTag("main_bottom_nav").assertIsDisplayed()

        // 6 tabs
        composeTestRule.onNodeWithTag("nav_item_0").assertIsDisplayed()
        composeTestRule.onNodeWithText("Início").assertIsDisplayed()

        composeTestRule.onNodeWithTag("nav_item_1").assertIsDisplayed()
        composeTestRule.onNodeWithText("Músicas").assertIsDisplayed()

        composeTestRule.onNodeWithTag("nav_item_2").assertIsDisplayed()
        composeTestRule.onNodeWithText("Playlists").assertIsDisplayed()

        composeTestRule.onNodeWithTag("nav_item_3").assertIsDisplayed()
        composeTestRule.onNodeWithText("Equalizador").assertIsDisplayed()

        composeTestRule.onNodeWithTag("nav_item_4").assertIsDisplayed()
        composeTestRule.onNodeWithText("Temas").assertIsDisplayed()

        composeTestRule.onNodeWithTag("nav_item_5").assertIsDisplayed()
        composeTestRule.onNodeWithText("Rádio").assertIsDisplayed()
    }

    @Test
    fun `AuroraNavBar clicking items triggers onTabSelected with expected index`() {
        var selectedIndex: Int? = null

        composeTestRule.setContent {
            MusicPlayerTheme {
                AuroraNavBar(
                    selectedTab = 0,
                    onTabSelected = { selectedIndex = it },
                    isPlayingSong = true,
                    isPlayingRadio = false
                )
            }
        }

        composeTestRule.onNodeWithTag("nav_item_1").performClick()
        assertEquals(1, selectedIndex)

        composeTestRule.onNodeWithTag("nav_item_3").performClick()
        assertEquals(3, selectedIndex)

        composeTestRule.onNodeWithTag("nav_item_5").performClick()
        assertEquals(5, selectedIndex)

        composeTestRule.onNodeWithTag("nav_item_2").performClick()
        assertEquals(2, selectedIndex)

        composeTestRule.onNodeWithTag("nav_item_4").performClick()
        assertEquals(4, selectedIndex)

        composeTestRule.onNodeWithTag("nav_item_0").performClick()
        assertEquals(0, selectedIndex)
    }

    @Test
    fun `AuroraNavBar renders with active radio playing indicator`() {
        composeTestRule.setContent {
            MusicPlayerTheme {
                AuroraNavBar(
                    selectedTab = 5,
                    onTabSelected = {},
                    isPlayingSong = false,
                    isPlayingRadio = true
                )
            }
        }

        composeTestRule.onNodeWithTag("nav_item_5").assertIsDisplayed()
        composeTestRule.onNodeWithText("Rádio").assertIsDisplayed()
    }
}
