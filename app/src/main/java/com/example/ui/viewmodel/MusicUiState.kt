package com.example.ui.viewmodel

import com.example.audio.RepeatMode
import com.example.data.lyrics.TrackLyrics
import com.example.data.model.EqualizerState
import com.example.data.model.LastFmSettings
import com.example.data.model.PlaylistWithSongs
import com.example.data.model.Song
import com.example.data.model.ThemeConfig

/** Immutable snapshot consumed by the Compose UI. */
data class MusicUiState(
    val selectedTab: Int = 0,
    val isFullPlayerOpen: Boolean = false,
    val currentSong: Song? = null,
    val isPlaying: Boolean = false,
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val queue: List<Song> = emptyList(),
    val repeatMode: RepeatMode = RepeatMode.ALL,
    val isShuffle: Boolean = false,
    val playbackSpeed: Float = 1.0f,
    val crossfadeSeconds: Int = 0,
    val visualizerAmplitudes: FloatArray = FloatArray(32) { 0.1f },
    val waveformSamples: List<Float> = List(96) { 0.12f },
    val sleepTimerRemainingSeconds: Int? = null,
    val sleepTimerEndAtTrackEnd: Boolean = false,
    val playbackError: String? = null,
    val onboardingCompleted: Boolean = false,
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val allSongs: List<Song> = emptyList(),
    val displayedSongs: List<Song> = emptyList(),
    val favoriteSongs: List<Song> = emptyList(),
    val recentlyPlayed: List<Song> = emptyList(),
    val mostPlayed: List<Song> = emptyList(),
    val allPlaylistsWithSongs: List<PlaylistWithSongs> = emptyList(),
    val activePlaylistDetail: PlaylistWithSongs? = null,
    val searchQuery: String = "",
    val sortOption: SortOption = SortOption.TITLE,
    val equalizerState: EqualizerState = EqualizerState(),
    val themeSettings: ThemeConfig = ThemeConfig(),
    val lastFmSettings: LastFmSettings = LastFmSettings(),
    val pendingScrobbleCount: Int = 0,
    val scanStatusMessage: String? = null,
    val lastFmMessage: String? = null,
    val lastFmAuthUrl: String? = null,
    val currentLyrics: TrackLyrics? = null,
    val isLyricsLoading: Boolean = false,
    val isLyricsViewActive: Boolean = false,
    val showCreatePlaylistDialog: Boolean = false,
    val showAddToPlaylistDialog: Song? = null,
    val showSleepTimerDialog: Boolean = false,
    val showSpeedDialog: Boolean = false,
    val showLastFmDialog: Boolean = false,
    val showLyricsEditor: Boolean = false
)
