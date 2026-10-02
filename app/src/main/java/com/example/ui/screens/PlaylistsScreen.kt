package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale
import com.example.data.model.Playlist
import com.example.data.model.PlaylistWithSongs
import com.example.data.model.Song
import com.example.ui.components.AddSongsToPlaylistPicker
import com.example.ui.components.DeletePlaylistConfirmDialog
import com.example.ui.components.PLAYLIST_GRADIENTS
import com.example.ui.components.PlaylistItemSkeleton
import com.example.ui.components.SongCoverArt
import com.example.ui.components.hapticTick

private enum class PlaylistSortOrder(val title: String) {
    RECENT("Mais recentes"),
    NAME("Nome (A-Z)"),
    SONGS_COUNT("Mais faixas")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistsScreen(
    playlistsWithSongs: List<PlaylistWithSongs>,
    isLoading: Boolean = false,
    isRefreshing: Boolean = false,
    onRefresh: () -> Unit = {},
    allSongs: List<Song> = emptyList(),
    favoriteSongs: List<Song>,
    recentlyPlayed: List<Song>,
    mostPlayed: List<Song>,
    currentPlayingSong: Song?,
    isPlaying: Boolean,
    activePlaylistDetail: PlaylistWithSongs?,
    onOpenPlaylistDetail: (PlaylistWithSongs) -> Unit,
    onClosePlaylistDetail: () -> Unit,
    onCreatePlaylistClick: () -> Unit,
    onDeletePlaylist: (Playlist) -> Unit,
    onPlaySongFromList: (songs: List<Song>, startIndex: Int) -> Unit,
    onToggleFavorite: (Song) -> Unit,
    onAddSongToPlaylist: (playlistId: Long, songId: Long) -> Unit = { _, _ -> },
    onRemoveSongFromPlaylist: (playlistId: Long, songId: Long) -> Unit
) {
    var playlistToDelete by remember { mutableStateOf<Playlist?>(null) }
    var playlistQuery by remember { mutableStateOf("") }
    var sortOrder by remember { mutableStateOf(PlaylistSortOrder.RECENT) }
    var showSortMenu by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val visiblePlaylists by remember(playlistsWithSongs, playlistQuery, sortOrder) {
        derivedStateOf {
            val filtered = playlistsWithSongs.filter { item ->
                playlistQuery.isBlank() || item.playlist.name.contains(playlistQuery, ignoreCase = true) ||
                    item.playlist.description.contains(playlistQuery, ignoreCase = true)
            }
            when (sortOrder) {
                PlaylistSortOrder.RECENT -> filtered.sortedByDescending { it.playlist.createdAt }
                PlaylistSortOrder.NAME -> filtered.sortedBy { it.playlist.name.lowercase(Locale.ROOT) }
                PlaylistSortOrder.SONGS_COUNT -> filtered.sortedByDescending { it.songs.size }
            }
        }
    }

    // Delete Confirmation Dialog
    playlistToDelete?.let { playlist ->
        DeletePlaylistConfirmDialog(
            playlist = playlist,
            onDismiss = { playlistToDelete = null },
            onConfirm = {
                onDeletePlaylist(playlist)
                playlistToDelete = null
            }
        )
    }

    if (activePlaylistDetail != null) {
        // Detail View for Selected Playlist (synced with latest database emission)
        val currentDetail = if (activePlaylistDetail.playlist.id > 0) {
            playlistsWithSongs.find { it.playlist.id == activePlaylistDetail.playlist.id } ?: activePlaylistDetail
        } else {
            // Smart playlist (Favorites, Recent, Most Played)
            when (activePlaylistDetail.playlist.name) {
                "Músicas Favoritas" -> activePlaylistDetail.copy(songs = favoriteSongs)
                "Tocadas Recentemente" -> activePlaylistDetail.copy(songs = recentlyPlayed)
                "Mais Tocadas" -> activePlaylistDetail.copy(songs = mostPlayed)
                else -> activePlaylistDetail
            }
        }

        PlaylistDetailView(
            playlistWithSongs = currentDetail,
            allSongs = allSongs,
            currentPlayingSong = currentPlayingSong,
            isPlaying = isPlaying,
            onBack = onClosePlaylistDetail,
            onPlaySong = { songs, index -> onPlaySongFromList(songs, index) },
            onToggleFavorite = onToggleFavorite,
            onAddSong = { songId -> onAddSongToPlaylist(currentDetail.playlist.id, songId) },
            onRemoveSong = { songId -> onRemoveSongFromPlaylist(currentDetail.playlist.id, songId) },
            onDeletePlaylist = { playlistToDelete = currentDetail.playlist }
        )
    } else {
        // Main Playlists Overview
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                                MaterialTheme.colorScheme.background,
                                MaterialTheme.colorScheme.background
                            )
                        )
                    )
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                        .testTag("playlists_screen"),
                    contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // ───────────────────────────────────────────────
                    // 1. Sleek Compact Header Bar
                    // ───────────────────────────────────────────────
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(
                                            Brush.linearGradient(
                                                listOf(
                                                    MaterialTheme.colorScheme.primary,
                                                    MaterialTheme.colorScheme.secondary
                                                )
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.QueueMusic,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                Column {
                                    Text(
                                        text = "Playlists",
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${playlistsWithSongs.size} ${if (playlistsWithSongs.size == 1) "coleção criada" else "coleções criadas"}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            // Create playlist CTA button
                            Button(
                                onClick = { context.hapticTick(); onCreatePlaylistClick() },
                                shape = RoundedCornerShape(14.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                modifier = Modifier.testTag("create_playlist_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Nova Playlist",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // ───────────────────────────────────────────────
                    // 2. Search Playlists Bar
                    // ───────────────────────────────────────────────
                    item {
                        OutlinedTextField(
                            value = playlistQuery,
                            onValueChange = { playlistQuery = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("search_playlists_input"),
                            placeholder = {
                                Text(
                                    text = "Buscar por nome ou descrição de playlist…",
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Buscar playlists",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            trailingIcon = {
                                if (playlistQuery.isNotEmpty()) {
                                    IconButton(onClick = { playlistQuery = "" }) {
                                        Icon(
                                            imageVector = Icons.Default.Clear,
                                            contentDescription = "Limpar busca",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(22.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                            )
                        )
                    }

                    // ───────────────────────────────────────────────
                    // 3. Smart Playlists (Coleções Inteligentes)
                    // ───────────────────────────────────────────────
                    if (playlistQuery.isBlank()) {
                        item {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            text = "Coleções inteligentes",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onBackground
                                        )
                                        Text(
                                            text = "Atualizadas automaticamente",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    SmartPlaylistCard(
                                        title = "Favoritas",
                                        subtitle = "Músicas que você curtiu",
                                        songCount = favoriteSongs.size,
                                        icon = Icons.Default.Favorite,
                                        topSong = favoriteSongs.firstOrNull(),
                                        gradient = listOf(Color(0xFFFF007F), Color(0xFFFF5252)),
                                        modifier = Modifier.width(160.dp),
                                        testTag = "smart_playlist_favorites",
                                        onPlayClick = {
                                            context.hapticTick()
                                            if (favoriteSongs.isNotEmpty()) {
                                                onPlaySongFromList(favoriteSongs, 0)
                                            }
                                        },
                                        onClick = {
                                            context.hapticTick()
                                            val dummyPlaylist = PlaylistWithSongs(
                                                playlist = Playlist(id = 0, name = "Músicas Favoritas", description = "Suas músicas curtidas com coração", gradientIndex = 1),
                                                songs = favoriteSongs
                                            )
                                            onOpenPlaylistDetail(dummyPlaylist)
                                        }
                                    )

                                    SmartPlaylistCard(
                                        title = "Recentes",
                                        subtitle = "Últimas faixas tocadas",
                                        songCount = recentlyPlayed.size,
                                        icon = Icons.Default.History,
                                        topSong = recentlyPlayed.firstOrNull(),
                                        gradient = listOf(Color(0xFF2979FF), Color(0xFF00E5FF)),
                                        modifier = Modifier.width(160.dp),
                                        testTag = "smart_playlist_recent",
                                        onPlayClick = {
                                            context.hapticTick()
                                            if (recentlyPlayed.isNotEmpty()) {
                                                onPlaySongFromList(recentlyPlayed, 0)
                                            }
                                        },
                                        onClick = {
                                            context.hapticTick()
                                            val dummyPlaylist = PlaylistWithSongs(
                                                playlist = Playlist(id = -1, name = "Tocadas Recentemente", description = "Histórico de faixas reproduzidas", gradientIndex = 4),
                                                songs = recentlyPlayed
                                            )
                                            onOpenPlaylistDetail(dummyPlaylist)
                                        }
                                    )

                                    SmartPlaylistCard(
                                        title = "Mais Tocadas",
                                        subtitle = "Seus maiores hits",
                                        songCount = mostPlayed.size,
                                        icon = Icons.Default.LocalFireDepartment,
                                        topSong = mostPlayed.firstOrNull(),
                                        gradient = listOf(Color(0xFFFF6B35), Color(0xFFFFD166)),
                                        modifier = Modifier.width(160.dp),
                                        testTag = "smart_playlist_most_played",
                                        onPlayClick = {
                                            context.hapticTick()
                                            if (mostPlayed.isNotEmpty()) {
                                                onPlaySongFromList(mostPlayed, 0)
                                            }
                                        },
                                        onClick = {
                                            context.hapticTick()
                                            val dummyPlaylist = PlaylistWithSongs(
                                                playlist = Playlist(id = -2, name = "Mais Tocadas", description = "Suas músicas mais escutadas", gradientIndex = 2),
                                                songs = mostPlayed
                                            )
                                            onOpenPlaylistDetail(dummyPlaylist)
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // ───────────────────────────────────────────────
                    // 4. Section: Custom Playlists
                    // ───────────────────────────────────────────────
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = if (playlistQuery.isBlank()) "Suas playlists" else "Resultados da busca",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    text = if (playlistQuery.isBlank()) {
                                        "${visiblePlaylists.size} ${if (visiblePlaylists.size == 1) "coleção" else "coleções"} • ${sortOrder.title}"
                                    } else {
                                        "${visiblePlaylists.size} ${if (visiblePlaylists.size == 1) "coleção encontrada" else "coleções encontradas"}"
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // Sort Menu
                            Box {
                                OutlinedButton(
                                    onClick = { showSortMenu = true },
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Sort,
                                        contentDescription = "Ordenar",
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = sortOrder.title,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                DropdownMenu(
                                    expanded = showSortMenu,
                                    onDismissRequest = { showSortMenu = false }
                                ) {
                                    PlaylistSortOrder.entries.forEach { order ->
                                        val isSelected = sortOrder == order
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    text = order.title,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                                )
                                            },
                                            onClick = {
                                                showSortMenu = false
                                                sortOrder = order
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (isLoading && playlistsWithSongs.isEmpty()) {
                        items(4) { index ->
                            PlaylistItemSkeleton(animDelay = index * 100)
                        }
                    } else if (visiblePlaylists.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(28.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(56.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primaryContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.QueueMusic,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(14.dp))
                                    Text(
                                        text = if (playlistQuery.isBlank()) "Nenhuma playlist criada ainda" else "Nenhuma playlist encontrada",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = if (playlistQuery.isBlank()) {
                                            "Crie coleções personalizadas para agrupar suas faixas por humor, momento ou gênero."
                                        } else {
                                            "Tente outro termo ou limpe a busca para ver todas as suas playlists."
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Button(
                                        onClick = if (playlistQuery.isBlank()) onCreatePlaylistClick else { { playlistQuery = "" } },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                    ) {
                                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(if (playlistQuery.isBlank()) "Criar Minha Primeira Playlist" else "Limpar busca")
                                    }
                                }
                            }
                        }
                    } else {
                        itemsIndexed(visiblePlaylists, key = { _, p -> p.playlist.id }) { _, item ->
                            val gradientColors = PLAYLIST_GRADIENTS.getOrElse(item.playlist.gradientIndex) { PLAYLIST_GRADIENTS[0] }
                            val totalDurationMs = item.songs.sumOf { it.durationMs }
                            val firstSongWithCover = item.songs.firstOrNull { !it.coverUri.isNullOrBlank() } ?: item.songs.firstOrNull()

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { context.hapticTick(); onOpenPlaylistDetail(item) }
                                    .testTag("playlist_item_${item.playlist.id}"),
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.65f)
                                ),
                                border = BorderStroke(
                                    1.dp,
                                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Visual Playlist Thumbnail
                                    Box(
                                        modifier = Modifier
                                            .size(62.dp)
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(Brush.linearGradient(gradientColors)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (firstSongWithCover != null) {
                                            SongCoverArt(
                                                song = firstSongWithCover,
                                                modifier = Modifier.fillMaxSize(),
                                                cornerRadius = 16.dp
                                            )
                                        } else {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.QueueMusic,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(28.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(14.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = item.playlist.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        if (item.playlist.description.isNotBlank()) {
                                            Text(
                                                text = item.playlist.description,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "${item.songs.size} ${if (item.songs.size == 1) "faixa" else "faixas"}${if (totalDurationMs > 0) " • ${formatPlaylistDuration(totalDurationMs)}" else ""}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }

                                    // Quick Play Button on Playlist Card
                                    if (item.songs.isNotEmpty()) {
                                        FilledIconButton(
                                            onClick = {
                                                context.hapticTick()
                                                onPlaySongFromList(item.songs, 0)
                                            },
                                            modifier = Modifier.size(38.dp),
                                            colors = IconButtonDefaults.filledIconButtonColors(
                                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                                contentColor = MaterialTheme.colorScheme.primary
                                            )
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.PlayArrow,
                                                contentDescription = "Tocar ${item.playlist.name}",
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(4.dp))
                                    }

                                    // Delete Playlist Button
                                    IconButton(
                                        onClick = { context.hapticTick(); playlistToDelete = item.playlist },
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .testTag("delete_playlist_btn_${item.playlist.id}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DeleteOutline,
                                            contentDescription = "Excluir playlist",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun SmartPlaylistCard(
    title: String,
    subtitle: String,
    songCount: Int,
    icon: ImageVector,
    topSong: Song?,
    gradient: List<Color>,
    modifier: Modifier = Modifier,
    testTag: String = "",
    onPlayClick: () -> Unit = {},
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(144.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .testTag(testTag),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.linearGradient(gradient))
                .padding(14.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.Start
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.22f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    if (songCount > 0) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.28f))
                                .clickable { onPlayClick() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Tocar $title",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "$songCount ${if (songCount == 1) "faixa" else "faixas"}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.88f)
                    )
                }
            }
        }
    }
}

@Composable
fun PlaylistDetailView(
    playlistWithSongs: PlaylistWithSongs,
    allSongs: List<Song>,
    currentPlayingSong: Song?,
    isPlaying: Boolean,
    onBack: () -> Unit,
    onPlaySong: (List<Song>, Int) -> Unit,
    onToggleFavorite: (Song) -> Unit,
    onAddSong: (Long) -> Unit,
    onRemoveSong: (Long) -> Unit,
    onDeletePlaylist: () -> Unit
) {
    val gradient = PLAYLIST_GRADIENTS.getOrElse(playlistWithSongs.playlist.gradientIndex) { PLAYLIST_GRADIENTS[0] }
    val isCustomPlaylist = playlistWithSongs.playlist.id > 0
    var showAddSongsDialog by remember { mutableStateOf(false) }
    var trackSearchQuery by remember { mutableStateOf("") }
    val context = LocalContext.current

    val displayedSongs by remember(playlistWithSongs.songs, trackSearchQuery) {
        derivedStateOf {
            if (trackSearchQuery.isBlank()) {
                playlistWithSongs.songs
            } else {
                playlistWithSongs.songs.filter {
                    it.title.contains(trackSearchQuery, ignoreCase = true) ||
                        it.artist.contains(trackSearchQuery, ignoreCase = true)
                }
            }
        }
    }

    val totalDurationMs = playlistWithSongs.songs.sumOf { it.durationMs }
    val firstSong = playlistWithSongs.songs.firstOrNull { !it.coverUri.isNullOrBlank() } ?: playlistWithSongs.songs.firstOrNull()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.background
                    )
                )
            )
            .padding(horizontal = 16.dp)
            .testTag("playlist_detail_screen")
    ) {
        // Top Back Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { context.hapticTick(); onBack() },
                    modifier = Modifier.testTag("playlist_detail_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Voltar",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = playlistWithSongs.playlist.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (isCustomPlaylist) {
                IconButton(
                    onClick = onDeletePlaylist,
                    modifier = Modifier.testTag("delete_playlist_from_detail")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Excluir Playlist",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        // Playlist Banner Card with rich cover artwork & gradient
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.linearGradient(gradient))
                    .padding(18.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (firstSong != null) {
                            SongCoverArt(
                                song = firstSong,
                                modifier = Modifier.fillMaxSize(),
                                cornerRadius = 16.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.QueueMusic,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(34.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = playlistWithSongs.playlist.name,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (playlistWithSongs.playlist.description.isNotBlank()) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = playlistWithSongs.playlist.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.88f),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${playlistWithSongs.songs.size} ${if (playlistWithSongs.songs.size == 1) "música" else "músicas"}${if (totalDurationMs > 0) " • ${formatPlaylistDuration(totalDurationMs)}" else ""}",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White.copy(alpha = 0.85f),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Play, Shuffle, and Add Songs Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    context.hapticTick()
                    if (displayedSongs.isNotEmpty()) {
                        onPlaySong(displayedSongs, 0)
                    }
                },
                enabled = displayedSongs.isNotEmpty(),
                modifier = Modifier
                    .weight(1f)
                    .testTag("play_playlist_button"),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Tocar", fontWeight = FontWeight.SemiBold)
            }

            FilledTonalButton(
                onClick = {
                    context.hapticTick()
                    if (displayedSongs.isNotEmpty()) {
                        val shuffled = displayedSongs.shuffled()
                        onPlaySong(shuffled, 0)
                    }
                },
                enabled = displayedSongs.isNotEmpty(),
                modifier = Modifier
                    .weight(1f)
                    .testTag("shuffle_playlist_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Default.Shuffle, contentDescription = null, modifier = Modifier.size(17.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Aleatório", fontWeight = FontWeight.SemiBold)
            }

            if (isCustomPlaylist) {
                OutlinedButton(
                    onClick = { context.hapticTick(); showAddSongsDialog = true },
                    modifier = Modifier.testTag("add_songs_to_playlist_btn"),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.PlaylistAdd,
                        contentDescription = "Adicionar músicas",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Optional filter within playlist if > 4 songs
        if (playlistWithSongs.songs.size >= 4) {
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
                value = trackSearchQuery,
                onValueChange = { trackSearchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Filtrar músicas nesta playlist…", style = MaterialTheme.typography.bodySmall) },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                },
                trailingIcon = {
                    if (trackSearchQuery.isNotEmpty()) {
                        IconButton(onClick = { trackSearchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Limpar busca", modifier = Modifier.size(18.dp))
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                )
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Songs list in playlist
        if (displayedSongs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 30.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (trackSearchQuery.isNotBlank()) "Nenhuma música corresponde à busca" else "Esta playlist está vazia",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (trackSearchQuery.isNotBlank()) "Tente buscar por outro termo." else "Adicione músicas da sua biblioteca para começar a ouvir.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                    if (isCustomPlaylist && trackSearchQuery.isBlank()) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = { showAddSongsDialog = true },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier.testTag("empty_state_add_songs_button")
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Adicionar Músicas")
                        }
                    } else if (trackSearchQuery.isNotBlank()) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = { trackSearchQuery = "" },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Limpar filtro")
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                itemsIndexed(displayedSongs, key = { _, s -> s.id }) { index, song ->
                    val isCurrent = currentPlayingSong?.id == song.id

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { context.hapticTick(); onPlaySong(displayedSongs, index) }
                            .testTag("playlist_track_${song.id}"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isCurrent) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                            else MaterialTheme.colorScheme.surface.copy(alpha = 0.65f)
                        ),
                        border = BorderStroke(
                            width = if (isCurrent) 1.5.dp else 1.dp,
                            color = if (isCurrent) MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                            else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = if (isCurrent) 2.dp else 0.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Track number
                            Text(
                                text = "${index + 1}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.width(22.dp)
                            )

                            // Cover thumbnail with playing overlay
                            Box(contentAlignment = Alignment.Center) {
                                SongCoverArt(
                                    song = song,
                                    modifier = Modifier.size(48.dp),
                                    cornerRadius = 12.dp
                                )
                                if (isCurrent) {
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(
                                                if (isPlaying) Color.Black.copy(alpha = 0.5f)
                                                else MaterialTheme.colorScheme.primary.copy(alpha = 0.22f)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (isPlaying) Icons.Default.GraphicEq else Icons.Default.PlayArrow,
                                            contentDescription = "Tocando",
                                            tint = if (isPlaying) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = song.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.SemiBold,
                                    color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = song.artist,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f, fill = false)
                                    )
                                    Text(
                                        text = " • ${formatPlaylistTime(song.durationMs)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                }
                                if (!song.isAvailable) {
                                    Text(
                                        text = "Arquivo indisponível",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }

                            // Favorite Icon
                            IconButton(
                                onClick = { context.hapticTick(); onToggleFavorite(song) },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = if (song.isFavorite) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                                    contentDescription = "Favoritar",
                                    tint = if (song.isFavorite) Color(0xFFFF4081) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            // Remove from playlist button (only for custom playlists)
                            if (isCustomPlaylist) {
                                IconButton(
                                    onClick = { context.hapticTick(); onRemoveSong(song.id) },
                                    modifier = Modifier
                                        .size(36.dp)
                                        .testTag("remove_song_from_playlist_${song.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = "Remover da playlist",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(88.dp))
                }
            }
        }
    }

    // Add Songs to Playlist Picker Dialog
    if (showAddSongsDialog) {
        val existingSongIds = remember(playlistWithSongs.songs) {
            playlistWithSongs.songs.map { it.id }.toSet()
        }

        AddSongsToPlaylistPicker(
            playlistName = playlistWithSongs.playlist.name,
            allSongs = allSongs,
            existingSongIds = existingSongIds,
            onDismiss = { showAddSongsDialog = false },
            onToggleSong = { songId ->
                if (existingSongIds.contains(songId)) {
                    onRemoveSong(songId)
                } else {
                    onAddSong(songId)
                }
            }
        )
    }
}

fun formatPlaylistDuration(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val hours = minutes / 60
    val remainingMinutes = minutes % 60
    return if (hours > 0) {
        "${hours}h ${remainingMinutes}m"
    } else {
        "${minutes} min"
    }
}

fun formatPlaylistTime(ms: Long): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.ROOT, "%d:%02d", minutes, seconds)
}
