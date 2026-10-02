package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Song
import com.example.ui.components.SongCoverArt
import com.example.ui.components.TrackItemSkeleton
import com.example.ui.components.formatTimeMs
import com.example.ui.components.hapticTick
import com.example.ui.viewmodel.delegate.SortOption

private enum class TrackFilter(val label: String) {
    ALL("Todas"),
    FAVORITES("Favoritas"),
    MOST_PLAYED("Mais tocadas"),
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
    var showSortMenu by remember { mutableStateOf(false) }

    val visibleSongs by remember(songs, selectedFilter) {
        derivedStateOf {
            when (selectedFilter) {
                TrackFilter.ALL -> songs
                TrackFilter.FAVORITES -> songs.filter(Song::isFavorite)
                TrackFilter.MOST_PLAYED -> songs.filter { it.playCount > 0 }.sortedByDescending { it.playCount }
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
                TrackFilter.MOST_PLAYED to songs.count { it.playCount > 0 },
                TrackFilter.AVAILABLE to songs.count(Song::isAvailable),
                TrackFilter.UNAVAILABLE to songs.count { !it.isAvailable }
            )
        }
    }

    // Only show unavailable filter if there are actually unavailable songs
    val availableFilters = remember(songCounts) {
        TrackFilter.entries.filter { filter ->
            filter != TrackFilter.UNAVAILABLE || (songCounts[TrackFilter.UNAVAILABLE] ?: 0) > 0
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.background
                    )
                )
            )
            .padding(horizontal = 16.dp)
            .testTag("tracks_screen")
    ) {
        // ───────────────────────────────────────────────
        // 1. Sleek Compact Header Bar
        // ───────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 12.dp),
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
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column {
                    Text(
                        text = "Músicas",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${songs.size} ${if (songs.size == 1) "faixa" else "faixas"} • ${formatTotalDuration(songs)}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            OutlinedButton(
                onClick = onOpenFiles,
                shape = RoundedCornerShape(14.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)),
                modifier = Modifier.testTag("open_device_files_button")
            ) {
                Icon(
                    imageVector = Icons.Default.FileOpen,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Importar",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        // ───────────────────────────────────────────────
        // 2. Modern Search Bar
        // ───────────────────────────────────────────────
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("search_tracks_input"),
            placeholder = {
                Text(
                    text = "Buscar por título, artista, álbum ou gênero…",
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
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

        Spacer(modifier = Modifier.height(10.dp))

        // ───────────────────────────────────────────────
        // 3. Action Controls: Play All, Shuffle, Sort
        // ───────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { onPlayAll(visibleSongs) },
                enabled = visibleSongs.isNotEmpty(),
                modifier = Modifier
                    .weight(1f)
                    .testTag("play_all_button"),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 9.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Tocar tudo",
                    fontWeight = FontWeight.SemiBold
                )
            }

            FilledTonalButton(
                onClick = { onShuffleAll(visibleSongs) },
                enabled = visibleSongs.isNotEmpty(),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 9.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Shuffle,
                    contentDescription = null,
                    modifier = Modifier.size(17.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Aleatório",
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Sort Dropdown
            Box {
                OutlinedButton(
                    onClick = { showSortMenu = true },
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 9.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Sort,
                        contentDescription = "Ordenar",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                DropdownMenu(
                    expanded = showSortMenu,
                    onDismissRequest = { showSortMenu = false }
                ) {
                    SortOption.entries.forEach { option ->
                        val isSelected = currentSort == option
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = option.title,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            },
                            leadingIcon = if (isSelected) {
                                {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            } else null,
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

        // ───────────────────────────────────────────────
        // 4. Quick Filter Chips Row
        // ───────────────────────────────────────────────
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("track_filters"),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(availableFilters) { filter ->
                val isSelected = selectedFilter == filter
                val count = songCounts[filter] ?: 0
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedFilter = filter },
                    label = {
                        Text(
                            text = "${filter.label} ($count)",
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // ───────────────────────────────────────────────
        // 5. Track List with Pull-to-Refresh & Rich States
        // ───────────────────────────────────────────────
        if (isLoading && songs.isEmpty()) {
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
                    .padding(horizontal = 24.dp, vertical = 40.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (searchQuery.isNotBlank()) Icons.Default.Search
                            else if (selectedFilter == TrackFilter.FAVORITES) Icons.Default.Favorite
                            else Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Text(
                        text = if (searchQuery.isNotBlank()) {
                            "Nenhum resultado para \"$searchQuery\""
                        } else if (selectedFilter == TrackFilter.FAVORITES) {
                            "Nenhuma música favorita"
                        } else if (selectedFilter == TrackFilter.MOST_PLAYED) {
                            "Nenhuma música tocada ainda"
                        } else if (songs.isEmpty()) {
                            "Sua biblioteca está vazia"
                        } else {
                            "Nenhuma música encontrada"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = if (searchQuery.isNotBlank()) {
                            "Tente buscar com outros termos de título, artista ou álbum."
                        } else if (selectedFilter == TrackFilter.FAVORITES) {
                            "Toque no coração em qualquer música para adicioná-la às favoritas."
                        } else if (songs.isEmpty()) {
                            "Importe músicas do armazenamento do seu celular para começar a ouvir."
                        } else {
                            "Tente selecionar outro filtro de biblioteca acima."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    if (searchQuery.isNotBlank()) {
                        Button(
                            onClick = { onSearchChange("") },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Limpar busca")
                        }
                    } else if (selectedFilter != TrackFilter.ALL) {
                        Button(
                            onClick = { selectedFilter = TrackFilter.ALL },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Ver todas as músicas")
                        }
                    } else if (songs.isEmpty()) {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(
                                onClick = onOpenFiles,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.FileOpen, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Importar áudios")
                            }
                            OutlinedButton(
                                onClick = onRefresh,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Atualizar")
                            }
                        }
                    }
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

                    // Informative Library Footer
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp, bottom = 96.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            HorizontalDivider(
                                modifier = Modifier
                                    .fillMaxWidth(0.6f)
                                    .padding(bottom = 12.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                            )
                            Text(
                                text = "${visibleSongs.size} ${if (visibleSongs.size == 1) "faixa listada" else "faixas listadas"} • ${formatTotalDuration(visibleSongs)}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Brasa Player Aurora",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }
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
            containerColor = if (isCurrent) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
            else MaterialTheme.colorScheme.surface.copy(alpha = 0.65f)
        ),
        border = BorderStroke(
            width = if (isCurrent) 1.5.dp else 1.dp,
            color = if (isCurrent) MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
            else MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isCurrent) 2.dp else 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Cover thumbnail with playing overlay
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
                                if (isPlaying) Color.Black.copy(alpha = 0.52f)
                                else MaterialTheme.colorScheme.primary.copy(alpha = 0.22f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.GraphicEq else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Reproduzindo" else "Pausado",
                            tint = if (isPlaying) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(24.dp)
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
                    if (song.album.isNotBlank() && song.album != song.title) {
                        Text(
                            text = " • ${song.album}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                    }
                    Text(
                        text = " • ${formatTimeMs(song.durationMs)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
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
                    contentDescription = if (song.isFavorite) "Remover dos favoritos" else "Favoritar",
                    tint = if (song.isFavorite) Color(0xFFFF4081) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.size(20.dp)
                )
            }

            // More Options Dropdown
            Box {
                IconButton(
                    onClick = { showTrackMenu = true },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Mais opções",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }

                DropdownMenu(
                    expanded = showTrackMenu,
                    onDismissRequest = { showTrackMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Tocar agora") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        onClick = {
                            showTrackMenu = false
                            onClick()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Adicionar à playlist") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.PlaylistAdd,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        onClick = {
                            showTrackMenu = false
                            onAddToPlaylist()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Compartilhar") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        onClick = {
                            showTrackMenu = false
                            val shareText = buildString {
                                appendLine("🎵 ${song.title}")
                                appendLine("🎤 ${song.artist}")
                                appendLine("💿 ${song.album}")
                                if (song.genre.isNotBlank() && song.genre != "Geral") {
                                    appendLine("🏷️ ${song.genre}")
                                }
                                appendLine()
                                appendLine("Ouvindo no Brasa Player Aurora")
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
                        text = { Text("Editar informações") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        onClick = {
                            showTrackMenu = false
                            onEditSong()
                        }
                    )
                    if (!song.isAvailable) {
                        DropdownMenuItem(
                            text = { Text("Escolher arquivo novamente") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.FileOpen,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            onClick = {
                                showTrackMenu = false
                                onRelinkSong()
                            }
                        )
                    }
                    DropdownMenuItem(
                        text = { Text("Excluir música", color = MaterialTheme.colorScheme.error) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
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

private fun formatTotalDuration(songs: List<Song>): String {
    val totalMs = songs.sumOf { it.durationMs }
    val totalMinutes = totalMs / 60000
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return if (hours > 0) {
        "${hours}h ${minutes}min"
    } else {
        "${minutes}min"
    }
}
