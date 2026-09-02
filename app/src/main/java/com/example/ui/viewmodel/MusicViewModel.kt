package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.audio.AudioPlayerEngine
import com.example.audio.RepeatMode
import com.example.data.backup.BackupManager
import com.example.data.datastore.ThemePreferencesDataStore
import com.example.data.datastore.LastFmPreferencesDataStore
import com.example.data.lastfm.ScrobbleQueueManager
import com.example.data.lyrics.TrackLyrics
import com.example.data.model.AlbumArtStyle
import com.example.data.model.AppThemeType
import com.example.data.model.CustomPresetEntity
import com.example.data.model.EqualizerPreset
import com.example.data.model.EqualizerState
import com.example.data.model.LastFmSettings
import com.example.data.model.LyricsEntity
import com.example.data.model.ListeningStatistics
import com.example.data.model.Playlist
import com.example.data.model.PlaylistWithSongs
import com.example.data.model.Song
import com.example.data.model.ThemeConfig
import com.example.data.model.ThemeMode
import com.example.data.model.UserSettingsEntity
import com.example.data.model.VisualizerStyle
import com.example.data.repository.MusicRepository
import com.example.data.lyrics.LyricsManager
import com.example.service.MusicPlaybackService
import com.example.ui.viewmodel.delegate.EqualizerController
import com.example.ui.viewmodel.delegate.LibraryController
import com.example.ui.viewmodel.delegate.PlaybackController
import com.example.ui.viewmodel.delegate.SettingsController
import com.example.ui.viewmodel.delegate.SortOption
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Orchestrator ViewModel that delegates to focused controller classes:
 * - [PlaybackController]: play/pause/seek/queue/repeat/shuffle/radio/sleep
 * - [EqualizerController]: EQ presets/bands/bass/virtualizer/balance
 * - [LibraryController]: search/sort/import/scan/delete/relink/playlists/lyrics
 * - [SettingsController]: themes/lastfm/backup
 *
 * All public method signatures are preserved for backward compatibility
 * with existing screens. Each call simply forwards to the appropriate delegate.
 */
