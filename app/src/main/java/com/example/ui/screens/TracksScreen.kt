package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Song
import com.example.ui.components.SongCoverArt
import com.example.ui.components.SectionHeader
import com.example.ui.components.TrackItemSkeleton
import com.example.ui.components.formatTimeMs
import com.example.ui.components.hapticTick
import com.example.ui.viewmodel.SortOption

private enum class TrackFilter(val label: String) {
    ALL("Todas"),
    FAVORITES("Favoritas"),
    AVAILABLE("Disponíveis"),
    UNAVAILABLE("Indisponíveis")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TracksScreen(
    songs: List<Song>,
    isLoading: Boolean = false,
    isRefreshing: Boolean = false,
    onRefresh: () -> Unit = {},
    currentPlayingSong: Song?,
    isPlaying: Boolean,
    searchQuery: String,
    currentSort: SortOption,
    onSearchChange: (String) -> Unit,
    onSortChange: (SortOption) -> Unit,
    onOpenFiles: () -> Unit,
    onSongClick: (songs: List<Song>, index: Int) -> Unit,
    onPlayAll: (songs: List<Song>) -> Unit,
    onShuffleAll: (songs: List<Song>) -> Unit,
    onToggleFavorite: (Song) -> Unit,
    onAddToPlaylist: (Song) -> Unit,
    onEditSong: (Song) -> Unit,
    onRelinkSong: (Song) -> Unit,
    onDeleteSong: (Song) -> Unit
) {
    var selectedFilter by remember { mutableStateOf(TrackFilter.ALL) }
    val visibleSongs by remember(songs, selectedFilter) {
        derivedStateOf {
            when (selectedFilter) {
                TrackFilter.ALL -> songs
                TrackFilter.FAVORITES -> songs.filter(Song::isFavorite)
                TrackFilter.AVAILABLE -> songs.filter(Song::isAvailable)
                TrackFilter.UNAVAILABLE -> songs.filterNot(Song::isAvailable)
            }
        }
    }
    val songCounts by remember(songs) {
        derivedStateOf {
            mapOf(
                TrackFilter.ALL to songs.size,
                TrackFilter.FAVORITES to songs.count(Song::isFavorite),
                TrackFilter.AVAILABLE to songs.count(Song::isAvailable),
                TrackFilter.UNAVAILABLE to songs.count { !it.isAvailable }
            )
        }
    }

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
            .testTag("tracks_screen")
    ) {
        SectionHeader(
            title = "Músicas",
            subtitle = "Encontre sua próxima faixa em segundos",
            icon = Icons.Default.MusicNote
        )

        LibraryOverviewCard(
            totalSongs = songs.size,
            availableSongs = songs.count(Song::isAvailable),
            currentSort = currentSort,
            onOpenFiles = onOpenFiles
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("search_tracks_input"),
            placeholder = { Text("Buscar por título, artista ou álbum…") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Buscar",
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchChange("") }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Limpar busca"
                        )
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

        Spacer(modifier = Modifier.height(8.dp))

        LibraryControlsCard(
            selectedFilter = selectedFilter,
            songCounts = songCounts,
            visibleSongs = visibleSongs,
            searchQuery = searchQuery,
            currentSort = currentSort,
            onSelectFilter = { selectedFilter = it },
            onPlayAll = { onPlayAll(visibleSongs) },
            onShuffleAll = { onShuffleAll(visibleSongs) },
            onSortChange = onSortChange
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Songs List
        if (isLoading && songs.isEmpty()) {
            // Show skeleton loading while initial data is loading
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(8) { index ->
                    TrackItemSkeleton(animDelay = index * 80)
                }
            }
        } else if (visibleSongs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 40.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (songs.isEmpty()) "Biblioteca vazia" else "Nenhuma música encontrada",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (songs.isEmpty()) {
                            "Experimente escanear o armazenamento local na aba Temas."
                        } else {
                            "Ajuste os filtros ou tente outra busca para encontrar uma faixa."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        } else {
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = onRefresh,
                modifier = Modifier.fillMaxSize()
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    itemsIndexed(visibleSongs, key = { _, s -> s.id }) { index, song ->
                        val isCurrent = currentPlayingSong?.id == song.id
                        TrackListItem(
                            song = song,
                            isCurrent = isCurrent,
                            isPlaying = isPlaying && isCurrent,
                            onClick = { onSongClick(visibleSongs, index) },
                            onToggleFavorite = { onToggleFavorite(song) },
                            onAddToPlaylist = { onAddToPlaylist(song) },
                            onEditSong = { onEditSong(song) },
                            onRelinkSong = { onRelinkSong(song) },
                            onDeleteSong = { onDeleteSong(song) }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(90.dp))
                    }
                }
            } // end PullToRefreshBox
        }
    }
}

