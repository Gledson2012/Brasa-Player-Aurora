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
import com.example.data.lastfm.LastFmClient
import com.example.data.lyrics.LyricsManager
import com.example.data.lyrics.TrackLyrics
import com.example.data.model.AlbumArtStyle
import com.example.data.model.AppThemeType
import com.example.data.model.EqualizerPreset
import com.example.data.model.EqualizerState
import com.example.data.model.LastFmSettings
import com.example.data.model.LyricsEntity
import com.example.data.model.Playlist
import com.example.data.model.PlaylistWithSongs
import com.example.data.model.Song
import com.example.data.model.ThemeConfig
import com.example.data.model.ThemeMode
import com.example.data.model.UserSettingsEntity
import com.example.data.model.VisualizerStyle
import com.example.data.repository.MusicRepository
import com.example.service.MusicPlaybackService
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class SortOption(val title: String) {
    TITLE("Título (A-Z)"),
    ARTIST("Artista (A-Z)"),
    DURATION("Duração"),
    RECENTLY_ADDED("Adicionadas Recentemente")
}

class MusicViewModel(
    application: Application,
    private val repository: MusicRepository
) : AndroidViewModel(application) {

    private val playerEngine = AudioPlayerEngine.getOrCreateInstance(application.applicationContext)
    private val themeDataStore = ThemePreferencesDataStore(application.applicationContext)
    private val lastFmDataStore = LastFmPreferencesDataStore(application.applicationContext)
    private val backupManager = BackupManager(repository, themeDataStore)

    // Player engine state exposures
    val isPlaying: StateFlow<Boolean> = playerEngine.isPlaying
    val currentSong: StateFlow<Song?> = playerEngine.currentSong
    val currentPositionMs: StateFlow<Long> = playerEngine.currentPositionMs
    val durationMs: StateFlow<Long> = playerEngine.durationMs
    val queue: StateFlow<List<Song>> = playerEngine.queue
    val repeatMode: StateFlow<RepeatMode> = playerEngine.repeatMode
    val isShuffle: StateFlow<Boolean> = playerEngine.isShuffle
    val playbackSpeed: StateFlow<Float> = playerEngine.playbackSpeed
    val crossfadeSeconds: StateFlow<Int> = playerEngine.crossfadeSeconds
    val visualizerAmplitudes: StateFlow<FloatArray> = playerEngine.visualizerAmplitudes
    val waveformSamples: StateFlow<List<Float>> = playerEngine.waveformSamples
    val playbackError: StateFlow<String?> = playerEngine.playbackError

    // Search and Filtering (encapsulated)
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _sortOption = MutableStateFlow(SortOption.TITLE)
    val sortOption: StateFlow<SortOption> = _sortOption.asStateFlow()

    // DB Songs Flow
    val allSongs: StateFlow<List<Song>> = repository.allSongs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteSongs: StateFlow<List<Song>> = repository.favoriteSongs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentlyPlayed: StateFlow<List<Song>> = repository.recentlyPlayed
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val mostPlayed: StateFlow<List<Song>> = repository.mostPlayed
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPlaylistsWithSongs: StateFlow<List<PlaylistWithSongs>> = repository.allPlaylistsWithSongs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Real-Time Filtered & Sorted Tracks using Room Database
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class, kotlinx.coroutines.FlowPreview::class)
    val displayedSongs: StateFlow<List<Song>> = combine(
        searchQuery.debounce(250),
        sortOption
    ) { query, sort ->
        Pair(query.trim(), sort)
    }.flatMapLatest { (query, sort) ->
        repository.searchAndSortSongs(query, sort.name)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Equalizer State
    private val _equalizerState = MutableStateFlow(EqualizerState())
    val equalizerState: StateFlow<EqualizerState> = _equalizerState.asStateFlow()

    // Themes & Settings (Persisted via DataStore)
    val themeConfig: StateFlow<ThemeConfig> = themeDataStore.themeConfigFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, ThemeConfig())

    val themeSettings: StateFlow<ThemeConfig> = themeConfig

    val lastFmSettings: StateFlow<LastFmSettings> = lastFmDataStore.settingsFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, LastFmSettings())

    // Sleep Timer (Managed by AudioPlayerEngine for persistent background playback)
    val sleepTimerRemainingSeconds: StateFlow<Int?> = playerEngine.sleepTimerRemainingSeconds
    val sleepTimerEndAtTrackEnd: StateFlow<Boolean> = playerEngine.sleepTimerEndAtTrackEnd

    // ---- Encapsulated UI Navigation & Dialog States ----
    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    private val _isFullPlayerOpen = MutableStateFlow(false)
    val isFullPlayerOpen: StateFlow<Boolean> = _isFullPlayerOpen.asStateFlow()

    private val _activePlaylistDetail = MutableStateFlow<PlaylistWithSongs?>(null)
    val activePlaylistDetail: StateFlow<PlaylistWithSongs?> = _activePlaylistDetail.asStateFlow()

    private val _showCreatePlaylistDialog = MutableStateFlow(false)
    val showCreatePlaylistDialog: StateFlow<Boolean> = _showCreatePlaylistDialog.asStateFlow()

    private val _showAddToPlaylistDialog = MutableStateFlow<Song?>(null)
    val showAddToPlaylistDialog: StateFlow<Song?> = _showAddToPlaylistDialog.asStateFlow()

    private val _showSleepTimerDialog = MutableStateFlow(false)
    val showSleepTimerDialog: StateFlow<Boolean> = _showSleepTimerDialog.asStateFlow()

    private val _showSpeedDialog = MutableStateFlow(false)
    val showSpeedDialog: StateFlow<Boolean> = _showSpeedDialog.asStateFlow()

    private val _showTrackOptionsSheet = MutableStateFlow<Song?>(null)
    val showTrackOptionsSheet: StateFlow<Song?> = _showTrackOptionsSheet.asStateFlow()

    private val _scanStatusMessage = MutableStateFlow<String?>(null)
    val scanStatusMessage: StateFlow<String?> = _scanStatusMessage.asStateFlow()

    private val _lastFmMessage = MutableStateFlow<String?>(null)
    val lastFmMessage: StateFlow<String?> = _lastFmMessage.asStateFlow()

    private val _lastFmAuthUrl = MutableStateFlow<String?>(null)
    val lastFmAuthUrl: StateFlow<String?> = _lastFmAuthUrl.asStateFlow()

    private val _showLastFmDialog = MutableStateFlow(false)
    val showLastFmDialog: StateFlow<Boolean> = _showLastFmDialog.asStateFlow()

    private val _showLyricsEditor = MutableStateFlow(false)
    val showLyricsEditor: StateFlow<Boolean> = _showLyricsEditor.asStateFlow()

    private val _isLyricsViewActive = MutableStateFlow(false)
    val isLyricsViewActive: StateFlow<Boolean> = _isLyricsViewActive.asStateFlow()

    // ---- Synced Lyrics State ----
    private val _currentLyrics = MutableStateFlow<TrackLyrics?>(null)
    val currentLyrics: StateFlow<TrackLyrics?> = _currentLyrics.asStateFlow()

    private val _isLyricsLoading = MutableStateFlow(false)
    val isLyricsLoading: StateFlow<Boolean> = _isLyricsLoading.asStateFlow()

    private var lastFmScrobbledSongId: Long? = null

    // Coalesced playback-position persistence: avoids racing writes and redundant DB hits.
    private var persistJob: Job? = null
    private var settingsPersistJob: Job? = null
    private var lastPersistedSongId: Long? = null
    private var lastPersistedPositionMs: Long = Long.MIN_VALUE

    init {
        playerEngine.setOnSongChangedListener { song ->
            lastFmScrobbledSongId = null
            viewModelScope.launch {
                repository.recordSongPlayed(song.id)
                repository.updateLastPlayedState(song.id, currentPositionMs.value)
                val settings = lastFmSettings.value
                if (settings.enabled && settings.isAuthenticated) {
                    try {
                        LastFmClient(settings).updateNowPlaying(song)
                    } catch (_: Exception) {
                        // Playback must not depend on Last.fm availability.
                    }
                }
            }
        }
        playerEngine.setOnFavoriteToggleListener { song ->
            toggleFavorite(song)
        }

        // Keep resume information fresh even when the user leaves the app without
        // changing another setting. The position is written at most once per interval.
        viewModelScope.launch {
            while (isActive) {
                delay(10_000)
                persistPlaybackPosition()
            }
        }

        // Observe current song and fetch synchronized lyrics
        viewModelScope.launch {
            currentSong.collectLatest { song ->
                if (song != null) {
                    _isLyricsLoading.value = true
                    _currentLyrics.value = null
                    try {
                        val lyrics = loadLyrics(song)
                        _currentLyrics.value = lyrics
                    } catch (e: Exception) {
                        Log.e("MusicViewModel", "Error loading lyrics for ${song.title}", e)
                    } finally {
                        _isLyricsLoading.value = false
                    }
                } else {
                    _currentLyrics.value = null
                    _isLyricsLoading.value = false
                }
            }
        }

        // Last.fm scrobbling follows the standard 50% / four-minute threshold.
        viewModelScope.launch {
            combine(currentSong, currentPositionMs, durationMs, lastFmSettings) { song, position, duration, settings ->
                ScrobbleState(song, position, duration, settings)
            }.collect { state ->
                val song = state.song ?: return@collect
                val settings = state.settings
                val threshold = minOf(state.durationMs / 2L, 240_000L)
                if (
                    state.durationMs >= 30_000L &&
                    state.positionMs >= threshold.coerceAtLeast(30_000L) &&
                    settings.enabled && settings.isAuthenticated &&
                    lastFmScrobbledSongId != song.id
                ) {
                    lastFmScrobbledSongId = song.id
                    try {
                        LastFmClient(settings).scrobble(song)
                    } catch (e: Exception) {
                        setLastFmMessage("Last.fm: ${e.message ?: "falha ao enviar scrobble"}")
                    }
                }
            }
        }

        // Initialize default queue when songs load
        viewModelScope.launch {
            allSongs.collect { songs ->
                if (songs.isNotEmpty() && currentSong.value == null) {
                    val saved = repository.getUserSettingsOnce()
                    val savedIndex = saved.lastPlayedSongId
                        ?.let { id -> songs.indexOfFirst { it.id == id }.takeIf { it >= 0 } }
                        ?: 0
                    playerEngine.setQueue(songs, startIndex = savedIndex, autoPlay = false)
                    if (saved.lastPlayedSongId != null && songs.getOrNull(savedIndex)?.id == saved.lastPlayedSongId) {
                        playerEngine.seekTo(saved.lastPlaybackPositionMs)
                    }
                }
            }
        }

        // Observe and load user settings from Room database for equalizer
        viewModelScope.launch {
            repository.userSettings.collect { settings ->
                if (settings != null) {
                    val eq = EqualizerState(
                        isEnabled = settings.equalizerEnabled,
                        currentPresetId = settings.currentPresetId,
                        bandLevels = listOf(settings.band0, settings.band1, settings.band2, settings.band3, settings.band4),
                        bassBoost = settings.bassBoost,
                        virtualizer = settings.virtualizer,
                        balance = settings.balance
                    )
                    _equalizerState.value = eq
                    playerEngine.syncEqualizer(eq)
                    playerEngine.setPlaybackSpeed(settings.playbackSpeed)
                    playerEngine.setCrossfadeSeconds(settings.crossfadeSeconds)
                    val restoredRepeatMode = try {
                        RepeatMode.valueOf(settings.repeatMode)
                    } catch (_: Exception) {
                        RepeatMode.ALL
                    }
                    playerEngine.setRepeatMode(restoredRepeatMode)
                    playerEngine.setShuffleMode(settings.isShuffle)
                }
            }
        }

        // Observe custom equalizer presets from Room
        viewModelScope.launch {
            repository.customPresets.collect { customList ->
                val converted = customList.map {
                    EqualizerPreset(
                        id = "custom_${it.id}",
                        name = it.name,
                        bandLevels = listOf(it.band0, it.band1, it.band2, it.band3, it.band4),
                        bassBoost = it.bassBoost,
                        virtualizer = it.virtualizer,
                        isCustom = true
                    )
                }
                _equalizerState.value = _equalizerState.value.copy(customPresets = converted)
            }
        }
    }

    // ---- UI Navigation / Dialog helpers ----
    fun selectTab(index: Int) {
        _selectedTab.value = index.coerceIn(0, 3)
    }

    fun openFullPlayer() {
        _isFullPlayerOpen.value = true
    }

    fun closeFullPlayer() {
        _isFullPlayerOpen.value = false
    }

    fun openPlaylistDetail(playlist: PlaylistWithSongs) {
        _activePlaylistDetail.value = playlist
    }

    fun closePlaylistDetail() {
        _activePlaylistDetail.value = null
    }

    fun showCreatePlaylistDialog() {
        _showCreatePlaylistDialog.value = true
    }

    fun dismissCreatePlaylistDialog() {
        _showCreatePlaylistDialog.value = false
    }

    fun showAddToPlaylistDialog(song: Song) {
        _showAddToPlaylistDialog.value = song
    }

    fun dismissAddToPlaylistDialog() {
        _showAddToPlaylistDialog.value = null
    }

    fun showSleepTimerDialog() {
        _showSleepTimerDialog.value = true
    }

    fun dismissSleepTimerDialog() {
        _showSleepTimerDialog.value = false
    }

    fun showSpeedDialog() {
        _showSpeedDialog.value = true
    }

    fun dismissSpeedDialog() {
        _showSpeedDialog.value = false
    }

    fun showTrackOptions(song: Song) {
        _showTrackOptionsSheet.value = song
    }

    fun dismissTrackOptions() {
        _showTrackOptionsSheet.value = null
    }

    fun notifyScanStatusMessage(message: String?) {
        _scanStatusMessage.value = message
    }

    private fun setScanStatusMessage(message: String?) {
        _scanStatusMessage.value = message
    }

    private fun setLastFmMessage(message: String?) {
        _lastFmMessage.value = message
    }

    fun showLastFmDialog() {
        _showLastFmDialog.value = true
    }

    fun dismissLastFmDialog() {
        _showLastFmDialog.value = false
    }

    fun showLyricsEditor() {
        _showLyricsEditor.value = true
    }

    fun dismissLyricsEditor() {
        _showLyricsEditor.value = false
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSortOption(option: SortOption) {
        _sortOption.value = option
    }

    private fun persistUserSettings() {
        settingsPersistJob?.cancel()
        settingsPersistJob = viewModelScope.launch {
            delay(250)
            val currentTheme = themeConfig.value
            val currentEq = _equalizerState.value
            val bands = currentEq.bandLevels
            val entity = UserSettingsEntity(
                id = 1,
                themeName = currentTheme.presetTheme.name,
                visualizerStyle = currentTheme.visualizerStyle.name,
                albumArtStyle = currentTheme.albumArtStyle.name,
                dynamicColors = currentTheme.dynamicColors,
                equalizerEnabled = currentEq.isEnabled,
                currentPresetId = currentEq.currentPresetId,
                band0 = bands.getOrElse(0) { 0 },
                band1 = bands.getOrElse(1) { 0 },
                band2 = bands.getOrElse(2) { 0 },
                band3 = bands.getOrElse(3) { 0 },
                band4 = bands.getOrElse(4) { 0 },
                bassBoost = currentEq.bassBoost,
                virtualizer = currentEq.virtualizer,
                balance = currentEq.balance,
                playbackSpeed = playbackSpeed.value,
                crossfadeSeconds = crossfadeSeconds.value,
                repeatMode = repeatMode.value.name,
                isShuffle = isShuffle.value,
                lastPlayedSongId = currentSong.value?.id,
                lastPlaybackPositionMs = currentPositionMs.value
            )
            repository.saveUserSettings(entity)
        }
    }

    // Playback Controls
    fun playSongFromList(songs: List<Song>, startIndex: Int) {
        ensurePlaybackService()
        schedulePersistPlaybackPosition()
        playerEngine.setQueue(songs, startIndex = startIndex, autoPlay = true)
    }

    fun playSong(song: Song) {
        ensurePlaybackService()
        val currentQueue = queue.value
        if (currentQueue.none { it.id == song.id }) {
            playerEngine.setQueue(listOf(song) + currentQueue, 0, true)
        } else {
            playerEngine.playSong(song)
        }
    }

    fun updateSongMetadata(
        song: Song,
        title: String,
        artist: String,
        album: String,
        genre: String,
        coverUri: String?
    ) {
        val updatedSong = song.copy(
            title = title.trim().ifBlank { song.title },
            artist = artist.trim().ifBlank { "Artista Desconhecido" },
            album = album.trim().ifBlank { "Álbum Desconhecido" },
            genre = genre.trim().ifBlank { "Geral" },
            coverUri = coverUri?.trim()?.takeIf { it.isNotEmpty() }
        )
        viewModelScope.launch {
            repository.updateSong(updatedSong)
            playerEngine.updateSongMetadata(updatedSong)
        }
    }

    fun deleteSong(song: Song) {
        viewModelScope.launch {
            try {
                repository.deleteSong(song)
                LyricsManager.clearCache(song.id)
                playerEngine.removeSong(song.id)
                setScanStatusMessage("Música removida da biblioteca.")
            } catch (e: Exception) {
                Log.e("MusicViewModel", "Error deleting song ${song.id}", e)
                setScanStatusMessage(e.message ?: "Não foi possível remover a música.")
            }
            delay(3500)
            setScanStatusMessage(null)
        }
    }

    fun relinkSong(context: Context, song: Song, uri: Uri) {
        viewModelScope.launch {
            try {
                val updated = repository.relinkSong(context, song.id, uri)
                playerEngine.updateSongMetadata(updated)
                setScanStatusMessage("Arquivo de \"${song.title}\" atualizado.")
            } catch (e: Exception) {
                Log.e("MusicViewModel", "Error relinking audio ${song.id}", e)
                setScanStatusMessage(e.message ?: "Não foi possível atualizar o arquivo de áudio.")
            }
            delay(3500)
            setScanStatusMessage(null)
        }
    }

    fun togglePlayPause() {
        ensurePlaybackService()
        playerEngine.togglePlayPause()
        schedulePersistPlaybackPosition()
    }

    fun skipToNext() {
        ensurePlaybackService()
        schedulePersistPlaybackPosition()
        playerEngine.playNext()
    }

    fun playNext() {
        ensurePlaybackService()
        schedulePersistPlaybackPosition()
        playerEngine.playNext()
    }

    fun skipToPrevious() {
        ensurePlaybackService()
        schedulePersistPlaybackPosition()
        playerEngine.playPrevious()
    }

    fun playPrevious() {
        ensurePlaybackService()
        schedulePersistPlaybackPosition()
        playerEngine.playPrevious()
    }

    fun seekTo(positionMs: Long) {
        ensurePlaybackService()
        playerEngine.seekTo(positionMs)
        schedulePersistPlaybackPosition()
    }

    fun clearPlaybackError() {
        playerEngine.clearPlaybackError()
    }

    /** The service owns the MediaSession; the engine only owns audio state. */
    private fun ensurePlaybackService() {
        MusicPlaybackService.startService(getApplication<Application>())
    }

    fun toggleRepeatMode() {
        playerEngine.cycleRepeatMode()
        persistUserSettings()
    }

    fun cycleRepeatMode() {
        toggleRepeatMode()
    }

    fun toggleShuffle() {
        playerEngine.toggleShuffle()
        persistUserSettings()
    }

    fun setPlaybackSpeed(speed: Float) {
        playerEngine.setPlaybackSpeed(speed)
        persistUserSettings()
    }

    fun setCrossfadeSeconds(seconds: Int) {
        playerEngine.setCrossfadeSeconds(seconds)
        persistUserSettings()
    }

    fun toggleFavorite(song: Song) {
        viewModelScope.launch {
            repository.toggleFavorite(song)
        }
    }

    // Equalizer Operations
    fun toggleEqualizer(enabled: Boolean) {
        val updated = _equalizerState.value.copy(isEnabled = enabled)
        _equalizerState.value = updated
        playerEngine.syncEqualizer(updated)
        persistUserSettings()
    }

    fun setEqualizerEnabled(enabled: Boolean) {
        toggleEqualizer(enabled)
    }

    fun selectEqualizerPreset(preset: EqualizerPreset) {
        val updated = _equalizerState.value.copy(
            currentPresetId = preset.id,
            bandLevels = preset.bandLevels,
            bassBoost = preset.bassBoost,
            virtualizer = preset.virtualizer
        )
        _equalizerState.value = updated
        playerEngine.syncEqualizer(updated)
        persistUserSettings()
    }

    fun setBandLevel(bandIndex: Int, level: Int) {
        val currentBands = _equalizerState.value.bandLevels.toMutableList()
        if (bandIndex in currentBands.indices) {
            currentBands[bandIndex] = level
            val updated = _equalizerState.value.copy(
                bandLevels = currentBands,
                currentPresetId = "custom_user"
            )
            _equalizerState.value = updated
            playerEngine.syncEqualizer(updated)
            persistUserSettings()
        }
    }

    fun setEqualizerBandGain(bandIndex: Int, gain: Int) {
        setBandLevel(bandIndex, gain)
    }

    fun resetEqualizer() {
        val resetState = EqualizerState()
        _equalizerState.value = resetState
        playerEngine.syncEqualizer(resetState)
        persistUserSettings()
    }

    fun setBassBoost(value: Int) {
        val updated = _equalizerState.value.copy(bassBoost = value)
        _equalizerState.value = updated
        playerEngine.syncEqualizer(updated)
        persistUserSettings()
    }

    fun setVirtualizer(value: Int) {
        val updated = _equalizerState.value.copy(virtualizer = value)
        _equalizerState.value = updated
        playerEngine.syncEqualizer(updated)
        persistUserSettings()
    }

    fun setBalance(value: Float) {
        val updated = _equalizerState.value.copy(balance = value)
        _equalizerState.value = updated
        playerEngine.syncEqualizer(updated)
        persistUserSettings()
    }

    fun saveCustomEqualizerPreset(name: String) {
        viewModelScope.launch {
            val currentEq = _equalizerState.value
            val bands = currentEq.bandLevels
            val entity = com.example.data.model.CustomPresetEntity(
                name = name,
                band0 = bands.getOrElse(0) { 0 },
                band1 = bands.getOrElse(1) { 0 },
                band2 = bands.getOrElse(2) { 0 },
                band3 = bands.getOrElse(3) { 0 },
                band4 = bands.getOrElse(4) { 0 },
                bassBoost = currentEq.bassBoost,
                virtualizer = currentEq.virtualizer
            )
            repository.saveCustomPreset(entity)
        }
    }

    fun saveCurrentAsCustomPreset(name: String) {
        saveCustomEqualizerPreset(name)
    }

    fun deleteCustomEqualizerPreset(presetId: String) {
        viewModelScope.launch {
            val longId = presetId.removePrefix("custom_").toLongOrNull()
            if (longId != null) {
                repository.deleteCustomPreset(longId)
            }
        }
    }

    fun deleteCustomPreset(preset: EqualizerPreset) {
        deleteCustomEqualizerPreset(preset.id)
    }

    fun deleteCustomPreset(presetId: String) {
        deleteCustomEqualizerPreset(presetId)
    }

    // Theme DataStore Operations
    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            themeDataStore.setThemeMode(mode)
            persistUserSettings()
        }
    }

    fun setPresetTheme(theme: AppThemeType) {
        viewModelScope.launch {
            themeDataStore.setPresetTheme(theme)
            persistUserSettings()
        }
    }

    fun setTheme(theme: AppThemeType) {
        setPresetTheme(theme)
    }

    fun setCustomTheme(
        primary: Color,
        secondary: Color,
        tertiary: Color = Color(0xFFFF007F),
        surface: Color,
        background: Color,
        isDark: Boolean = true
    ) {
        viewModelScope.launch {
            themeDataStore.setCustomTheme(primary, secondary, tertiary, surface, background, isDark)
            themeDataStore.setThemeMode(ThemeMode.CUSTOM)
            persistUserSettings()
        }
    }

    fun setDynamicColors(enabled: Boolean) {
        viewModelScope.launch {
            themeDataStore.setDynamicColors(enabled)
            persistUserSettings()
        }
    }

    fun setVisualizerStyle(style: VisualizerStyle) {
        viewModelScope.launch {
            themeDataStore.setVisualizerStyle(style)
            persistUserSettings()
        }
    }

    fun setAlbumArtStyle(style: AlbumArtStyle) {
        viewModelScope.launch {
            themeDataStore.setAlbumArtStyle(style)
            persistUserSettings()
        }
    }

    fun resetThemeSettings() {
        viewModelScope.launch {
            themeDataStore.resetToDefault()
            persistUserSettings()
        }
    }

    // Playlists Operations
    fun createPlaylist(name: String, description: String = "", iconName: String = "playlist_play", gradientIndex: Int = 0) {
        viewModelScope.launch {
            if (name.isNotBlank()) {
                repository.createPlaylist(name.trim(), description.trim(), iconName, gradientIndex)
            }
        }
    }

    fun addSongToPlaylist(playlistId: Long, songId: Long) {
        viewModelScope.launch {
            repository.addSongToPlaylist(playlistId, songId)
        }
    }

    fun removeSongFromPlaylist(playlistId: Long, songId: Long) {
        viewModelScope.launch {
            repository.removeSongFromPlaylist(playlistId, songId)
        }
    }

    fun deletePlaylist(playlist: Playlist) {
        viewModelScope.launch {
            repository.deletePlaylist(playlist)
            if (_activePlaylistDetail.value?.playlist?.id == playlist.id) {
                _activePlaylistDetail.value = null
            }
        }
    }

    // Sleep Timer
    fun setSleepTimer(minutes: Int) {
        playerEngine.setSleepTimer(minutes)
    }

    fun setSleepTimerUntilTrackEnd() {
        playerEngine.setSleepTimerUntilTrackEnd()
    }

    fun cancelSleepTimer() {
        playerEngine.cancelSleepTimer()
    }

    // Storage Scanner & Importer
    fun scanLocalStorage(context: Context) {
        viewModelScope.launch {
            setScanStatusMessage("Escaneando armazenamento...")
            try {
                val count = repository.scanDeviceAudio(context)
                if (count > 0) {
                    setScanStatusMessage("$count nova(s) música(s) importada(s)!")
                } else {
                    setScanStatusMessage("Nenhuma nova música encontrada no dispositivo.")
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("MusicViewModel", "Error scanning local storage", e)
                setScanStatusMessage(e.message ?: "Falha ao escanear as músicas.")
            }
            delay(3500)
            setScanStatusMessage(null)
        }
    }

    fun importSingleAudio(context: Context, uri: Uri, fileName: String) {
        viewModelScope.launch {
            val title = fileName.substringBeforeLast(".")
            try {
                repository.importAudioUri(context, uri, title)
                setScanStatusMessage("Música \"$title\" importada com sucesso!")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("MusicViewModel", "Error importing audio $uri", e)
                setScanStatusMessage(e.message ?: "Falha ao importar o arquivo de áudio.")
            }
            delay(3500)
            setScanStatusMessage(null)
        }
    }

    fun importAudioFiles(context: Context, uris: List<Uri>) {
        if (uris.isEmpty()) return

        viewModelScope.launch {
            var importedCount = 0
            var failedCount = 0
            setScanStatusMessage("Importando ${uris.size} arquivo(s)...")

            uris.forEachIndexed { index, uri ->
                try {
                    val displayName = uri.lastPathSegment
                        ?.substringAfterLast('/')
                        ?.let(Uri::decode)
                        ?.substringBeforeLast('.')
                        ?.takeIf { it.isNotBlank() }
                        ?: "Faixa importada ${index + 1}"
                    repository.importAudioUri(context, uri, displayName)
                    importedCount++
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    failedCount++
                    Log.e("MusicViewModel", "Error importing audio $uri", e)
                }
            }

            setScanStatusMessage(
                when {
                    failedCount == 0 -> "$importedCount música(s) adicionada(s) à biblioteca."
                    importedCount == 0 -> "Não foi possível abrir os arquivos selecionados."
                    else -> "$importedCount música(s) adicionada(s); $failedCount arquivo(s) não puderam ser lidos."
                }
            )
            delay(3500)
            setScanStatusMessage(null)
        }
    }

    // Lyrics Controls
    fun toggleLyricsView() {
        _isLyricsViewActive.value = !_isLyricsViewActive.value
    }

    fun refreshLyrics() {
        val song = currentSong.value ?: return
        viewModelScope.launch {
            _isLyricsLoading.value = true
            try {
                // LyricsManager caches misses as well as successful results.
                // Clear only the online/built-in cache here so a retry really
                // performs a new lookup; manually edited lyrics remain in Room.
                if (repository.getLyricsOnce(song.id) == null) {
                    LyricsManager.clearCache(song.id)
                }
                val lyrics = loadLyrics(song)
                _currentLyrics.value = lyrics
            } catch (e: Exception) {
                Log.e("MusicViewModel", "Error refreshing lyrics", e)
            } finally {
                _isLyricsLoading.value = false
            }
        }
    }

    private suspend fun loadLyrics(song: Song): TrackLyrics {
        val saved = repository.getLyricsOnce(song.id)
        return if (saved != null) {
            LyricsManager.parseLrc(song.id, saved.content, saved.source)
        } else {
            LyricsManager.getLyrics(song)
        }
    }

    fun saveLyrics(rawLrc: String) {
        val song = currentSong.value ?: return
        viewModelScope.launch {
            if (rawLrc.isBlank()) {
                repository.deleteLyrics(song.id)
                LyricsManager.clearCache(song.id)
                _currentLyrics.value = TrackLyrics(song.id, emptyList(), false, "Editor")
            } else {
                val parsed = LyricsManager.parseLrc(song.id, rawLrc, "Editor")
                repository.saveLyrics(LyricsEntity(song.id, rawLrc, parsed.isSynced, "Editor"))
                LyricsManager.cache(song.id, parsed)
                _currentLyrics.value = parsed
            }
            dismissLyricsEditor()
        }
    }

    fun requestLastFmAuthorization() {
        val settings = lastFmSettings.value
        if (settings.apiKey.isBlank() || settings.apiSecret.isBlank()) {
            setLastFmMessage("Informe a API key e o API secret antes de autorizar.")
            return
        }
        viewModelScope.launch {
            try {
                val token = LastFmClient(settings).requestToken()
                lastFmDataStore.saveAuthToken(token)
                _lastFmAuthUrl.value = LastFmClient.authorizationUrl(settings.apiKey, token)
                setLastFmMessage("Abra a página de autorização e depois conclua o login.")
            } catch (e: Exception) {
                setLastFmMessage("Não foi possível obter o token: ${e.message ?: "erro desconhecido"}")
            }
        }
    }

    fun completeLastFmAuthorization() {
        val settings = lastFmSettings.value
        if (settings.authToken.isBlank()) {
            setLastFmMessage("Solicite um token e autorize o aplicativo primeiro.")
            return
        }
        viewModelScope.launch {
            try {
                val session = LastFmClient(settings).getSession(settings.authToken)
                lastFmDataStore.saveSession(session.username, session.sessionKey)
                _lastFmAuthUrl.value = null
                setLastFmMessage("Last.fm conectado como ${session.username}.")
            } catch (e: Exception) {
                setLastFmMessage("Não foi possível concluir o login: ${e.message ?: "erro desconhecido"}")
            }
        }
    }

    fun saveLastFmCredentials(apiKey: String, apiSecret: String) {
        viewModelScope.launch {
            lastFmDataStore.saveCredentials(apiKey, apiSecret)
            setLastFmMessage("Credenciais salvas. Solicite um token para conectar.")
        }
    }

    fun setLastFmEnabled(enabled: Boolean) {
        viewModelScope.launch { lastFmDataStore.setEnabled(enabled) }
    }

    fun disconnectLastFm() {
        viewModelScope.launch {
            lastFmDataStore.clear()
            _lastFmAuthUrl.value = null
            setLastFmMessage("Last.fm desconectado.")
        }
    }

    fun exportBackup(context: Context, uri: Uri) {
        viewModelScope.launch {
            setScanStatusMessage("Criando backup...")
            try {
                backupManager.export(context, uri)
                setScanStatusMessage("Backup salvo com sucesso.")
            } catch (e: Exception) {
                setScanStatusMessage("Falha no backup: ${e.message ?: "arquivo inválido"}")
            }
            delay(3500)
            setScanStatusMessage(null)
        }
    }

    fun restoreBackup(context: Context, uri: Uri) {
        viewModelScope.launch {
            setScanStatusMessage("Restaurando backup...")
            try {
                val songs = backupManager.restore(context, uri)
                if (songs.isNotEmpty()) playerEngine.setQueue(songs, startIndex = 0, autoPlay = false)
                setScanStatusMessage("Backup restaurado: ${songs.size} música(s).")
            } catch (e: Exception) {
                setScanStatusMessage("Falha na restauração: ${e.message ?: "arquivo inválido"}")
            }
            delay(3500)
            setScanStatusMessage(null)
        }
    }

    private suspend fun persistPlaybackPosition() {
        val song = currentSong.value ?: return
        repository.updateLastPlayedState(song.id, currentPositionMs.value)
    }

    private fun schedulePersistPlaybackPosition() {
        val song = currentSong.value ?: return
        val position = currentPositionMs.value
        viewModelScope.launch {
            repository.updateLastPlayedState(song.id, position)
        }
    }

    private data class ScrobbleState(
        val song: Song?,
        val positionMs: Long,
        val durationMs: Long,
        val settings: LastFmSettings
    )

    override fun onCleared() {
        playerEngine.setOnSongChangedListener(null)
        playerEngine.setOnFavoriteToggleListener(null)
        settingsPersistJob?.cancel()
        persistJob?.cancel()
        super.onCleared()
    }

    class Factory(
        private val application: Application,
        private val repository: MusicRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MusicViewModel::class.java)) {
                return MusicViewModel(application, repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