class MusicViewModel(
    application: Application,
    private val repository: MusicRepository,
    private val themeDataStore: ThemePreferencesDataStore,
    private val lastFmDataStore: LastFmPreferencesDataStore,
    private val backupManager: BackupManager,
    private val playerEngine: AudioPlayerEngine,
    private val scrobbleQueueManager: ScrobbleQueueManager
) : AndroidViewModel(application) {

    // ──────────────────────────────────────────────────
    // Delegate Controllers
    // ──────────────────────────────────────────────────

    val playbackCtrl = PlaybackController(
        application = application,
        playerEngine = playerEngine,
        repository = repository,
        scope = viewModelScope
    )

    val equalizerCtrl = EqualizerController(
        repository = repository,
        playerEngine = playerEngine,
        scope = viewModelScope
    )

    val libraryCtrl = LibraryController(
        application = application,
        repository = repository,
        scope = viewModelScope
    )

    val settingsCtrl = SettingsController(
        application = application,
        themeDataStore = themeDataStore,
        lastFmDataStore = lastFmDataStore,
        backupManager = backupManager,
        scrobbleQueueManager = scrobbleQueueManager,
        scope = viewModelScope
    )

    // ──────────────────────────────────────────────────
    // Backward-compatible StateFlow delegations
    // ──────────────────────────────────────────────────

    // Playback (from PlaybackController)
    val isPlaying: StateFlow<Boolean> = playbackCtrl.isPlaying
    val currentSong: StateFlow<Song?> = playbackCtrl.currentSong
    val currentPositionMs: StateFlow<Long> = playbackCtrl.currentPositionMs
    val durationMs: StateFlow<Long> = playbackCtrl.durationMs
    val queue: StateFlow<List<Song>> = playbackCtrl.queue
    val repeatMode: StateFlow<RepeatMode> = playbackCtrl.repeatMode
    val isShuffle: StateFlow<Boolean> = playbackCtrl.isShuffle
    val playbackSpeed: StateFlow<Float> = playbackCtrl.playbackSpeed
    val crossfadeSeconds: StateFlow<Int> = playbackCtrl.crossfadeSeconds
    val visualizerAmplitudes: StateFlow<FloatArray> = playbackCtrl.visualizerAmplitudes
    val waveformSamples: StateFlow<List<Float>> = playbackCtrl.waveformSamples
    val playbackError: StateFlow<String?> = playbackCtrl.playbackError
    val sleepTimerRemainingSeconds: StateFlow<Int?> = playbackCtrl.sleepTimerRemainingSeconds
    val sleepTimerEndAtTrackEnd: StateFlow<Boolean> = playbackCtrl.sleepTimerEndAtTrackEnd

    // Library (from LibraryController)
    val searchQuery: StateFlow<String> = libraryCtrl.searchQuery
    val sortOption: StateFlow<SortOption> = libraryCtrl.sortOption
    val allSongs: StateFlow<List<Song>> = libraryCtrl.allSongs
    val favoriteSongs: StateFlow<List<Song>> = libraryCtrl.favoriteSongs
    val recentlyPlayed: StateFlow<List<Song>> = libraryCtrl.recentlyPlayed
    val mostPlayed: StateFlow<List<Song>> = libraryCtrl.mostPlayed
    val allPlaylistsWithSongs: StateFlow<List<PlaylistWithSongs>> = libraryCtrl.allPlaylistsWithSongs
    val displayedSongs: StateFlow<List<Song>> = libraryCtrl.displayedSongs
    val currentLyrics: StateFlow<TrackLyrics?> = libraryCtrl.currentLyrics
    val isLyricsLoading: StateFlow<Boolean> = libraryCtrl.isLyricsLoading
    val isLyricsViewActive: StateFlow<Boolean> = libraryCtrl.isLyricsViewActive

    // Settings (from SettingsController)
    val themeConfig: StateFlow<ThemeConfig> = settingsCtrl.themeConfig
    val themeSettings: StateFlow<ThemeConfig> = settingsCtrl.themeSettings
    val onboardingCompleted: StateFlow<Boolean> = settingsCtrl.onboardingCompleted
    val lastFmSettings: StateFlow<LastFmSettings> = settingsCtrl.lastFmSettings
    val lastFmMessage: StateFlow<String?> = settingsCtrl.lastFmMessage
    val lastFmAuthUrl: StateFlow<String?> = settingsCtrl.lastFmAuthUrl
    val pendingScrobbleCount: StateFlow<Int> = settingsCtrl.pendingScrobbleCount

    // Equalizer (from EqualizerController)
    val equalizerState: StateFlow<EqualizerState> = equalizerCtrl.equalizerState

    // ──────────────────────────────────────────────────
    // ViewModel-owned UI state (navigation, dialogs, messages)
    // ──────────────────────────────────────────────────

    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    private val _isFullPlayerOpen = MutableStateFlow(false)
    val isFullPlayerOpen: StateFlow<Boolean> = _isFullPlayerOpen.asStateFlow()

    private val _activePlaylistDetail = MutableStateFlow<PlaylistWithSongs?>(null)
    val activePlaylistDetail: StateFlow<PlaylistWithSongs?> = _activePlaylistDetail.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _scanStatusMessage = MutableStateFlow<String?>(null)
    val scanStatusMessage: StateFlow<String?> = _scanStatusMessage.asStateFlow()

    private val _showCreatePlaylistDialog = MutableStateFlow(false)
    val showCreatePlaylistDialog: StateFlow<Boolean> = _showCreatePlaylistDialog.asStateFlow()

    private val _showAddToPlaylistDialog = MutableStateFlow<Song?>(null)
    val showAddToPlaylistDialog: StateFlow<Song?> = _showAddToPlaylistDialog.asStateFlow()

    private val _showSleepTimerDialog = MutableStateFlow(false)
    val showSleepTimerDialog: StateFlow<Boolean> = _showSleepTimerDialog.asStateFlow()

    private val _showSpeedDialog = MutableStateFlow(false)
    val showSpeedDialog: StateFlow<Boolean> = _showSpeedDialog.asStateFlow()

    private val _showLastFmDialog = MutableStateFlow(false)
    val showLastFmDialog: StateFlow<Boolean> = _showLastFmDialog.asStateFlow()

    private val _showLyricsEditor = MutableStateFlow(false)
    val showLyricsEditor: StateFlow<Boolean> = _showLyricsEditor.asStateFlow()

    private val _showTrackOptionsSheet = MutableStateFlow<Song?>(null)
    val showTrackOptionsSheet: StateFlow<Song?> = _showTrackOptionsSheet.asStateFlow()

    // Statistics
    private val _statistics = MutableStateFlow<ListeningStatistics?>(null)
    val statistics: StateFlow<ListeningStatistics?> = _statistics.asStateFlow()
    private val _isStatisticsLoading = MutableStateFlow(false)
    val isStatisticsLoading: StateFlow<Boolean> = _isStatisticsLoading.asStateFlow()

    private fun setScanStatusMessage(message: String?) {
        _scanStatusMessage.value = message
    }

    // ──────────────────────────────────────────────────
    // Combined UiState (identical structure to before)
    // ──────────────────────────────────────────────────

    private data class PlaybackCore(
        val currentSong: Song?, val isPlaying: Boolean,
        val currentPositionMs: Long, val durationMs: Long, val queue: List<Song>
    )
    private data class PlaybackOptions(
        val repeatMode: RepeatMode, val isShuffle: Boolean,
        val playbackSpeed: Float, val crossfadeSeconds: Int
    )
    private data class PlaybackVisuals(
        val visualizerAmplitudes: FloatArray, val waveformSamples: List<Float>,
        val sleepTimerRemainingSeconds: Int?, val sleepTimerEndAtTrackEnd: Boolean,
        val playbackError: String?
    )
    private data class LibraryState(
        val allSongs: List<Song>, val displayedSongs: List<Song>,
        val favoriteSongs: List<Song>, val recentlyPlayed: List<Song>,
        val mostPlayed: List<Song>, val allPlaylistsWithSongs: List<PlaylistWithSongs>
    )
    private data class NavigationState(
        val selectedTab: Int, val isFullPlayerOpen: Boolean,
        val activePlaylistDetail: PlaylistWithSongs?
    )
    private data class AppStatus(
        val onboardingCompleted: Boolean, val isLoading: Boolean,
        val isRefreshing: Boolean, val searchQuery: String, val sortOption: SortOption
    )
    private data class Messages(
        val scanStatusMessage: String?, val lastFmMessage: String?, val lastFmAuthUrl: String?
    )
    private data class DialogState(
        val showCreatePlaylistDialog: Boolean, val showAddToPlaylistDialog: Song?,
        val showSleepTimerDialog: Boolean, val showSpeedDialog: Boolean,
        val showLastFmDialog: Boolean, val showLyricsEditor: Boolean
    )
    private data class LyricsState(
        val currentLyrics: TrackLyrics?, val isLyricsLoading: Boolean,
        val isLyricsViewActive: Boolean
    )

    private val playbackCore = combine(currentSong, isPlaying, currentPositionMs, durationMs, queue) { s, p, pos, dur, q ->
        PlaybackCore(s, p, pos, dur, q)
    }
    private val playbackOptions = combine(repeatMode, isShuffle, playbackSpeed, crossfadeSeconds) { r, sh, sp, cf ->
        PlaybackOptions(r, sh, sp, cf)
    }
    private val playbackVisuals = combine(visualizerAmplitudes, waveformSamples, sleepTimerRemainingSeconds, sleepTimerEndAtTrackEnd, playbackError) { a, w, t, e, err ->
        PlaybackVisuals(a, w, t, e, err)
    }
    private val playbackState = combine(playbackCore, playbackOptions, playbackVisuals) { c, o, v -> Triple(c, o, v) }

    private val libraryStateFlow = combine(allSongs, displayedSongs, favoriteSongs, recentlyPlayed, mostPlayed) { s, d, f, r, p ->
        LibraryState(s, d, f, r, p, emptyList())
    }.let { ss -> combine(ss, allPlaylistsWithSongs) { s, pl -> s.copy(allPlaylistsWithSongs = pl) } }

    private val preferencesState = combine(equalizerState, themeSettings, lastFmSettings) { eq, th, lf -> Triple(eq, th, lf) }
    private val navigationState = combine(selectedTab, isFullPlayerOpen, activePlaylistDetail) { t, fp, pl -> NavigationState(t, fp, pl) }
    private val appStatus = combine(onboardingCompleted, isLoading, isRefreshing, searchQuery, sortOption) { o, l, r, q, s -> AppStatus(o, l, r, q, s) }
    private val messagesState = combine(scanStatusMessage, lastFmMessage, lastFmAuthUrl) { s, l, a -> Messages(s, l, a) }
    private val dialogCore = combine(showCreatePlaylistDialog, showAddToPlaylistDialog, showSleepTimerDialog, showSpeedDialog, showLastFmDialog) { c, a, sl, sp, lf ->
        DialogState(c, a, sl, sp, lf, false)
    }
    private val dialogs = combine(dialogCore, showLyricsEditor) { s, l -> s.copy(showLyricsEditor = l) }
    private val lyricsState = combine(currentLyrics, isLyricsLoading, isLyricsViewActive) { ly, lo, la -> LyricsState(ly, lo, la) }

    private val uiStateBase = combine(playbackState, libraryStateFlow, preferencesState, navigationState, appStatus) { pb, lib, pref, nav, st ->
        MusicUiState(
            currentSong = pb.first.currentSong, isPlaying = pb.first.isPlaying,
            currentPositionMs = pb.first.currentPositionMs, durationMs = pb.first.durationMs,
            queue = pb.first.queue, repeatMode = pb.second.repeatMode,
            isShuffle = pb.second.isShuffle, playbackSpeed = pb.second.playbackSpeed,
            crossfadeSeconds = pb.second.crossfadeSeconds,
            visualizerAmplitudes = pb.third.visualizerAmplitudes,
            waveformSamples = pb.third.waveformSamples,
            sleepTimerRemainingSeconds = pb.third.sleepTimerRemainingSeconds,
            sleepTimerEndAtTrackEnd = pb.third.sleepTimerEndAtTrackEnd,
            playbackError = pb.third.playbackError,
            allSongs = lib.allSongs, displayedSongs = lib.displayedSongs,
            favoriteSongs = lib.favoriteSongs, recentlyPlayed = lib.recentlyPlayed,
            mostPlayed = lib.mostPlayed, allPlaylistsWithSongs = lib.allPlaylistsWithSongs,
            equalizerState = pref.first, themeSettings = pref.second,
            lastFmSettings = pref.third, selectedTab = nav.selectedTab,
            isFullPlayerOpen = nav.isFullPlayerOpen, activePlaylistDetail = nav.activePlaylistDetail,
            onboardingCompleted = st.onboardingCompleted, isLoading = st.isLoading,
            isRefreshing = st.isRefreshing, searchQuery = st.searchQuery,
            sortOption = st.sortOption
        )
    }

    private val statisticsState = combine(_statistics, _isStatisticsLoading) { s, l -> Pair(s, l) }

    val uiState: StateFlow<MusicUiState> = combine(uiStateBase, messagesState, dialogs, lyricsState, pendingScrobbleCount) { state, msg, dlg, ly, sc ->
        state.copy(
            scanStatusMessage = msg.scanStatusMessage, lastFmMessage = msg.lastFmMessage,
            lastFmAuthUrl = msg.lastFmAuthUrl, currentLyrics = ly.currentLyrics,
            isLyricsLoading = ly.isLyricsLoading, isLyricsViewActive = ly.isLyricsViewActive,
            showCreatePlaylistDialog = dlg.showCreatePlaylistDialog,
            showAddToPlaylistDialog = dlg.showAddToPlaylistDialog,
            showSleepTimerDialog = dlg.showSleepTimerDialog,
            showSpeedDialog = dlg.showSpeedDialog,
            showLastFmDialog = dlg.showLastFmDialog,
            showLyricsEditor = dlg.showLyricsEditor, pendingScrobbleCount = sc
        )
    }.combine(statisticsState) { s, sp -> s.copy(statistics = sp.first, isStatisticsLoading = sp.second) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, MusicUiState())

    // ──────────────────────────────────────────────────
    // Init block
    // ──────────────────────────────────────────────────

    private var lastFmScrobbledSongId: Long? = null
    private var settingsPersistJob: Job? = null

    init {
        // Wire delegate callbacks
        playbackCtrl.onScanStatusMessage = { setScanStatusMessage(it) }
        playbackCtrl.onFullPlayerOpen = { _isFullPlayerOpen.value = true }
        equalizerCtrl.onSettingsChanged = { persistUserSettings() }
        libraryCtrl.setScanStatusMessage = { setScanStatusMessage(it) }

        // Player engine listeners
        playerEngine.setOnSongChangedListener { song ->
            lastFmScrobbledSongId = null
            if (!playbackCtrl.isRadioSong(song)) {
                viewModelScope.launch(Dispatchers.IO) {
                    repository.recordSongPlayed(song.id)
                    repository.updateLastPlayedState(song.id, currentPositionMs.value)
                    val settings = lastFmSettings.value
                    if (settings.enabled && settings.isAuthenticated) {
                        try { scrobbleQueueManager.updateNowPlaying(song) }
                        catch (e: CancellationException) { throw e }
                        catch (_: Exception) { }
                    }
                }
            }
        }
        playerEngine.setOnFavoriteToggleListener { song -> toggleFavorite(song) }

        // Position persistence loop
        playbackCtrl.startPositionPersistenceLoop()

        // Lyrics auto-loading on song change
        viewModelScope.launch {
            currentSong.collectLatest { song ->
                if (song != null && !playbackCtrl.isRadioSong(song)) {
                    libraryCtrl.setLyricsLoading(true)
                    libraryCtrl.setLyrics(null)
                    try {
                        val lyrics = libraryCtrl.loadLyricsForSong(song)
                        libraryCtrl.setLyrics(lyrics)
                    } catch (e: CancellationException) { throw e }
                    catch (e: Exception) { Log.e("MusicViewModel", "Error loading lyrics for ${song.title}", e) }
                    finally { libraryCtrl.setLyricsLoading(false) }
                } else {
                    libraryCtrl.setLyrics(null)
                    libraryCtrl.setLyricsLoading(false)
                }
            }
        }

        // Last.fm scrobbling (50% / 4min threshold)
        viewModelScope.launch {
            combine(currentSong, currentPositionMs, durationMs, lastFmSettings) { song, pos, dur, settings ->
                Triple(song, Pair(pos, dur), settings)
            }.collect { state ->
                val song = state.first ?: return@collect
                if (playbackCtrl.isRadioSong(song)) return@collect
                val (pos, dur) = state.second
                val settings = state.third
                val threshold = minOf(dur / 2L, 240_000L)
                if (dur >= 30_000L && pos >= threshold.coerceAtLeast(30_000L) &&
                    settings.enabled && settings.isAuthenticated && lastFmScrobbledSongId != song.id
                ) {
                    lastFmScrobbledSongId = song.id
                    try { scrobbleQueueManager.queueScrobble(song) }
                    catch (e: CancellationException) { throw e }
                    catch (e: Exception) {
                        settingsCtrl.lastFmMessage.let { /* message already set */ }
                    }
                }
            }
        }

        // Initialize default queue when songs load
        viewModelScope.launch {
            allSongs.collect { songs ->
                if (_isLoading.value) _isLoading.value = false
                if (songs.isNotEmpty() && currentSong.value == null) {
                    val saved = withContext(Dispatchers.IO) { repository.getUserSettingsOnce() }
                    val savedIndex = saved.lastPlayedSongId
                        ?.let { id -> songs.indexOfFirst { it.id == id }.takeIf { it >= 0 } } ?: 0
                    playerEngine.setQueue(songs, startIndex = savedIndex, autoPlay = false)
                    if (saved.lastPlayedSongId != null && songs.getOrNull(savedIndex)?.id == saved.lastPlayedSongId) {
                        playerEngine.seekTo(saved.lastPlaybackPositionMs)
                    }
                }
            }
        }

        // Observe user settings from Room for equalizer
        viewModelScope.launch {
            repository.userSettings.collect { settings ->
                if (settings != null) {
                    equalizerCtrl.updateFromSettings(settings)
                    playerEngine.setPlaybackSpeed(settings.playbackSpeed)
                    playerEngine.setCrossfadeSeconds(settings.crossfadeSeconds)
                    val restoredRepeatMode = try { RepeatMode.valueOf(settings.repeatMode) } catch (_: Exception) { RepeatMode.ALL }
                    playerEngine.setRepeatMode(restoredRepeatMode)
                    playerEngine.setShuffleMode(settings.isShuffle)
                }
            }
        }

        // Observe custom equalizer presets
        viewModelScope.launch {
            repository.customPresets.collect { customList -> equalizerCtrl.updateCustomPresets(customList) }
        }
    }

    // ──────────────────────────────────────────────────
    // UI Navigation / Dialog helpers
    // ──────────────────────────────────────────────────

    fun completeOnboarding() = settingsCtrl.completeOnboarding()

    fun handleShortcutAction(action: String) {
        viewModelScope.launch {
            delay(600)
            when (action) {
                "play_favorites" -> {
                    val favorites = withContext(Dispatchers.IO) { repository.favoriteSongs.first() }
                    if (favorites.isNotEmpty()) {
                        playbackCtrl.ensurePlaybackService()
                        playerEngine.setQueue(favorites, startIndex = 0, autoPlay = true)
                        _isFullPlayerOpen.value = true
                    } else {
                        setScanStatusMessage("Nenhuma música favorita encontrada.")
                        delay(3000); setScanStatusMessage(null)
                    }
                }
                "shuffle_all" -> {
                    val songs = withContext(Dispatchers.IO) { repository.allSongs.first() }
                    if (songs.isNotEmpty()) {
                        playbackCtrl.ensurePlaybackService()
                        playerEngine.setQueue(songs.shuffled(), startIndex = 0, autoPlay = true)
                        _isFullPlayerOpen.value = true
                    } else {
                        setScanStatusMessage("Biblioteca vazia.")
                        delay(3000); setScanStatusMessage(null)
                    }
                }
                "open_radio" -> { _selectedTab.value = 5 }
            }
        }
    }

    fun selectTab(index: Int) { _selectedTab.value = index.coerceIn(0, 5) }
    fun openFullPlayer() { _isFullPlayerOpen.value = true }
    fun closeFullPlayer() { _isFullPlayerOpen.value = false }
    fun openPlaylistDetail(playlist: PlaylistWithSongs) { _activePlaylistDetail.value = playlist }
    fun closePlaylistDetail() { _activePlaylistDetail.value = null }
    fun showCreatePlaylistDialog() { _showCreatePlaylistDialog.value = true }
    fun dismissCreatePlaylistDialog() { _showCreatePlaylistDialog.value = false }
    fun showAddToPlaylistDialog(song: Song) { _showAddToPlaylistDialog.value = song }
    fun dismissAddToPlaylistDialog() { _showAddToPlaylistDialog.value = null }
    fun showSleepTimerDialog() { _showSleepTimerDialog.value = true }
    fun dismissSleepTimerDialog() { _showSleepTimerDialog.value = false }
    fun showSpeedDialog() { _showSpeedDialog.value = true }
    fun dismissSpeedDialog() { _showSpeedDialog.value = false }
    fun showTrackOptions(song: Song) { _showTrackOptionsSheet.value = song }
    fun dismissTrackOptions() { _showTrackOptionsSheet.value = null }
    fun notifyScanStatusMessage(message: String?) { _scanStatusMessage.value = message }
    fun showLastFmDialog() { _showLastFmDialog.value = true }
    fun dismissLastFmDialog() { _showLastFmDialog.value = false }
    fun showLyricsEditor() { _showLyricsEditor.value = true }
    fun dismissLyricsEditor() { _showLyricsEditor.value = false }

    // ──────────────────────────────────────────────────
    // Statistics
    // ──────────────────────────────────────────────────

    fun loadStatistics() {
        viewModelScope.launch {
            _isStatisticsLoading.value = true
            try { _statistics.value = repository.getListeningStatistics() }
            catch (e: CancellationException) { throw e }
            catch (e: Exception) { Log.e("MusicViewModel", "Error loading statistics", e) }
            finally { _isStatisticsLoading.value = false }
        }
    }

    // ──────────────────────────────────────────────────
    // Settings persistence
    // ──────────────────────────────────────────────────

    private fun persistUserSettings() {
        settingsPersistJob?.cancel()
        settingsPersistJob = viewModelScope.launch {
            delay(250)
            val currentTheme = themeConfig.value
            val currentEq = equalizerState.value
            val bands = currentEq.bandLevels
            val entity = UserSettingsEntity(
                id = 1,
                themeName = currentTheme.presetTheme.name,
                visualizerStyle = currentTheme.visualizerStyle.name,
                albumArtStyle = currentTheme.albumArtStyle.name,
                dynamicColors = currentTheme.dynamicColors,
                equalizerEnabled = currentEq.isEnabled,
                currentPresetId = currentEq.currentPresetId,
                band0 = bands.getOrElse(0) { 0 }, band1 = bands.getOrElse(1) { 0 },
                band2 = bands.getOrElse(2) { 0 }, band3 = bands.getOrElse(3) { 0 },
                band4 = bands.getOrElse(4) { 0 },
                bassBoost = currentEq.bassBoost, virtualizer = currentEq.virtualizer,
                balance = currentEq.balance,
                playbackSpeed = playbackSpeed.value, crossfadeSeconds = crossfadeSeconds.value,
                repeatMode = repeatMode.value.name, isShuffle = isShuffle.value,
                lastPlayedSongId = currentSong.value?.takeUnless { playbackCtrl.isRadioSong(it) }?.id,
                lastPlaybackPositionMs = currentPositionMs.value
            )
            repository.saveUserSettings(entity)
        }
    }

    private fun isRadioSong(song: Song) = playbackCtrl.isRadioSong(song)

    // ──────────────────────────────────────────────────
    // Playback Controls (delegate)
    // ──────────────────────────────────────────────────

    fun playSongFromList(songs: List<Song>, startIndex: Int) = playbackCtrl.playSongFromList(songs, startIndex)
    fun playSong(song: Song) = playbackCtrl.playSong(song)
    fun togglePlayPause() = playbackCtrl.togglePlayPause()
    fun skipToNext() = playbackCtrl.skipToNext()
    fun playNext() = playbackCtrl.playNext()
    fun skipToPrevious() = playbackCtrl.skipToPrevious()
    fun playPrevious() = playbackCtrl.playPrevious()
    fun seekTo(positionMs: Long) = playbackCtrl.seekTo(positionMs)
    fun clearPlaybackError() = playbackCtrl.clearPlaybackError()
    fun toggleRepeatMode() { playbackCtrl.toggleRepeatMode(); persistUserSettings() }
    fun cycleRepeatMode() = toggleRepeatMode()
    fun toggleShuffle() { playbackCtrl.toggleShuffle(); persistUserSettings() }
    fun setPlaybackSpeed(speed: Float) { playbackCtrl.setPlaybackSpeed(speed); persistUserSettings() }
    fun setCrossfadeSeconds(seconds: Int) { playbackCtrl.setCrossfadeSeconds(seconds); persistUserSettings() }
    fun toggleFavorite(song: Song) = playbackCtrl.toggleFavorite(song)

    fun playRadio(title: String, category: String, coverUri: String, radioId: String?, streamUrl: String? = null) =
        playbackCtrl.playRadio(title, category, coverUri, radioId, streamUrl)

    fun updateSongMetadata(song: Song, title: String, artist: String, album: String, genre: String, coverUri: String?) =
        playbackCtrl.updateSongMetadata(song, title, artist, album, genre, coverUri)

    // ──────────────────────────────────────────────────
    // Equalizer Controls (delegate)
    // ──────────────────────────────────────────────────

    fun toggleEqualizer(enabled: Boolean) = equalizerCtrl.toggleEqualizer(enabled)
    fun setEqualizerEnabled(enabled: Boolean) = toggleEqualizer(enabled)
    fun selectEqualizerPreset(preset: EqualizerPreset) = equalizerCtrl.selectPreset(preset)
    fun setBandLevel(bandIndex: Int, level: Int) = equalizerCtrl.setBandLevel(bandIndex, level)
    fun setEqualizerBandGain(bandIndex: Int, gain: Int) = setBandLevel(bandIndex, gain)
    fun resetEqualizer() = equalizerCtrl.resetEqualizer()
    fun setBassBoost(value: Int) = equalizerCtrl.setBassBoost(value)
    fun setVirtualizer(value: Int) = equalizerCtrl.setVirtualizer(value)
    fun setBalance(value: Float) = equalizerCtrl.setBalance(value)
    fun saveCustomEqualizerPreset(name: String) = equalizerCtrl.saveCustomPreset(name)
    fun saveCurrentAsCustomPreset(name: String) = saveCustomEqualizerPreset(name)
    fun deleteCustomEqualizerPreset(presetId: String) = equalizerCtrl.deleteCustomPreset(presetId)
    fun deleteCustomPreset(preset: EqualizerPreset) = deleteCustomEqualizerPreset(preset.id)
    fun deleteCustomPreset(presetId: String) = deleteCustomEqualizerPreset(presetId)

    // ──────────────────────────────────────────────────
    // Theme Controls (delegate)
    // ──────────────────────────────────────────────────

    fun setThemeMode(mode: ThemeMode) { settingsCtrl.setThemeMode(mode); persistUserSettings() }
    fun setPresetTheme(theme: AppThemeType) { settingsCtrl.setPresetTheme(theme); persistUserSettings() }
    fun setTheme(theme: AppThemeType) = setPresetTheme(theme)
    fun setCustomTheme(primary: Color, secondary: Color, tertiary: Color = Color(0xFFFF007F), surface: Color, background: Color, isDark: Boolean = true) {
        settingsCtrl.setCustomTheme(primary, secondary, tertiary, surface, background, isDark); persistUserSettings()
    }
    fun setDynamicColors(enabled: Boolean) { settingsCtrl.setDynamicColors(enabled); persistUserSettings() }
    fun setVisualizerStyle(style: VisualizerStyle) { settingsCtrl.setVisualizerStyle(style); persistUserSettings() }
    fun setAlbumArtStyle(style: AlbumArtStyle) { settingsCtrl.setAlbumArtStyle(style); persistUserSettings() }
    fun resetThemeSettings() { settingsCtrl.resetThemeSettings(); persistUserSettings() }

    // ──────────────────────────────────────────────────
    // Last.fm Controls (delegate)
    // ──────────────────────────────────────────────────

    fun requestLastFmAuthorization() = settingsCtrl.requestLastFmAuthorization()
    fun completeLastFmAuthorization() = settingsCtrl.completeLastFmAuthorization()
    fun saveLastFmCredentials(apiKey: String, apiSecret: String) = settingsCtrl.saveLastFmCredentials(apiKey, apiSecret)
    fun setLastFmEnabled(enabled: Boolean) = settingsCtrl.setLastFmEnabled(enabled)
    fun disconnectLastFm() = settingsCtrl.disconnectLastFm()
    fun clearPendingScrobbles() = settingsCtrl.clearPendingScrobbles()
    fun processPendingScrobbles() = settingsCtrl.processPendingScrobbles()

    // ──────────────────────────────────────────────────
    // Library Controls (delegate)
    // ──────────────────────────────────────────────────

    fun setSearchQuery(query: String) = libraryCtrl.setSearchQuery(query)
    fun setSortOption(option: SortOption) = libraryCtrl.setSortOption(option)
    fun deleteSong(song: Song) = libraryCtrl.deleteSong(song) { setScanStatusMessage(it) }
    fun relinkSong(context: Context, song: Song, uri: Uri) = libraryCtrl.relinkSong(context, song, uri) { setScanStatusMessage(it) }
    fun scanLocalStorage(context: Context) = libraryCtrl.scanLocalStorage(context) { setScanStatusMessage(it) }
    fun importSingleAudio(context: Context, uri: Uri, fileName: String) = libraryCtrl.importSingleAudio(context, uri, fileName) { setScanStatusMessage(it) }
    fun importAudioFiles(context: Context, uris: List<Uri>) = libraryCtrl.importAudioFiles(context, uris) { setScanStatusMessage(it) }
    fun importAudioFolder(context: Context, folderUri: Uri) = libraryCtrl.importAudioFolder(context, folderUri) { setScanStatusMessage(it) }

    // ──────────────────────────────────────────────────
    // Playlist Controls (delegate)
    // ──────────────────────────────────────────────────

    fun createPlaylist(name: String, description: String = "", iconName: String = "playlist_play", gradientIndex: Int = 0) =
        libraryCtrl.createPlaylist(name, description, iconName, gradientIndex)
    fun addSongToPlaylist(playlistId: Long, songId: Long) = libraryCtrl.addSongToPlaylist(playlistId, songId)
    fun removeSongFromPlaylist(playlistId: Long, songId: Long) = libraryCtrl.removeSongFromPlaylist(playlistId, songId)
    fun deletePlaylist(playlist: Playlist) {
        libraryCtrl.deletePlaylist(playlist)
        if (_activePlaylistDetail.value?.playlist?.id == playlist.id) _activePlaylistDetail.value = null
    }

    // ──────────────────────────────────────────────────
    // Sleep Timer (delegate)
    // ──────────────────────────────────────────────────

    fun setSleepTimer(minutes: Int) = playbackCtrl.setSleepTimer(minutes)
    fun setSleepTimerUntilTrackEnd() = playbackCtrl.setSleepTimerUntilTrackEnd()
    fun cancelSleepTimer() = playbackCtrl.cancelSleepTimer()

    // ──────────────────────────────────────────────────
    // Lyrics (delegate)
    // ──────────────────────────────────────────────────

    fun toggleLyricsView() = libraryCtrl.toggleLyricsView()
    fun refreshLyrics() {
        val song = currentSong.value ?: return
        libraryCtrl.refreshLyrics(song)
    }
    fun saveLyrics(rawLrc: String) {
        val song = currentSong.value ?: return
        libraryCtrl.saveLyrics(song, rawLrc) { dismissLyricsEditor() }
    }

    // ──────────────────────────────────────────────────
    // Backup (delegate)
    // ──────────────────────────────────────────────────

    fun exportBackup(context: Context, uri: Uri) = settingsCtrl.exportBackup(getApplication(), uri) { setScanStatusMessage(it) }
    fun restoreBackup(context: Context, uri: Uri) {
        settingsCtrl.restoreBackup(getApplication(), uri) { setScanStatusMessage(it) }
    }

    // ──────────────────────────────────────────────────
    // Pull-to-refresh
    // ──────────────────────────────────────────────────

    fun refreshLibrary() {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                val ctx = getApplication<Application>().applicationContext
                if (!libraryCtrl.hasAudioPermission(ctx)) {
                    setScanStatusMessage("Permissão de áudio necessária para escanear músicas.")
                    return@launch
                }
                withContext(Dispatchers.IO) { repository.scanDeviceAudio(ctx) }
                delay(800)
            } catch (e: CancellationException) { throw e }
            catch (e: Exception) {
                Log.e("MusicViewModel", "Error refreshing local storage", e)
                setScanStatusMessage(e.message ?: "Falha ao atualizar as músicas.")
            } finally { _isRefreshing.value = false }
        }
    }

    // ──────────────────────────────────────────────────
    // Factory & Lifecycle
    // ──────────────────────────────────────────────────

    class Factory(
        private val application: Application,
        private val repository: MusicRepository,
        private val themeDataStore: ThemePreferencesDataStore,
        private val lastFmDataStore: LastFmPreferencesDataStore,
        private val backupManager: BackupManager,
        private val playerEngine: AudioPlayerEngine,
        private val scrobbleQueueManager: ScrobbleQueueManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (!modelClass.isAssignableFrom(MusicViewModel::class.java)) {
                throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
            return MusicViewModel(
                application = application, repository = repository,
                themeDataStore = themeDataStore, lastFmDataStore = lastFmDataStore,
                backupManager = backupManager, playerEngine = playerEngine,
                scrobbleQueueManager = scrobbleQueueManager
            ) as T
        }
    }

    override fun onCleared() {
        playerEngine.setOnSongChangedListener(null)
        playerEngine.setOnFavoriteToggleListener(null)
        settingsPersistJob?.cancel()
        playbackCtrl.onCleared()
        super.onCleared()
    }
}
