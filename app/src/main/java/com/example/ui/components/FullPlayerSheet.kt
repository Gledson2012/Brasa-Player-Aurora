package com.example.ui.components

import android.content.Intent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode as AnimRepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.RepeatMode
import com.example.data.lyrics.TrackLyrics
import com.example.data.model.AlbumArtStyle
import com.example.data.model.Song
import com.example.data.model.ThemeSettings
import com.example.data.model.VisualizerStyle
import kotlin.math.abs
import java.util.Locale

fun formatTimeMs(ms: Long): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
}

@Composable
fun FullPlayerSheet(
    song: Song?,
    isPlaying: Boolean,
    currentPositionMs: Long,
    durationMs: Long,
    queue: List<Song>,
    repeatMode: RepeatMode,
    isShuffle: Boolean,
    playbackSpeed: Float,
    visualizerAmplitudes: FloatArray,
    waveformSamples: List<Float> = emptyList(),
    themeSettings: ThemeSettings,
    sleepTimerRemainingSeconds: Int?,
    lyrics: TrackLyrics? = null,
    isLyricsLoading: Boolean = false,
    isLyricsViewActive: Boolean = false,
    onToggleLyricsView: () -> Unit = {},
    onRefreshLyrics: () -> Unit = {},
    onDismiss: () -> Unit,
    onPlayPauseClick: () -> Unit,
    onNextClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onSeek: (Long) -> Unit,
    onCycleRepeat: () -> Unit,
    onToggleShuffle: () -> Unit,
    onFavoriteClick: (Song) -> Unit,
    onOpenEqualizer: () -> Unit,
    onOpenSleepTimer: () -> Unit,
    onOpenSpeedDialog: () -> Unit,
    onOpenQueue: () -> Unit,
    onAddToPlaylist: (Song) -> Unit,
    onDeleteSong: (Song) -> Unit = {},
    onEditLyrics: () -> Unit = {}
) {
    if (song == null) return

    val context = LocalContext.current
    val isLiveRadio = song.sourceKey?.startsWith("radio:") == true
    var isUserSeeking by remember { mutableStateOf(false) }
    var seekSliderPosition by remember { mutableFloatStateOf(0f) }
    var showMenu by remember { mutableStateOf(false) }
    var showRemainingTime by remember(song.id) { mutableStateOf(false) }

    val rotationAnim = remember { Animatable(0f) }

    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            rotationAnim.animateTo(
                targetValue = rotationAnim.value + 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 16000, easing = LinearEasing),
                    repeatMode = AnimRepeatMode.Restart
                )
            )
        }
    }

    val progress = if (durationMs > 0) (currentPositionMs.toFloat() / durationMs).coerceIn(0f, 1f) else 0f
    val displaySliderValue = if (isUserSeeking) seekSliderPosition else progress

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .testTag("full_player_screen"),
        color = MaterialTheme.colorScheme.background
    ) {
        // Subtle gradient overlay for depth
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                                Color.Transparent,
                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.04f)
                            ),
                            startY = 0f,
                            endY = Float.POSITIVE_INFINITY
                        )
                    )
            )
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("full_player_collapse_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Fechar tela cheia",
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "TOCANDO AGORA",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = song.album,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Mais opções",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        if (!isLiveRadio) {
                            DropdownMenuItem(
                                text = { Text("Compartilhar") },
                                onClick = {
                                    showMenu = false
                                    val shareText = buildString {
                                        appendLine("🎵 ${song.title}")
                                        appendLine("🎤 ${song.artist}")
                                        appendLine("💿 ${song.album}")
                                        if (song.genre.isNotBlank() && song.genre != "Geral") {
                                            appendLine("🏷️ ${song.genre}")
                                        }
                                        appendLine()
                                        appendLine("Tocado no Brasa Player Aurora")
                                    }
                                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, shareText)
                                        putExtra(Intent.EXTRA_SUBJECT, "${song.title} - ${song.artist}")
                                    }
                                    context.startActivity(Intent.createChooser(sendIntent, "Compartilhar música"))
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Adicionar à Playlist") },
                                onClick = {
                                    showMenu = false
                                    onAddToPlaylist(song)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Excluir música") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    onDeleteSong(song)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Editar letras") },
                                onClick = {
                                    showMenu = false
                                    onEditLyrics()
                                }
                            )
                        }
                        DropdownMenuItem(
                            text = { Text("Equalizador de Áudio") },
                            onClick = {
                                showMenu = false
                                onOpenEqualizer()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Timer para Dormir") },
                            onClick = {
                                showMenu = false
                                onOpenSleepTimer()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Velocidade de Reprodução (${playbackSpeed}x)") },
                            onClick = {
                                showMenu = false
                                onOpenSpeedDialog()
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Player View Mode Tabs: Artwork/Visualizer vs Synced Lyrics
            TabRow(
                selectedTabIndex = if (isLyricsViewActive) 1 else 0,
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .clip(RoundedCornerShape(14.dp)),
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                contentColor = MaterialTheme.colorScheme.primary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[if (isLyricsViewActive) 1 else 0]),
                        color = MaterialTheme.colorScheme.primary,
                        height = 3.dp
                    )
                },
                divider = {}
            ) {
                Tab(
                    selected = !isLyricsViewActive,
                    onClick = { if (isLyricsViewActive) onToggleLyricsView() },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Album,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Arte & Áudio",
                                fontWeight = if (!isLyricsViewActive) FontWeight.Bold else FontWeight.Medium,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                )
                Tab(
                    selected = isLyricsViewActive,
                    onClick = { if (!isLyricsViewActive) onToggleLyricsView() },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Letras LRC",
                                fontWeight = if (isLyricsViewActive) FontWeight.Bold else FontWeight.Medium,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Center Content: Either Synced Lyrics OR Album Art + Visualizer
            if (isLyricsViewActive) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(340.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    SyncedLyricsView(
                        song = song,
                        lyrics = lyrics,
                        isLoading = isLyricsLoading,
                        currentPositionMs = currentPositionMs,
                        onSeekTo = onSeek,
                        onRefreshLyrics = onRefreshLyrics,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            } else {
                // Album Art Display Area
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .aspectRatio(1f)
                        .pointerInput(song.id) {
                            var totalX = 0f
                            var totalY = 0f
                            detectDragGestures(
                                onDragStart = { totalX = 0f; totalY = 0f },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    totalX += dragAmount.x
                                    totalY += dragAmount.y
                                },
                                onDragEnd = {
                                    when {
                                        abs(totalX) > 100f && abs(totalX) > abs(totalY) -> {
                                            if (totalX < 0f) onNextClick() else onPreviousClick()
                                        }
                                        abs(totalY) > 100f && abs(totalY) > abs(totalX) -> {
                                            val delta = if (totalY < 0f) durationMs / 10L else -durationMs / 10L
                                            onSeek((currentPositionMs + delta).coerceIn(0L, durationMs.coerceAtLeast(0L)))
                                        }
                                    }
                                }
                            )
                        }
                        .pointerInput(song.id) {
                            detectTapGestures(onDoubleTap = { context.hapticHeavyClick(); onPlayPauseClick() })
                        },
                    contentAlignment = Alignment.Center
                ) {
                    when (themeSettings.albumArtStyle) {
                        AlbumArtStyle.VINYL_ROTATION -> {
                            // Vinyl Record Effect
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .background(Color(0xFF111115))
                                    .border(8.dp, Color(0xFF1C1B22), CircleShape)
                                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), CircleShape)
                                    .rotate(if (isPlaying) rotationAnim.value else 0f),
                                contentAlignment = Alignment.Center
                            ) {
                                // Grooves effect
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize(0.85f)
                                        .border(1.dp, Color(0xFF262530), CircleShape)
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize(0.70f)
                                        .border(1.dp, Color(0xFF2E2D3A), CircleShape)
                                )
                                // Center Artwork Label
                                SongCoverArt(
                                    song = song,
                                    modifier = Modifier
                                        .fillMaxSize(0.48f)
                                        .clip(CircleShape)
                                        .border(3.dp, MaterialTheme.colorScheme.primary, CircleShape),
                                    cornerRadius = 999.dp
                                )
                                // Spindle hole
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.background)
                                )
                            }
                        }

                        AlbumArtStyle.CARD_ROUNDED -> {
                            SongCoverArt(
                                song = song,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .shadow(16.dp, RoundedCornerShape(24.dp))
                                    .border(
                                        2.dp,
                                        Brush.linearGradient(
                                            listOf(
                                                MaterialTheme.colorScheme.primary,
                                                MaterialTheme.colorScheme.secondary
                                            )
                                        ),
                                        RoundedCornerShape(24.dp)
                                    ),
                                cornerRadius = 24.dp
                            )
                        }

                        AlbumArtStyle.FULLSCREEN_GLOW -> {
                            Box(contentAlignment = Alignment.Center) {
                                // Glowing aura
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .scale(1.08f)
                                        .background(
                                            brush = Brush.radialGradient(
                                                colors = listOf(
                                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.45f),
                                                    Color.Transparent
                                                )
                                            ),
                                            shape = CircleShape
                                        )
                                )
                                SongCoverArt(
                                    song = song,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .shadow(20.dp, RoundedCornerShape(24.dp)),
                                    cornerRadius = 24.dp
                                )
                            }
                        }

                        AlbumArtStyle.POLAROID_FRAME -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .shadow(18.dp, RoundedCornerShape(12.dp))
                                    .background(
                                        MaterialTheme.colorScheme.surface,
                                        RoundedCornerShape(12.dp)
                                    )
                                    .padding(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                SongCoverArt(
                                    song = song,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f),
                                    cornerRadius = 5.dp
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(0.55f)
                                        .height(5.dp)
                                        .clip(RoundedCornerShape(50))
                                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.16f))
                                )
                            }
                        }

                        AlbumArtStyle.GLASSMORPHIC -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(4.dp)
                                    .shadow(20.dp, RoundedCornerShape(30.dp))
                                    .clip(RoundedCornerShape(30.dp))
                                    .background(
                                        Brush.linearGradient(
                                            listOf(
                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.32f),
                                                MaterialTheme.colorScheme.surface.copy(alpha = 0.72f),
                                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.24f)
                                            )
                                        )
                                    )
                                    .border(
                                        1.5.dp,
                                        Brush.linearGradient(
                                            listOf(
                                                Color.White.copy(alpha = 0.7f),
                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.45f),
                                                Color.White.copy(alpha = 0.16f)
                                            )
                                        ),
                                        RoundedCornerShape(30.dp)
                                    )
                                    .padding(14.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                SongCoverArt(
                                    song = song,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .shadow(12.dp, RoundedCornerShape(22.dp)),
                                    cornerRadius = 22.dp
                                )
                            }
                        }

                        AlbumArtStyle.NEON_RING -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .rotate(if (isPlaying) rotationAnim.value else 0f)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.radialGradient(
                                            listOf(
                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.32f),
                                                MaterialTheme.colorScheme.background.copy(alpha = 0.92f)
                                            )
                                        )
                                    )
                                    .border(
                                        8.dp,
                                        Brush.sweepGradient(
                                            listOf(
                                                MaterialTheme.colorScheme.primary,
                                                MaterialTheme.colorScheme.secondary,
                                                MaterialTheme.colorScheme.tertiary,
                                                MaterialTheme.colorScheme.primary
                                            )
                                        ),
                                        CircleShape
                                    )
                                    .padding(15.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                SongCoverArt(
                                    song = song,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                        .border(2.dp, Color.White.copy(alpha = 0.5f), CircleShape),
                                    cornerRadius = 999.dp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Visualizer Integrated View
                if (themeSettings.visualizerStyle == VisualizerStyle.WAVEFORM) {
                    AudioWaveformView(
                        samples = waveformSamples,
                        isPlaying = isPlaying,
                        height = 64.dp,
                        primaryColor = MaterialTheme.colorScheme.primary,
                        secondaryColor = MaterialTheme.colorScheme.secondary,
                        tertiaryColor = MaterialTheme.colorScheme.tertiary
                    )
                } else {
                    VisualizerView(
                        amplitudes = visualizerAmplitudes,
                        isPlaying = isPlaying,
                        style = themeSettings.visualizerStyle,
                        height = 50.dp,
                        barCount = 28,
                        primaryColor = MaterialTheme.colorScheme.primary,
                        secondaryColor = MaterialTheme.colorScheme.secondary,
                        tertiaryColor = MaterialTheme.colorScheme.tertiary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Track Title, Artist, & Favorite Heart
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = song.title,
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = song.artist,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(
                    onClick = { onFavoriteClick(song) },
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("full_player_favorite_button")
                ) {
                    Icon(
                        imageVector = if (song.isFavorite) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = if (song.isFavorite) "Desfavoritar" else "Favoritar",
                        tint = if (song.isFavorite) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                        modifier = Modifier
                            .size(28.dp)
                            .pulseOnChange(song.isFavorite)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Live streams do not have a finite duration or seek position.
            if (isLiveRadio) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.10f))
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Radio,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 10.dp)
                    ) {
                        Text(
                            text = "Transmissão ao vivo",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "O áudio está tocando em tempo real",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = "AO VIVO",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                // Seekbar Slider & Timestamps
                Column(modifier = Modifier.fillMaxWidth()) {
                    Slider(
                        value = displaySliderValue,
                        onValueChange = {
                            isUserSeeking = true
                            seekSliderPosition = it
                        },
                        onValueChangeFinished = {
                            val targetMs = (seekSliderPosition * durationMs).toLong()
                            onSeek(targetMs)
                            isUserSeeking = false
                        },
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary,
                            inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    val currentDisplayMs = if (isUserSeeking) (seekSliderPosition * durationMs).toLong() else currentPositionMs
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(
                            onClick = {
                                onSeek((currentDisplayMs - 10_000L).coerceAtLeast(0L))
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Replay10,
                                contentDescription = "Voltar 10 segundos",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Text(
                            text = formatTimeMs(currentDisplayMs),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                        )
                        Text(
                            text = if (showRemainingTime) {
                                "-${formatTimeMs((durationMs - currentDisplayMs).coerceAtLeast(0L))}"
                            } else {
                                formatTimeMs(durationMs)
                            },
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { showRemainingTime = !showRemainingTime }
                                .padding(horizontal = 6.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (showRemainingTime) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                        )
                        IconButton(
                            onClick = {
                                onSeek((currentDisplayMs + 10_000L).coerceAtMost(durationMs.coerceAtLeast(0L)))
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Forward10,
                                contentDescription = "Avançar 10 segundos",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Main Playback Controls Deck
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Shuffle Button
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            if (isShuffle) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f)
                            else Color.Transparent
                        )
                        .clickable { context.hapticTick(); onToggleShuffle() }
                        .testTag("player_shuffle_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shuffle,
                            contentDescription = if (isShuffle) "Modo aleatório ativado" else "Modo aleatório desativado",
                            tint = if (isShuffle) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                            modifier = Modifier.size(24.dp)
                        )
                        if (isShuffle) {
                            Box(
                                modifier = Modifier
                                    .padding(top = 2.dp)
                                    .size(4.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                        }
                    }
                }

                // Previous Button
                IconButton(
                    onClick = onPreviousClick,
                    modifier = Modifier
                        .size(52.dp)
                        .testTag("full_player_prev_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = "Faixa anterior",
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(36.dp)
                    )
                }

                // Large Play/Pause Primary FAB with gradient
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .shadow(12.dp, CircleShape)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.secondary
                                )
                            )
                        )
                        .clickable { context.hapticHeavyClick(); onPlayPauseClick() }
                        .testTag("full_player_play_pause_button"),
                    contentAlignment = Alignment.Center
                ) {
                    AnimatedPlayPauseIcon(
                        isPlaying = isPlaying,
                        contentDescription = if (isPlaying) "Pausar" else "Reproduzir",
                        tint = Color.White,
                        iconSize = 38.dp
                    )
                }

                // Next Button
                IconButton(
                    onClick = onNextClick,
                    modifier = Modifier
                        .size(52.dp)
                        .testTag("full_player_next_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Próxima faixa",
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(36.dp)
                    )
                }

                // Repeat Mode Button
                val isRepeatActive = repeatMode != RepeatMode.OFF
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            if (isRepeatActive) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f)
                            else Color.Transparent
                        )
                        .clickable { context.hapticTick(); onCycleRepeat() }
                        .testTag("player_repeat_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = when (repeatMode) {
                                RepeatMode.ONE -> Icons.Default.RepeatOne
                                else -> Icons.Default.Repeat
                            },
                            contentDescription = when (repeatMode) {
                                RepeatMode.OFF -> "Repetição desativada"
                                RepeatMode.ALL -> "Repetir todas as faixas"
                                RepeatMode.ONE -> "Repetir faixa atual"
                            },
                            tint = if (isRepeatActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                            modifier = Modifier.size(24.dp)
                        )
                        if (isRepeatActive) {
                            Box(
                                modifier = Modifier
                                    .padding(top = 2.dp)
                                    .size(4.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bottom Quick Actions Bar (Equalizer, Speed, Sleep Timer, Queue)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Lyrics Quick Button
                IconButton(
                    onClick = onToggleLyricsView,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Letras Sincronizadas",
                        tint = if (isLyricsViewActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Equalizer Quick Button
                IconButton(
                    onClick = onOpenEqualizer,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.GraphicEq,
                        contentDescription = "Abrir Equalizador",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Speed Selector Button
                IconButton(
                    onClick = onOpenSpeedDialog,
                    modifier = Modifier.size(40.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = "Velocidade",
                            tint = if (playbackSpeed != 1.0f) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "${playbackSpeed}x",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (playbackSpeed != 1.0f) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Sleep Timer Button
                IconButton(
                    onClick = onOpenSleepTimer,
                    modifier = Modifier.size(40.dp)
                ) {
                    if (sleepTimerRemainingSeconds != null && sleepTimerRemainingSeconds > 0) {
                        val mins = sleepTimerRemainingSeconds / 60
                        BadgedBox(
                            badge = {
                                Badge(containerColor = MaterialTheme.colorScheme.primary) {
                                    Text("${mins}m")
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = "Timer ativo",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    } else {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = "Configurar timer",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Queue Count Button
                IconButton(
                    onClick = {
                        onOpenQueue()
                    },
                    modifier = Modifier.size(40.dp)
                ) {
                    BadgedBox(
                        badge = {
                            Badge(containerColor = MaterialTheme.colorScheme.secondary) {
                                Text("${queue.size}")
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.QueueMusic,
                            contentDescription = "Fila de reprodução",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
