package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material3.Icon
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.db.AppDatabase
import com.example.data.lyrics.LyricsManager
import com.example.data.model.Song
import com.example.data.repository.MusicRepository
import com.example.ui.components.AddToPlaylistDialog
import com.example.ui.components.CreatePlaylistDialog
import com.example.ui.components.EditSongDialog
import com.example.ui.components.FullPlayerSheet
import com.example.ui.components.LastFmSettingsDialog
import com.example.ui.components.LyricsEditorDialog
import com.example.ui.components.MiniPlayerBar
import com.example.ui.components.QueueSheet
import com.example.ui.components.SleepTimerDialog
import com.example.ui.components.SpeedDialog
import com.example.ui.screens.EqualizerScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.PlaylistsScreen
import com.example.ui.screens.ThemesScreen
import com.example.ui.screens.TracksScreen
import com.example.ui.theme.MusicPlayerTheme
import com.example.ui.viewmodel.MusicViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MusicViewModel by viewModels {
        val database = AppDatabase.getInstance(applicationContext)
        val repository = MusicRepository(
            database = database,
            songDao = database.songDao(),
            playlistDao = database.playlistDao(),
            userSettingsDao = database.userSettingsDao(),
            lyricsDao = database.lyricsDao()
        )
        MusicViewModel.Factory(application, repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val themeSettings by viewModel.themeSettings.collectAsStateWithLifecycle()

            MusicPlayerTheme(themeConfig = themeSettings) {
                MainAppContent(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppContent(viewModel: MusicViewModel) {
    val context = LocalContext.current
    var showRestoreConfirmation by remember { mutableStateOf(false) }
    var editingSong by remember { mutableStateOf<Song?>(null) }
    var songToDelete by remember { mutableStateOf<Song?>(null) }
    var songToRelink by remember { mutableStateOf<Song?>(null) }
    var showQueueSheet by remember { mutableStateOf(false) }
    var notificationPermissionRequested by remember { mutableStateOf(false) }
    val audioPermission = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
    }
    val requestAudioPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            viewModel.scanLocalStorage(context)
        } else {
            viewModel.notifyScanStatusMessage("Permissão de áudio necessária para escanear músicas.")
        }
    }
    val requestNotificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val isFullPlayerOpen by viewModel.isFullPlayerOpen.collectAsStateWithLifecycle()

    val currentSong by viewModel.currentSong.collectAsStateWithLifecycle()
    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()
    val currentPositionMs by viewModel.currentPositionMs.collectAsStateWithLifecycle()
    val durationMs by viewModel.durationMs.collectAsStateWithLifecycle()
    val queue by viewModel.queue.collectAsStateWithLifecycle()
    val repeatMode by viewModel.repeatMode.collectAsStateWithLifecycle()
    val isShuffle by viewModel.isShuffle.collectAsStateWithLifecycle()
    val playbackSpeed by viewModel.playbackSpeed.collectAsStateWithLifecycle()
    val crossfadeSeconds by viewModel.crossfadeSeconds.collectAsStateWithLifecycle()
    val visualizerAmplitudes by viewModel.visualizerAmplitudes.collectAsStateWithLifecycle()
    val waveformSamples by viewModel.waveformSamples.collectAsStateWithLifecycle()

    val onboardingCompleted by viewModel.onboardingCompleted.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val allSongs by viewModel.allSongs.collectAsStateWithLifecycle()
    val displayedSongs by viewModel.displayedSongs.collectAsStateWithLifecycle()
    val favoriteSongs by viewModel.favoriteSongs.collectAsStateWithLifecycle()
    val recentlyPlayed by viewModel.recentlyPlayed.collectAsStateWithLifecycle()
    val mostPlayed by viewModel.mostPlayed.collectAsStateWithLifecycle()
    val allPlaylistsWithSongs by viewModel.allPlaylistsWithSongs.collectAsStateWithLifecycle()
    val activePlaylistDetail by viewModel.activePlaylistDetail.collectAsStateWithLifecycle()

    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val sortOption by viewModel.sortOption.collectAsStateWithLifecycle()
    val equalizerState by viewModel.equalizerState.collectAsStateWithLifecycle()
    val themeSettings by viewModel.themeSettings.collectAsStateWithLifecycle()
    val sleepTimerRemainingSeconds by viewModel.sleepTimerRemainingSeconds.collectAsStateWithLifecycle()
    val sleepTimerEndAtTrackEnd by viewModel.sleepTimerEndAtTrackEnd.collectAsStateWithLifecycle()
    val scanStatusMessage by viewModel.scanStatusMessage.collectAsStateWithLifecycle()
    val lastFmSettings by viewModel.lastFmSettings.collectAsStateWithLifecycle()
    val lastFmMessage by viewModel.lastFmMessage.collectAsStateWithLifecycle()
    val lastFmAuthUrl by viewModel.lastFmAuthUrl.collectAsStateWithLifecycle()
    val playbackError by viewModel.playbackError.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(currentSong?.id, isPlaying) {
        if (
            currentSong != null &&
            isPlaying &&
            !notificationPermissionRequested &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionRequested = true
            requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    LaunchedEffect(playbackError) {
        playbackError?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearPlaybackError()
        }
    }

    LaunchedEffect(scanStatusMessage) {
        scanStatusMessage?.let { snackbarHostState.showSnackbar(it) }
    }

    val currentLyrics by viewModel.currentLyrics.collectAsStateWithLifecycle()
    val isLyricsLoading by viewModel.isLyricsLoading.collectAsStateWithLifecycle()
    val isLyricsViewActive by viewModel.isLyricsViewActive.collectAsStateWithLifecycle()

    val showCreatePlaylistDialog by viewModel.showCreatePlaylistDialog.collectAsStateWithLifecycle()
    val showAddToPlaylistDialog by viewModel.showAddToPlaylistDialog.collectAsStateWithLifecycle()
    val showSleepTimerDialog by viewModel.showSleepTimerDialog.collectAsStateWithLifecycle()
    val showSpeedDialog by viewModel.showSpeedDialog.collectAsStateWithLifecycle()
    val showLastFmDialog by viewModel.showLastFmDialog.collectAsStateWithLifecycle()
    val showLyricsEditor by viewModel.showLyricsEditor.collectAsStateWithLifecycle()

    BackHandler(enabled = isFullPlayerOpen) {
        viewModel.closeFullPlayer()
    }

    val backupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri -> uri?.let { viewModel.exportBackup(context, it) } }
    val restoreLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri -> uri?.let { viewModel.restoreBackup(context, it) } }
    val relinkLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        runCatching {
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        }
        songToRelink?.let { viewModel.relinkSong(context, it, uri) }
        songToRelink = null
    }
    val audioFilesLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        uris.forEach { uri ->
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
        }
        viewModel.importAudioFiles(context, uris)
    }

    if (!onboardingCompleted) {
        OnboardingScreen(
            onComplete = { viewModel.completeOnboarding() }
        )
    } else {
    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            bottomBar = {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Mini Player Floating Bar
                    if (currentSong != null && !isFullPlayerOpen) {
                        MiniPlayerBar(
                            song = currentSong,
                            isPlaying = isPlaying,
                            currentPositionMs = currentPositionMs,
                            durationMs = durationMs,
                            visualizerAmplitudes = visualizerAmplitudes,
                            onPlayPauseClick = { viewModel.togglePlayPause() },
                            onNextClick = { viewModel.playNext() },
                            onFavoriteClick = { viewModel.toggleFavorite(it) },
                            onBarClick = { viewModel.openFullPlayer() }
                        )
                    }

                    // Navigation Bar (4 Main Sections)
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 8.dp,
                        modifier = Modifier.testTag("main_bottom_nav")
                    ) {
                        NavigationBarItem(
                            selected = selectedTab == 0,
                            onClick = { viewModel.selectTab(0) },
                            icon = { Icon(imageVector = Icons.Default.MusicNote, contentDescription = "Músicas") },
                            label = { Text("Músicas", fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )

                        NavigationBarItem(
                            selected = selectedTab == 1,
                            onClick = { viewModel.selectTab(1) },
                            icon = { Icon(imageVector = Icons.AutoMirrored.Filled.QueueMusic, contentDescription = "Playlists") },
                            label = { Text("Playlists", fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )

                        NavigationBarItem(
                            selected = selectedTab == 2,
                            onClick = { viewModel.selectTab(2) },
                            icon = { Icon(imageVector = Icons.Default.GraphicEq, contentDescription = "Equalizador") },
                            label = { Text("Equalizador", fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )

                        NavigationBarItem(
                            selected = selectedTab == 3,
                            onClick = { viewModel.selectTab(3) },
                            icon = { Icon(imageVector = Icons.Default.Palette, contentDescription = "Temas") },
                            label = { Text("Temas", fontWeight = if (selectedTab == 3) FontWeight.Bold else FontWeight.Normal) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .statusBarsPadding()
            ) {
                AnimatedContent(
                targetState = selectedTab,
                transitionSpec = { fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300)) },
                label = "tab_transition"
            ) { tab ->
                when (tab) {
                    0 -> TracksScreen(
                        songs = displayedSongs,
                        isLoading = isLoading,
                        isRefreshing = isRefreshing,
                        onRefresh = { viewModel.refreshLibrary() },
                        currentPlayingSong = currentSong,
                        isPlaying = isPlaying,
                        searchQuery = searchQuery,
                        currentSort = sortOption,
                        onSearchChange = { viewModel.setSearchQuery(it) },
                        onSortChange = { viewModel.setSortOption(it) },
                        onOpenFiles = { audioFilesLauncher.launch(arrayOf("audio/*")) },
                        onSongClick = { songs, index -> viewModel.playSongFromList(songs, index) },
                        onPlayAll = { songs ->
                            if (songs.isNotEmpty()) {
                                viewModel.playSongFromList(songs, 0)
                            }
                        },
                        onShuffleAll = { songs ->
                            if (songs.isNotEmpty()) {
                                val shuffled = songs.shuffled()
                                viewModel.playSongFromList(shuffled, 0)
                            }
                        },
                        onToggleFavorite = { viewModel.toggleFavorite(it) },
                        onAddToPlaylist = { viewModel.showAddToPlaylistDialog(it) },
                        onEditSong = { editingSong = it },
                        onDeleteSong = { songToDelete = it },
                        onRelinkSong = {
                            songToRelink = it
                            relinkLauncher.launch(arrayOf("audio/*"))
                        }
                    )

                    1 -> PlaylistsScreen(
                        playlistsWithSongs = allPlaylistsWithSongs,
                        isLoading = isLoading,
                        isRefreshing = isRefreshing,
                        onRefresh = { viewModel.refreshLibrary() },
                        allSongs = allSongs,
                        favoriteSongs = favoriteSongs,
                        recentlyPlayed = recentlyPlayed,
                        mostPlayed = mostPlayed,
                        currentPlayingSong = currentSong,
                        isPlaying = isPlaying,
                        activePlaylistDetail = activePlaylistDetail,
                        onOpenPlaylistDetail = { viewModel.openPlaylistDetail(it) },
                        onClosePlaylistDetail = { viewModel.closePlaylistDetail() },
                        onCreatePlaylistClick = { viewModel.showCreatePlaylistDialog() },
                        onDeletePlaylist = { viewModel.deletePlaylist(it) },
                        onPlaySongFromList = { songs, index -> viewModel.playSongFromList(songs, index) },
                        onToggleFavorite = { viewModel.toggleFavorite(it) },
                        onAddSongToPlaylist = { pId, sId -> viewModel.addSongToPlaylist(pId, sId) },
                        onRemoveSongFromPlaylist = { pId, sId -> viewModel.removeSongFromPlaylist(pId, sId) }
                    )

                    2 -> EqualizerScreen(
                        equalizerState = equalizerState,
                        isLoading = isLoading,
                        isPlaying = isPlaying,
                        visualizerAmplitudes = visualizerAmplitudes,
                        onToggleEnabled = { viewModel.setEqualizerEnabled(it) },
                        onSelectPreset = { viewModel.selectEqualizerPreset(it) },
                        onBandGainChange = { band, gain -> viewModel.setEqualizerBandGain(band, gain) },
                        onBassBoostChange = { viewModel.setBassBoost(it) },
                        onVirtualizerChange = { viewModel.setVirtualizer(it) },
                        onBalanceChange = { viewModel.setBalance(it) },
                        onReset = { viewModel.resetEqualizer() },
                        onSaveCustomPreset = { viewModel.saveCurrentAsCustomPreset(it) },
                        onDeleteCustomPreset = { viewModel.deleteCustomPreset(it) }
                    )

                    3 -> ThemesScreen(
                        themeConfig = themeSettings,
                        crossfadeSeconds = crossfadeSeconds,
                        scanStatusMessage = scanStatusMessage,
                        onSelectThemeMode = { viewModel.setThemeMode(it) },
                        onSelectPresetTheme = { viewModel.setPresetTheme(it) },
                        onSaveCustomTheme = { primary, secondary, tertiary, surface, background, isDark ->
                            viewModel.setCustomTheme(primary, secondary, tertiary, surface, background, isDark)
                        },
                        onToggleDynamicColors = { viewModel.setDynamicColors(it) },
                        onSelectVisualizerStyle = { viewModel.setVisualizerStyle(it) },
                        onSelectAlbumArtStyle = { viewModel.setAlbumArtStyle(it) },
                        onSetCrossfadeSeconds = { viewModel.setCrossfadeSeconds(it) },
                        onResetDefaults = { viewModel.resetThemeSettings() },
                        onScanLocalStorage = { scanContext ->
                            if (ContextCompat.checkSelfPermission(scanContext, audioPermission) == PackageManager.PERMISSION_GRANTED) {
                                viewModel.scanLocalStorage(scanContext)
                            } else {
                                requestAudioPermissionLauncher.launch(audioPermission)
                            }
                        },
                        onImportAudioFile = { context, uri, name -> viewModel.importSingleAudio(context, uri, name) },
                        onOpenLastFm = { viewModel.showLastFmDialog() },
                        onBackup = { backupLauncher.launch("music-player-backup.json") },
                        onRestore = { showRestoreConfirmation = true }
                    )
                }
                } // AnimatedContent
            }
        }

        // Full Player Modal Sheet
        AnimatedVisibility(
            visible = isFullPlayerOpen && currentSong != null,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            FullPlayerSheet(
                song = currentSong,
                isPlaying = isPlaying,
                currentPositionMs = currentPositionMs,
                durationMs = durationMs,
                queue = queue,
                repeatMode = repeatMode,
                isShuffle = isShuffle,
                playbackSpeed = playbackSpeed,
                visualizerAmplitudes = visualizerAmplitudes,
                waveformSamples = waveformSamples,
                themeSettings = themeSettings,
                sleepTimerRemainingSeconds = sleepTimerRemainingSeconds,
                lyrics = currentLyrics,
                isLyricsLoading = isLyricsLoading,
                isLyricsViewActive = isLyricsViewActive,
                onToggleLyricsView = { viewModel.toggleLyricsView() },
                onRefreshLyrics = { viewModel.refreshLyrics() },
                onDismiss = { viewModel.closeFullPlayer() },
                onPlayPauseClick = { viewModel.togglePlayPause() },
                onNextClick = { viewModel.playNext() },
                onPreviousClick = { viewModel.playPrevious() },
                onSeek = { viewModel.seekTo(it) },
                onCycleRepeat = { viewModel.cycleRepeatMode() },
                onToggleShuffle = { viewModel.toggleShuffle() },
                onFavoriteClick = { viewModel.toggleFavorite(it) },
                onOpenEqualizer = {
                    viewModel.closeFullPlayer()
                    viewModel.selectTab(2)
                },
                onOpenSleepTimer = { viewModel.showSleepTimerDialog() },
                onOpenSpeedDialog = { viewModel.showSpeedDialog() },
                onOpenQueue = { showQueueSheet = true },
                onAddToPlaylist = { viewModel.showAddToPlaylistDialog(it) },
                onDeleteSong = { songToDelete = it },
                onEditLyrics = { viewModel.showLyricsEditor() }
            )
        }

        if (showQueueSheet) {
            QueueSheet(
                queue = queue,
                currentSongId = currentSong?.id,
                onDismiss = { showQueueSheet = false },
                onPlaySong = { selectedSong ->
                    val index = queue.indexOfFirst { it.id == selectedSong.id }
                    if (index >= 0) {
                        viewModel.playSongFromList(queue, index)
                    }
                    showQueueSheet = false
                }
            )
        }

        // Dialogs
        if (showCreatePlaylistDialog) {
            CreatePlaylistDialog(
                onDismiss = { viewModel.dismissCreatePlaylistDialog() },
                onConfirm = { name, desc, gradientIndex ->
                    viewModel.createPlaylist(name, desc, gradientIndex = gradientIndex)
                    viewModel.dismissCreatePlaylistDialog()
                }
            )
        }

        showAddToPlaylistDialog?.let { songToAddToPlaylist ->
            AddToPlaylistDialog(
                song = songToAddToPlaylist,
                playlists = allPlaylistsWithSongs,
                onDismiss = { viewModel.dismissAddToPlaylistDialog() },
                onSelectPlaylist = { playlistId ->
                    viewModel.addSongToPlaylist(playlistId, songToAddToPlaylist.id)
                    viewModel.dismissAddToPlaylistDialog()
                },
                onCreateNewPlaylist = {
                    viewModel.dismissAddToPlaylistDialog()
                    viewModel.showCreatePlaylistDialog()
                }
            )
        }

        if (showSleepTimerDialog) {
            SleepTimerDialog(
                currentRemainingSeconds = sleepTimerRemainingSeconds,
                isEndAtTrackEnd = sleepTimerEndAtTrackEnd,
                onDismiss = { viewModel.dismissSleepTimerDialog() },
                onSetTimer = { mins -> viewModel.setSleepTimer(mins) },
                onSetTimerUntilTrackEnd = { viewModel.setSleepTimerUntilTrackEnd() },
                onCancelTimer = { viewModel.cancelSleepTimer() }
            )
        }

        if (showSpeedDialog) {
            SpeedDialog(
                currentSpeed = playbackSpeed,
                onDismiss = { viewModel.dismissSpeedDialog() },
                onSelectSpeed = { speed -> viewModel.setPlaybackSpeed(speed) }
            )
        }

        if (showLyricsEditor && currentSong != null) {
            LyricsEditorDialog(
                song = currentSong!!,
                initialLrc = currentLyrics?.let(LyricsManager::toLrc).orEmpty(),
                onDismiss = { viewModel.dismissLyricsEditor() },
                onSave = { viewModel.saveLyrics(it) }
            )
        }

        editingSong?.let { song ->
            EditSongDialog(
                song = song,
                onDismiss = { editingSong = null },
                onSave = { title, artist, album, genre, coverUri ->
                    viewModel.updateSongMetadata(song, title, artist, album, genre, coverUri)
                    editingSong = null
                }
            )
        }

        songToDelete?.let { song ->
            AlertDialog(
                onDismissRequest = { songToDelete = null },
                modifier = Modifier.testTag("delete_song_dialog"),
                title = { Text("Excluir música?") },
                text = {
                    Text(
                        "A música \"${song.title}\" será removida da biblioteca e das playlists. O arquivo original não será apagado."
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.deleteSong(song)
                            songToDelete = null
                        }
                    ) {
                        Text("Excluir", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { songToDelete = null }) {
                        Text("Cancelar")
                    }
                }
            )
        }

        if (showLastFmDialog) {
            LastFmSettingsDialog(
                settings = lastFmSettings,
                message = lastFmMessage,
                authUrl = lastFmAuthUrl,
                onDismiss = { viewModel.dismissLastFmDialog() },
                onSaveCredentials = viewModel::saveLastFmCredentials,
                onRequestAuthorization = viewModel::requestLastFmAuthorization,
                onOpenAuthorization = { url ->
                    context.startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
                },
                onCompleteAuthorization = viewModel::completeLastFmAuthorization,
                onToggleEnabled = viewModel::setLastFmEnabled,
                onDisconnect = viewModel::disconnectLastFm
            )
        }

        if (showRestoreConfirmation) {
            AlertDialog(
                onDismissRequest = { showRestoreConfirmation = false },
                title = { Text("Restaurar backup?") },
                text = { Text("A restauração substitui músicas, playlists, letras e preferências atuais.") },
                confirmButton = {
                    TextButton(onClick = {
                        showRestoreConfirmation = false
                        restoreLauncher.launch(arrayOf("application/json", "text/plain"))
                    }) { Text("Continuar") }
                },
                dismissButton = {
                    TextButton(onClick = { showRestoreConfirmation = false }) { Text("Cancelar") }
                }
            )
        }
    }
    } // end else (onboarding)
}
