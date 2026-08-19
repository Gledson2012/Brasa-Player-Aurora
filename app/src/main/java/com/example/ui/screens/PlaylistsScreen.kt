package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Playlist
import com.example.data.model.PlaylistWithSongs
import com.example.data.model.Song
import com.example.ui.components.AddSongsToPlaylistPicker
import com.example.ui.components.DeletePlaylistConfirmDialog
import com.example.ui.components.PLAYLIST_GRADIENTS
import com.example.ui.components.SectionHeader
import com.example.ui.components.SongCoverArt

@Composable
fun PlaylistsScreen(
    playlistsWithSongs: List<PlaylistWithSongs>,
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
    val visiblePlaylists = playlistsWithSongs.filter { item ->
        playlistQuery.isBlank() || item.playlist.name.contains(playlistQuery, ignoreCase = true) ||
            item.playlist.description.contains(playlistQuery, ignoreCase = true)
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
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .testTag("playlists_screen"),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    SectionHeader(
                        title = "Playlists",
                        subtitle = "Organize momentos, estilos e descobertas",
                        icon = Icons.AutoMirrored.Filled.QueueMusic
                    )

                    OutlinedTextField(
                        value = playlistQuery,
                        onValueChange = { playlistQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("search_playlists_input"),
                        placeholder = { Text("Buscar playlist…") },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "Buscar playlists")
                        },
                        trailingIcon = {
                            if (playlistQuery.isNotEmpty()) {
                                IconButton(onClick = { playlistQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Limpar busca")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = Color.Transparent
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Create Playlist Button Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onCreatePlaylistClick() }
                            .testTag("create_playlist_button"),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f)
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary))),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(26.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Criar Nova Playlist",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "Organize suas faixas favoritas em coleções",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }

                item {
                    // Smart Playlists Section
                    Text(
                        text = "Coleções Inteligentes",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        SmartPlaylistCard(
                            title = "Favoritas",
                            songCount = favoriteSongs.size,
                            icon = Icons.Default.Favorite,
                            gradient = listOf(Color(0xFFFF007F), Color(0xFFFF5252)),
                            modifier = Modifier.weight(1f),
                            testTag = "smart_playlist_favorites",
                            onClick = {
                                val dummyPlaylist = PlaylistWithSongs(
                                    playlist = Playlist(id = 0, name = "Músicas Favoritas", description = "Suas músicas curtidas com coração", gradientIndex = 1),
                                    songs = favoriteSongs
                                )
                                onOpenPlaylistDetail(dummyPlaylist)
                            }
                        )

                        SmartPlaylistCard(
                            title = "Recentes",
                            songCount = recentlyPlayed.size,
                            icon = Icons.Default.History,
                            gradient = listOf(Color(0xFF2979FF), Color(0xFF00E5FF)),
                            modifier = Modifier.weight(1f),
                            testTag = "smart_playlist_recent",
                            onClick = {
                                val dummyPlaylist = PlaylistWithSongs(
                                    playlist = Playlist(id = -1, name = "Tocadas Recentemente", description = "Histórico de faixas reproduzidas", gradientIndex = 4),
                                    songs = recentlyPlayed
                                )
                                onOpenPlaylistDetail(dummyPlaylist)
                            }
                        )

                        SmartPlaylistCard(
                            title = "Mais Tocadas",
                            songCount = mostPlayed.size,
                            icon = Icons.Default.LocalFireDepartment,
                            gradient = listOf(Color(0xFFFF6B35), Color(0xFFFFD166)),
                            modifier = Modifier.weight(1f),
                            testTag = "smart_playlist_most_played",
                            onClick = {
                                val dummyPlaylist = PlaylistWithSongs(
                                    playlist = Playlist(id = -2, name = "Mais Tocadas", description = "Suas músicas mais escutadas", gradientIndex = 2),
                                    songs = mostPlayed
                                )
                                onOpenPlaylistDetail(dummyPlaylist)
                            }
                        )
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Playlists Personalizadas (${visiblePlaylists.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }

                if (visiblePlaylists.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.QueueMusic,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = if (playlistQuery.isBlank()) "Nenhuma playlist criada ainda" else "Nenhuma playlist encontrada",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (playlistQuery.isBlank()) {
                                        "Toque em \"Criar Nova Playlist\" para começar a montar suas próprias seleções."
                                    } else {
                                        "Tente outro termo ou limpe a busca para ver todas as suas playlists."
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(14.dp))
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

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOpenPlaylistDetail(item) }
                                .testTag("playlist_item_${item.playlist.id}"),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Brush.linearGradient(gradientColors)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                    imageVector = Icons.AutoMirrored.Filled.QueueMusic,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(28.dp)
                                    )
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
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = if (item.playlist.description.isNotBlank()) item.playlist.description
                                        else "${item.songs.size} ${if (item.songs.size == 1) "música" else "músicas"}${if (totalDurationMs > 0) " • ${formatPlaylistDuration(totalDurationMs)}" else ""}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                // Delete Playlist Button
                                IconButton(
                                    onClick = { playlistToDelete = item.playlist },
                                    modifier = Modifier.testTag("delete_playlist_btn_${item.playlist.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = "Excluir playlist",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(90.dp))
                }
            }
        }
    }
}

@Composable
fun SmartPlaylistCard(
    title: String,
    songCount: Int,
    icon: ImageVector,
    gradient: List<Color>,
    modifier: Modifier = Modifier,
    testTag: String = "",
    onClick: () -> Unit
) {            Card(
                            modifier = modifier
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { onClick() }
                                .testTag(testTag),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                            border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Brush.linearGradient(gradient)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )

            Text(
                text = "$songCount faixas",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
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

    val totalDurationMs = playlistWithSongs.songs.sumOf { it.durationMs }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("playlist_detail_screen")
    ) {
        // Back Button & Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("playlist_detail_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Voltar",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
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

        // Playlist Banner Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.linearGradient(gradient))
                    .padding(20.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Column {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.QueueMusic,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(38.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = playlistWithSongs.playlist.name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
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
                        color = Color.White.copy(alpha = 0.8f)
                    )
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
                    if (playlistWithSongs.songs.isNotEmpty()) {
                        onPlaySong(playlistWithSongs.songs, 0)
                    }
                },
                enabled = playlistWithSongs.songs.isNotEmpty(),
                modifier = Modifier
                    .weight(1f)
                    .testTag("play_playlist_button"),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Tocar")
            }

            FilledTonalButton(
                onClick = {
                    if (playlistWithSongs.songs.isNotEmpty()) {
                        val shuffled = playlistWithSongs.songs.shuffled()
                        onPlaySong(shuffled, 0)
                    }
                },
                enabled = playlistWithSongs.songs.isNotEmpty(),
                modifier = Modifier
                    .weight(1f)
                    .testTag("shuffle_playlist_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Default.Shuffle, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Aleatório")
            }

            if (isCustomPlaylist) {
                OutlinedButton(
                    onClick = { showAddSongsDialog = true },
                    modifier = Modifier.testTag("add_songs_to_playlist_btn"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.PlaylistAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Songs list in playlist
        if (playlistWithSongs.songs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 30.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Esta playlist está vazia",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Adicione músicas da sua biblioteca para começar a ouvir.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                    if (isCustomPlaylist) {
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
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                itemsIndexed(playlistWithSongs.songs, key = { _, s -> s.id }) { index, song ->
                    val isCurrent = currentPlayingSong?.id == song.id

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onPlaySong(playlistWithSongs.songs, index) }
                            .testTag("playlist_track_${song.id}"),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isCurrent) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        )
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

                            // Cover thumbnail
                            Box(contentAlignment = Alignment.Center) {
                                SongCoverArt(
                                    song = song,
                                    modifier = Modifier.size(42.dp),
                                    cornerRadius = 8.dp
                                )
                                if (isCurrent && isPlaying) {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color.Black.copy(alpha = 0.4f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = "Tocando",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(10.dp))

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
                            }

                            // Favorite Icon
                            IconButton(
                                onClick = { onToggleFavorite(song) },
                                modifier = Modifier.size(34.dp)
                            ) {
                                Icon(
                                    imageVector = if (song.isFavorite) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                                    contentDescription = "Favoritar",
                                    tint = if (song.isFavorite) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            // Remove from playlist button (only for custom playlists)
                            if (isCustomPlaylist) {
                                IconButton(
                                    onClick = { onRemoveSong(song.id) },
                                    modifier = Modifier
                                        .size(34.dp)
                                        .testTag("remove_song_from_playlist_${song.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = "Remover da playlist",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(90.dp))
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
    return String.format("%d:%02d", minutes, seconds)
}