@Composable
fun TrackListItem(
    song: Song,
    isCurrent: Boolean,
    isPlaying: Boolean,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onAddToPlaylist: () -> Unit,
    onEditSong: () -> Unit,
    onRelinkSong: () -> Unit,
    onDeleteSong: () -> Unit
) {
    var showTrackMenu by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { context.hapticTick(); onClick() }
            .testTag("track_item_${song.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrent) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            else MaterialTheme.colorScheme.surface.copy(alpha = 0.62f)
        ),
        border = BorderStroke(
            1.dp,
            if (isCurrent) MaterialTheme.colorScheme.primary.copy(alpha = 0.36f)
            else MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isCurrent) 2.dp else 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Cover thumbnail with playing indicator
            Box(contentAlignment = Alignment.Center) {
                SongCoverArt(
                    song = song,
                    modifier = Modifier.size(54.dp),
                    cornerRadius = 14.dp
                )

                if (isCurrent) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                if (isPlaying) Color.Black.copy(alpha = 0.5f)
                                else MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
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

            // Track details
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
                        text = " • ${formatTimeMs(song.durationMs)}",
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

            // Favorite Button
            IconButton(
                onClick = onToggleFavorite,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = if (song.isFavorite) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = "Favoritar",
                    tint = if (song.isFavorite) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.size(20.dp)
                )
            }

            // More Options
            Box {
                IconButton(
                    onClick = { showTrackMenu = true },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Opções",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }

                DropdownMenu(
                    expanded = showTrackMenu,
                    onDismissRequest = { showTrackMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Adicionar à Playlist") },
                        onClick = {
                            showTrackMenu = false
                            onAddToPlaylist()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Editar metadados") },
                        onClick = {
                            showTrackMenu = false
                            onEditSong()
                        }
                    )
                    if (!song.isAvailable) {
                        DropdownMenuItem(
                            text = { Text("Escolher arquivo novamente") },
                            onClick = {
                                showTrackMenu = false
                                onRelinkSong()
                            }
                        )
                    }
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
                            showTrackMenu = false
                            onDeleteSong()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun LibraryControlsCard(
    selectedFilter: TrackFilter,
    songCounts: Map<TrackFilter, Int>,
    visibleSongs: List<Song>,
    searchQuery: String,
    currentSort: SortOption,
    onSelectFilter: (TrackFilter) -> Unit,
    onPlayAll: () -> Unit,
    onShuffleAll: () -> Unit,
    onSortChange: (SortOption) -> Unit
) {
    var showSortMenu by remember { mutableStateOf(false) }
    val resultLabel = if (searchQuery.isNotBlank() || selectedFilter != TrackFilter.ALL) {
        "${visibleSongs.size} ${if (visibleSongs.size == 1) "faixa encontrada" else "faixas encontradas"}"
    } else {
        "Tudo em um só lugar"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.56f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Organizar biblioteca",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = resultLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (searchQuery.isNotBlank() || selectedFilter != TrackFilter.ALL) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
                Box {
                    IconButton(
                        onClick = { showSortMenu = true },
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.48f))
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Sort,
                            contentDescription = "Ordenar lista",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false }
                    ) {
                        SortOption.values().forEach { option ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = option.title,
                                        fontWeight = if (currentSort == option) FontWeight.Bold else FontWeight.Normal,
                                        color = if (currentSort == option) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                },
                                onClick = {
                                    showSortMenu = false
                                    onSortChange(option)
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.26f))
                    .padding(horizontal = 4.dp, vertical = 3.dp)
                    .testTag("track_filters"),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(TrackFilter.values().toList()) { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { onSelectFilter(filter) },
                        label = { Text("${filter.label} ${songCounts.getValue(filter)}") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
            )
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onPlayAll,
                    enabled = visibleSongs.isNotEmpty(),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("play_all_button"),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(5.dp))
                    Text("Tocar tudo")
                }

                FilledTonalButton(
                    onClick = onShuffleAll,
                    enabled = visibleSongs.isNotEmpty(),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Shuffle, contentDescription = null, modifier = Modifier.size(17.dp))
                    Spacer(modifier = Modifier.width(5.dp))
                    Text("Aleatório")
                }
            }
        }
    }
}

@Composable
private fun LibraryOverviewCard(
    totalSongs: Int,
    availableSongs: Int,
    currentSort: SortOption,
    onOpenFiles: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(
                            MaterialTheme.colorScheme.primaryContainer,
                            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.72f)
                        )
                    )
                )
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "SUA BIBLIOTECA",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.1.sp
                    )
                    Text(
                        text = if (totalSongs == 1) "1 faixa" else "$totalSongs faixas",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$availableSongs disponíveis • ${currentSort.title}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(
                onClick = onOpenFiles,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("open_device_files_button"),
                contentPadding = PaddingValues(vertical = 10.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.FileOpen, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Abrir arquivos do celular")
            }
        }
    }
}
