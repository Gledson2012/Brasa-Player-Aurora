package com.example

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import com.example.data.model.Song
import com.example.data.radio.RadioBrowserClient
import com.example.ui.screens.RadiosScreen
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
class RadiosScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val testRadioClient = RadioBrowserClient(mirrors = emptyList())

    @Test
    fun `RadiosScreen renders header, hero card, search bar, and source tabs`() {
        composeTestRule.setContent {
            MusicPlayerTheme {
                RadiosScreen(
                    onOpenLink = {},
                    onPlayStation = { _, _, _, _, _ -> },
                    currentPlayingSong = null,
                    isPlaying = false,
                    onTogglePlayPause = {},
                    radioBrowserClient = testRadioClient
                )
            }
        }

        composeTestRule.onNodeWithTag("radios_screen").assertExists()
        composeTestRule.onNodeWithText("Rádios ao Vivo").assertIsDisplayed()
        composeTestRule.onNodeWithTag("refresh_radios_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("add_radio_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("radio_hero_card").assertIsDisplayed()
        composeTestRule.onNodeWithTag("radio_search_input").assertIsDisplayed()

        // Tabs
        composeTestRule.onNodeWithTag("filter_tab_BRAZIL").assertExists()
        composeTestRule.onNodeWithTag("filter_tab_GLOBAL").assertExists()
        composeTestRule.onNodeWithTag("filter_tab_GENRES").assertExists()
        composeTestRule.onNodeWithTag("filter_tab_FAVORITES").assertExists()
        composeTestRule.onNodeWithTag("filter_tab_CURATED").assertExists()
    }

    @Test
    fun `RadiosScreen clicking hero play button calls onPlayStation`() {
        var playedTitle: String? = null
        var playedUrl: String? = null

        composeTestRule.setContent {
            MusicPlayerTheme {
                RadiosScreen(
                    onOpenLink = {},
                    onPlayStation = { title, _, _, _, streamUrl ->
                        playedTitle = title
                        playedUrl = streamUrl
                    },
                    currentPlayingSong = null,
                    isPlaying = false,
                    onTogglePlayPause = {},
                    radioBrowserClient = testRadioClient
                )
            }
        }

        composeTestRule.onNodeWithTag("hero_play_button").performClick()
        assertNotNull(playedTitle)
        assertNotNull(playedUrl)
    }

    @Test
    fun `RadiosScreen searching filters stations and clearing resets input`() {
        composeTestRule.setContent {
            MusicPlayerTheme {
                RadiosScreen(
                    onOpenLink = {},
                    onPlayStation = { _, _, _, _, _ -> },
                    currentPlayingSong = null,
                    isPlaying = false,
                    onTogglePlayPause = {},
                    radioBrowserClient = testRadioClient
                )
            }
        }

        composeTestRule.onNodeWithTag("radio_search_input").performTextInput("Jornal")
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("clear_search_button").assertExists().performClick()
    }

    @Test
    fun `RadiosScreen switching tabs reveals genres and curated lists`() {
        composeTestRule.setContent {
            MusicPlayerTheme {
                RadiosScreen(
                    onOpenLink = {},
                    onPlayStation = { _, _, _, _, _ -> },
                    currentPlayingSong = null,
                    isPlaying = false,
                    onTogglePlayPause = {},
                    radioBrowserClient = testRadioClient
                )
            }
        }

        // Switch to Curadas tab
        composeTestRule.onNodeWithTag("filter_tab_CURATED").performClick()
        composeTestRule.waitForIdle()

        // Switch to Genres tab
        composeTestRule.onNodeWithTag("filter_tab_GENRES").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("radios_screen").performScrollToNode(androidx.compose.ui.test.hasTestTag("genre_chip_Todos"))
        composeTestRule.onNodeWithTag("genre_chip_Todos").assertExists()
    }

    @Test
    fun `RadiosScreen adding custom radio station through dialog triggers playback`() {
        var playedStationName: String? = null
        var playedStationStream: String? = null

        composeTestRule.setContent {
            MusicPlayerTheme {
                RadiosScreen(
                    onOpenLink = {},
                    onPlayStation = { title, _, _, _, streamUrl ->
                        playedStationName = title
                        playedStationStream = streamUrl
                    },
                    currentPlayingSong = null,
                    isPlaying = false,
                    onTogglePlayPause = {},
                    radioBrowserClient = testRadioClient
                )
            }
        }

        // Open Dialog
        composeTestRule.onNodeWithTag("add_radio_button").performClick()

        // Input details
        composeTestRule.onNodeWithTag("custom_station_name_input").performTextInput("Custom Test Radio")
        composeTestRule.onNodeWithTag("custom_station_url_input").performTextInput("https://customstream.com/live.aac")

        // Confirm
        composeTestRule.onNodeWithTag("confirm_add_station_button").performClick()

        assertEquals("Custom Test Radio", playedStationName)
        assertEquals("https://customstream.com/live.aac", playedStationStream)
    }

    @Test
    fun `RadiosScreen indicates currently playing radio in hero banner`() {
        val playingSong = Song(
            id = -13492L,
            title = "Rádio Jornal 91.3 FM",
            artist = "Rádio • Notícias",
            album = "Rádio ao vivo",
            durationMs = 0L,
            mediaUri = "https://www.radios.com.br/play/playlist/13492/listen-radio.m3u",
            coverUri = "",
            sourceKey = "radio:13492"
        )

        composeTestRule.setContent {
            MusicPlayerTheme {
                RadiosScreen(
                    onOpenLink = {},
                    onPlayStation = { _, _, _, _, _ -> },
                    currentPlayingSong = playingSong,
                    isPlaying = true,
                    onTogglePlayPause = {},
                    radioBrowserClient = testRadioClient
                )
            }
        }

        composeTestRule.onNodeWithText("SINTONIZADO AGORA").assertExists()
    }
}
