package com.example

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import com.example.data.model.Playlist
import com.example.data.model.PlaylistWithSongs
import com.example.data.model.Song
import com.example.ui.screens.PlaylistsScreen
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
class PlaylistsScreenTest {

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

    private val samplePlaylists = listOf(
        PlaylistWithSongs(
            playlist = Playlist(id = 10, name = "MPB Clássicos", description = "As melhores do Brasil"),
            songs = sampleSongs
        ),
        PlaylistWithSongs(
            playlist = Playlist(id = 11, name = "Relax Acústico", description = "Para relaxar"),
            songs = emptyList()
        )
    )

    @Test
    fun `PlaylistsScreen renders overview with smart playlists and custom playlists`() {
        var createClicked = false
        var openedPlaylist: PlaylistWithSongs? = null

        composeTestRule.setContent {
            MusicPlayerTheme {
                PlaylistsScreen(
                    playlistsWithSongs = samplePlaylists,
                    favoriteSongs = sampleSongs,
                    recentlyPlayed = sampleSongs,
                    mostPlayed = sampleSongs,
                    currentPlayingSong = null,
                    isPlaying = false,
                    activePlaylistDetail = null,
                    onOpenPlaylistDetail = { openedPlaylist = it },
                    onClosePlaylistDetail = {},
                    onCreatePlaylistClick = { createClicked = true },
                    onDeletePlaylist = {},
                    onPlaySongFromList = { _, _ -> },
                    onToggleFavorite = {},
                    onRemoveSongFromPlaylist = { _, _ -> }
                )
            }
        }

        // Header and Smart playlists
        composeTestRule.onNodeWithText("Playlists").assertIsDisplayed()
        composeTestRule.onNodeWithTag("smart_playlist_favorites").assertIsDisplayed()
        composeTestRule.onNodeWithTag("smart_playlist_recent").assertIsDisplayed()
        composeTestRule.onNodeWithTag("smart_playlist_most_played").assertExists()

        // Create playlist button
        composeTestRule.onNodeWithTag("create_playlist_button").assertIsDisplayed().performClick()
        assertTrue("onCreatePlaylistClick should have been called", createClicked)

        // Custom playlist items (scroll down to items)
        composeTestRule.onNodeWithTag("playlists_screen").performScrollToNode(hasTestTag("playlist_item_10"))
        composeTestRule.onNodeWithText("MPB Clássicos").assertExists()

        // Click on playlist item to open detail
        composeTestRule.onNodeWithTag("playlist_item_10").performClick()
        assertEquals(10L, openedPlaylist?.playlist?.id)
    }

    @Test
    fun `PlaylistsScreen search filters playlist items`() {
        composeTestRule.setContent {
            MusicPlayerTheme {
                PlaylistsScreen(
                    playlistsWithSongs = samplePlaylists,
                    favoriteSongs = emptyList(),
                    recentlyPlayed = emptyList(),
                    mostPlayed = emptyList(),
                    currentPlayingSong = null,
                    isPlaying = false,
                    activePlaylistDetail = null,
                    onOpenPlaylistDetail = {},
                    onClosePlaylistDetail = {},
                    onCreatePlaylistClick = {},
                    onDeletePlaylist = {},
                    onPlaySongFromList = { _, _ -> },
                    onToggleFavorite = {},
                    onRemoveSongFromPlaylist = { _, _ -> }
                )
            }
        }

        // Filter by typing 'MPB'
        composeTestRule.onNodeWithTag("search_playlists_input").performTextInput("MPB")
        composeTestRule.onNodeWithTag("playlists_screen").performScrollToNode(hasTestTag("playlist_item_10"))
        composeTestRule.onNodeWithText("MPB Clássicos").assertExists()
        composeTestRule.onNodeWithText("Relax Acústico").assertDoesNotExist()
    }

    @Test
    fun `PlaylistsScreen renders detail view when activePlaylistDetail is set`() {
        var backInvoked = false
        var playAllInvoked = false

        composeTestRule.setContent {
            MusicPlayerTheme {
                PlaylistsScreen(
                    playlistsWithSongs = samplePlaylists,
                    favoriteSongs = emptyList(),
                    recentlyPlayed = emptyList(),
                    mostPlayed = emptyList(),
                    currentPlayingSong = sampleSongs[0],
                    isPlaying = true,
                    activePlaylistDetail = samplePlaylists[0],
                    onOpenPlaylistDetail = {},
                    onClosePlaylistDetail = { backInvoked = true },
                    onCreatePlaylistClick = {},
                    onDeletePlaylist = {},
                    onPlaySongFromList = { _, _ -> playAllInvoked = true },
                    onToggleFavorite = {},
                    onRemoveSongFromPlaylist = { _, _ -> }
                )
            }
        }

        composeTestRule.onNodeWithTag("playlist_detail_screen").assertIsDisplayed()
        composeTestRule.onNodeWithText("Aquarela").assertIsDisplayed()

        // Play playlist
        composeTestRule.onNodeWithTag("play_playlist_button").assertIsDisplayed().performClick()
        assertTrue("onPlaySongFromList should have been called", playAllInvoked)

        // Back button
        composeTestRule.onNodeWithTag("playlist_detail_back_button").assertIsDisplayed().performClick()
        assertTrue("onClosePlaylistDetail should have been called", backInvoked)
    }
}
