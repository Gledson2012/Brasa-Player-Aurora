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
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Radio
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.lyrics.LyricsManager
import com.example.data.model.Song
import com.example.di.ServiceLocator
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
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.PlaylistsScreen
import com.example.ui.screens.ThemesScreen
import com.example.ui.screens.TracksScreen
import com.example.ui.screens.RadiosScreen
import com.example.ui.theme.MusicPlayerTheme
import com.example.ui.viewmodel.MusicViewModel
import com.example.ui.viewmodel.MusicUiState

class MainActivity : ComponentActivity() {

    private val viewModel: MusicViewModel by viewModels {
        ServiceLocator.get(applicationContext).musicViewModelFactory(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MusicPlayerRoot(viewModel = viewModel)
        }
    }
}

@Composable
private fun MusicPlayerRoot(viewModel: MusicViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    MusicPlayerTheme(themeConfig = uiState.themeSettings) {
        MainAppContent(viewModel = viewModel, uiState = uiState)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppContent(viewModel: MusicViewModel, uiState: MusicUiState) {
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
    val selectedTab = uiState.selectedTab
    val isFullPlayerOpen = uiState.isFullPlayerOpen
    val currentSong = uiState.currentSong
    val isPlaying = uiState.isPlaying
    val currentPositionMs = uiState.currentPositionMs
    val durationMs = uiState.durationMs
    val queue = uiState.queue
    val repeatMode = uiState.repeatMode
    val isShuffle = uiState.isShuffle
    val playbackSpeed = uiState.playbackSpeed
    val crossfadeSeconds = uiState.crossfadeSeconds
    val visualizerAmplitudes = uiState.visualizerAmplitudes
    val waveformSamples = uiState.waveformSamples
    val onboardingCompleted = uiState.onboardingCompleted
    val isLoading = uiState.isLoading
    val isRefreshing = uiState.isRefreshing
    val allSongs = uiState.allSongs
    val displayedSongs = uiState.displayedSongs
    val favoriteSongs = uiState.favoriteSongs
    val recentlyPlayed = uiState.recentlyPlayed
    val mostPlayed = uiState.mostPlayed
    val allPlaylistsWithSongs = uiState.allPlaylistsWithSongs
    val activePlaylistDetail = uiState.activePlaylistDetail
    val searchQuery = uiState.searchQuery
    val sortOption = uiState.sortOption
    val equalizerState = uiState.equalizerState
    val themeSettings = uiState.themeSettings
    val sleepTimerRemainingSeconds = uiState.sleepTimerRemainingSeconds
    val sleepTimerEndAtTrackEnd = uiState.sleepTimerEndAtTrackEnd
    val scanStatusMessage = uiState.scanStatusMessage
    val lastFmSettings = uiState.lastFmSettings
    val lastFmMessage = uiState.lastFmMessage
    val lastFmAuthUrl = uiState.lastFmAuthUrl
    val playbackError = uiState.playbackError
    val snackbarHostState = remember { SnackbarHostState() }
    val openRadioLink: (String) -> Unit = { url ->
        runCatching {
            context.startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
        }
    }
    val playRadioStation: (String, String, String, String?, String?) -> Unit = { title, category, coverUri, radioId, streamUrl ->
        viewModel.playRadio(
            title = title,
            category = category,
            coverUri = coverUri,
            radioId = radioId,
            streamUrl = streamUrl
        )
    }

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

    val currentLyrics = uiState.currentLyrics
    val isLyricsLoading = uiState.isLyricsLoading
    val isLyricsViewActive = uiState.isLyricsViewActive
    val showCreatePlaylistDialog = uiState.showCreatePlaylistDialog
    val showAddToPlaylistDialog = uiState.showAddToPlaylistDialog
    val showSleepTimerDialog = uiState.showSleepTimerDialog
    val showSpeedDialog = uiState.showSpeedDialog
    val showLastFmDialog = uiState.showLastFmDialog
    val showLyricsEditor = uiState.showLyricsEditor

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
    val audioFolderLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        runCatching {
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        }
        viewModel.importAudioFolder(context, uri)
    }

    AnimatedContent(
        targetState = onboardingCompleted,
        transitionSpec = {
            if (targetState) {
                fadeIn(animationSpec = tween(500)) togetherWith fadeOut(animationSpec = tween(300))
            } else {
                fadeIn(animationSpec = tween(500)) togetherWith fadeOut(animationSpec = tween(300))
            }
        },
        label = "onboarding_transition"
    ) { completed ->
    if (!completed) {
        OnboardingScreen(
            onComplete = { viewModel.completeOnboarding() }
        )
    } else {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.06f),
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.background
                    )
                )
            )
    ) {
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

                    // Navigation Bar
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
                        tonalElevation = 8.dp,
                        modifier = Modifier
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f),
                                shape = RoundedCornerShape(24.dp)
                            )
                            .testTag("main_bottom_nav")
                    ) {
                        NavigationBarItem(
                            selected = selectedTab == 0,
                            onClick = { viewModel.selectTab(0) },
                            icon = { Icon(imageVector = Icons.Default.Home, contentDescription = "Início") },
                            label = { Text("Início", fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal) },
                            alwaysShowLabel = false,
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )

                        NavigationBarItem(
                            selected = selectedTab == 1,
                            onClick = { viewModel.selectTab(1) },
                            icon = { Icon(imageVector = Icons.Default.MusicNote, contentDescription = "Músicas") },
                            label = { Text("Músicas", fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal) },
                            alwaysShowLabel = false,
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )

                        NavigationBarItem(
                            selected = selectedTab == 2,
                            onClick = { viewModel.selectTab(2) },
                            icon = { Icon(imageVector = Icons.AutoMirrored.Filled.QueueMusic, contentDescription = "Playlists") },
                            label = { Text("Playlists", fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal) },
                            alwaysShowLabel = false,
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )

                        NavigationBarItem(
                            selected = selectedTab == 3,
                            onClick = { viewModel.selectTab(3) },
                            icon = { Icon(imageVector = Icons.Default.GraphicEq, contentDescription = "Equalizador") },
                            label = { Text("Equalizador", fontWeight = if (selectedTab == 3) FontWeight.Bold else FontWeight.Normal) },
                            alwaysShowLabel = false,
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )

                        NavigationBarItem(
                            selected = selectedTab == 4,
                            onClick = { viewModel.selectTab(4) },
                            icon = { Icon(imageVector = Icons.Default.Palette, contentDescription = "Temas") },
                            label = { Text("Temas", fontWeight = if (selectedTab == 4) FontWeight.Bold else FontWeight.Normal) },
                            alwaysShowLabel = false,
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )

                        NavigationBarItem(
                            selected = selectedTab == 5,
                            onClick = { viewModel.selectTab(5) },
                            icon = { Icon(imageVector = Icons.Default.Radio, contentDescription = "Rádio") },
                            label = { Text("Rádio", fontWeight = if (selectedTab == 5) FontWeight.Bold else FontWeight.Normal) },
                            alwaysShowLabel = false,
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
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
                    0 -> HomeScreen(
                        currentSong = currentSong,
                        isPlaying = isPlaying,
                        allSongs = allSongs,
                        recentlyPlayed = recentlyPlayed,
                        favoriteSongs = favoriteSongs,
                        mostPlayed = mostPlayed,
                        playlists = allPlaylistsWithSongs,
                        onPlayPause = { viewModel.togglePlayPause() },
                        onPlaySong = { songs, index -> viewModel.playSongFromList(songs, index) },
                        onOpenTracks = { viewModel.selectTab(1) },
                        onOpenPlaylists = { viewModel.selectTab(2) }
                    )

                    1 -> TracksScreen(
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

                    2 -> PlaylistsScreen(
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

                    3 -> EqualizerScreen(
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

                    4 -> ThemesScreen(
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
                        onImportAudioFolder = { context -> audioFolderLauncher.launch(null) },
                        onOpenLastFm = { viewModel.showLastFmDialog() },
                        onBackup = { backupLauncher.launch("music-player-backup.json") },
                        onRestore = { showRestoreConfirmation = true }
                    )

                    5 -> RadiosScreen(
                        onOpenLink = openRadioLink,
                        onPlayStation = playRadioStation
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
                    viewModel.selectTab(3)
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
                pendingScrobbleCount = uiState.pendingScrobbleCount,
                onDismiss = { viewModel.dismissLastFmDialog() },
                onSaveCredentials = viewModel::saveLastFmCredentials,
                onRequestAuthorization = viewModel::requestLastFmAuthorization,
                onOpenAuthorization = { url ->
                    context.startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
                },
                onCompleteAuthorization = viewModel::completeLastFmAuthorization,
                onToggleEnabled = viewModel::setLastFmEnabled,
                onDisconnect = viewModel::disconnectLastFm,
                onClearPendingScrobbles = viewModel::clearPendingScrobbles,
                onProcessPendingScrobbles = viewModel::processPendingScrobbles
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
    } // end AnimatedContent
}
