package com.example

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.data.model.Song
import com.example.ui.screens.HomeScreen
import com.example.ui.theme.MusicPlayerTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class HomeScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val sampleSongs = listOf(
        Song(
            id = 1,
            title = "Aquarela",
            artist = "Toquinho",
            album = "Aquarela",
            durationMs = 240000,
            isFavorite = true,
            playCount = 10
        ),
        Song(
            id = 2,
            title = "Águas de Março",
            artist = "Tom Jobim",
            album = "Elis & Tom",
            durationMs = 210000,
            isFavorite = false,
            playCount = 5
        )
    )

    @Test
    fun `HomeScreen renders empty state when no songs exist`() {
        var scanCalled = false

        composeTestRule.setContent {
            MusicPlayerTheme {
                HomeScreen(
                    currentSong = null,
                    isPlaying = false,
                    allSongs = emptyList(),
                    recentlyPlayed = emptyList(),
                    favoriteSongs = emptyList(),
                    mostPlayed = emptyList(),
                    playlists = emptyList(),
                    onPlayPause = {},
                    onPlaySong = { _, _ -> },
                    onOpenTracks = {},
                    onOpenPlaylists = {},
                    onScanMedia = { scanCalled = true }
                )
            }
        }

        composeTestRule.onNodeWithText("Comece sua biblioteca").assertIsDisplayed()
        composeTestRule.onNodeWithText("Escanear áudio").assertIsDisplayed().performClick()
        assertTrue("onScanMedia should have been invoked", scanCalled)
    }

    @Test
    fun `HomeScreen renders now playing card and metrics when songs exist`() {
        var playerOpened = false
        var favoriteToggled = false

        composeTestRule.setContent {
            MusicPlayerTheme {
                HomeScreen(
                    currentSong = sampleSongs[0],
                    isPlaying = true,
                    currentPositionMs = 60000L,
                    durationMs = 240000L,
                    allSongs = sampleSongs,
                    recentlyPlayed = sampleSongs,
                    favoriteSongs = listOf(sampleSongs[0]),
                    mostPlayed = sampleSongs,
                    playlists = emptyList(),
                    onPlayPause = {},
                    onPlaySong = { _, _ -> },
                    onOpenTracks = {},
                    onOpenPlaylists = {},
                    onOpenPlayer = { playerOpened = true },
                    onToggleFavorite = { favoriteToggled = true }
                )
            }
        }

        // Check header branding and song info
        composeTestRule.onNodeWithText("BRASA PLAYER AURORA").assertIsDisplayed()
        composeTestRule.onAllNodesWithText("Aquarela").onFirst().assertIsDisplayed()
        composeTestRule.onAllNodesWithText("Toquinho").onFirst().assertIsDisplayed()
        composeTestRule.onNodeWithText("REPRODUZINDO AGORA").assertIsDisplayed()

        // Check interactive metric items
        composeTestRule.onAllNodesWithText("2").onFirst().assertIsDisplayed() // Metric value
        composeTestRule.onNodeWithText("faixas").assertIsDisplayed()
        composeTestRule.onNodeWithText("artistas").assertIsDisplayed()

        // Test clicking "Tela cheia"
        composeTestRule.onNodeWithText("Tela cheia").assertIsDisplayed().performClick()
        assertTrue("onOpenPlayer should have been invoked", playerOpened)
    }
}
