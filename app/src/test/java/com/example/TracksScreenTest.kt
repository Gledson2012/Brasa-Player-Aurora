package com.example

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.example.data.model.Song
import com.example.ui.screens.TracksScreen
import com.example.ui.theme.MusicPlayerTheme
import com.example.ui.viewmodel.delegate.SortOption
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class TracksScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val sampleSongs = listOf(
        Song(
            id = 1,
            title = "Aquarela",
            artist = "Toquinho",
            album = "Aquarela Álbum",
            durationMs = 240000,
            isFavorite = true,
            playCount = 10
        ),
        Song(
            id = 2,
            title = "Garota de Ipanema",
            artist = "Tom Jobim",
            album = "Bossa Nova Hits",
            durationMs = 180000,
            isFavorite = false,
            playCount = 3
        )
    )

    @Test
    fun `TracksScreen renders empty state when library has no songs`() {
        var openFilesCalled = false

        composeTestRule.setContent {
            MusicPlayerTheme {
                TracksScreen(
                    songs = emptyList(),
                    currentPlayingSong = null,
                    isPlaying = false,
                    searchQuery = "",
                    currentSort = SortOption.TITLE,
                    onSearchChange = {},
                    onSortChange = {},
                    onOpenFiles = { openFilesCalled = true },
                    onSongClick = { _, _ -> },
                    onPlayAll = {},
                    onShuffleAll = {},
                    onToggleFavorite = {},
                    onAddToPlaylist = {},
                    onEditSong = {},
                    onRelinkSong = {},
                    onDeleteSong = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Sua biblioteca está vazia").assertIsDisplayed()
        composeTestRule.onNodeWithTag("open_device_files_button").assertIsDisplayed().performClick()
        assertTrue("onOpenFiles should have been invoked", openFilesCalled)
    }

    @Test
    fun `TracksScreen renders songs list and allows playing all`() {
        var playAllInvoked = false

        composeTestRule.setContent {
            MusicPlayerTheme {
                TracksScreen(
                    songs = sampleSongs,
                    currentPlayingSong = sampleSongs[0],
                    isPlaying = true,
                    searchQuery = "",
                    currentSort = SortOption.TITLE,
                    onSearchChange = {},
                    onSortChange = {},
                    onOpenFiles = {},
                    onSongClick = { _, _ -> },
                    onPlayAll = { playAllInvoked = true },
                    onShuffleAll = {},
                    onToggleFavorite = {},
                    onAddToPlaylist = {},
                    onEditSong = {},
                    onRelinkSong = {},
                    onDeleteSong = {}
                )
            }
        }

        // Header and items
        composeTestRule.onNodeWithText("Músicas").assertIsDisplayed()
        composeTestRule.onNodeWithText("Aquarela").assertIsDisplayed()
        composeTestRule.onNodeWithText("Garota de Ipanema").assertIsDisplayed()

        // Play all button
        composeTestRule.onNodeWithTag("play_all_button").assertIsDisplayed().performClick()
        assertTrue("onPlayAll should have been called", playAllInvoked)
    }

    @Test
    fun `TracksScreen search text input triggers callback`() {
        var currentQuery = ""

        composeTestRule.setContent {
            MusicPlayerTheme {
                TracksScreen(
                    songs = sampleSongs,
                    currentPlayingSong = null,
                    isPlaying = false,
                    searchQuery = currentQuery,
                    currentSort = SortOption.TITLE,
                    onSearchChange = { currentQuery = it },
                    onSortChange = {},
                    onOpenFiles = {},
                    onSongClick = { _, _ -> },
                    onPlayAll = {},
                    onShuffleAll = {},
                    onToggleFavorite = {},
                    onAddToPlaylist = {},
                    onEditSong = {},
                    onRelinkSong = {},
                    onDeleteSong = {}
                )
            }
        }

        composeTestRule.onNodeWithTag("search_tracks_input").performTextInput("Tom")
        assertEquals("Tom", currentQuery)
    }
}
