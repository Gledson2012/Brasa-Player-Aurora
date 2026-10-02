package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PlaylistWithSongs
import com.example.data.model.Song
import com.example.ui.components.PLAYLIST_GRADIENTS
import com.example.ui.components.SongCoverArt
import com.example.ui.components.formatTimeMs
import java.util.Calendar

@Composable
fun HomeScreen(
    currentSong: Song?,
    isPlaying: Boolean,
    allSongs: List<Song>,
    recentlyPlayed: List<Song>,
    favoriteSongs: List<Song>,
    mostPlayed: List<Song>,
    playlists: List<PlaylistWithSongs>,
    onPlayPause: () -> Unit,
    onPlaySong: (List<Song>, Int) -> Unit,
    onOpenTracks: () -> Unit,
    onOpenPlaylists: () -> Unit,
    onOpenStatistics: () -> Unit = {},
    onOpenBrowser: () -> Unit = {},
    onOpenPlayer: () -> Unit = {},
    onToggleFavorite: (Song) -> Unit = {},
    onShuffleAll: () -> Unit = {},
    onPlayFavorites: () -> Unit = {},
    onOpenEqualizer: () -> Unit = {},
    onOpenRadio: () -> Unit = {},
    onCreatePlaylist: () -> Unit = {},
    onScanMedia: () -> Unit = {},
    currentPositionMs: Long = 0L,
    durationMs: Long = 0L,
    modifier: Modifier = Modifier
) {
    // Dynamic greeting based on time of day
    val (greeting, greetingEmoji) = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when (hour) {
            in 5..11 -> Pair("Bom dia", "☀️")
            in 12..17 -> Pair("Boa tarde", "🌤️")
            else -> Pair("Boa noite", "🌙")
        }
    }

    // Category navigation filter chips
    var selectedCategory by remember { mutableStateOf("Tudo") }
    val categories = remember(recentlyPlayed, favoriteSongs, mostPlayed, playlists) {
        buildList {
            add("Tudo")
            if (recentlyPlayed.isNotEmpty()) add("Recentes")
            if (favoriteSongs.isNotEmpty()) add("Favoritas")
            if (mostPlayed.isNotEmpty()) add("Mais tocadas")
            add("Artistas")
            if (playlists.isNotEmpty()) add("Playlists")
        }
    }

    // Process lists
    val isActualRecent = recentlyPlayed.isNotEmpty()
    val recentSongs = remember(recentlyPlayed, allSongs) {
        if (recentlyPlayed.isNotEmpty()) {
            recentlyPlayed.take(15)
        } else {
            allSongs.sortedByDescending { it.addedTimestamp }.take(15)
        }
    }

    val popularSongs = remember(mostPlayed, allSongs) {
        if (mostPlayed.isNotEmpty()) {
            mostPlayed.take(15)
        } else {
            allSongs.sortedByDescending { it.playCount }.take(15)
        }
    }

    val favoriteList = remember(favoriteSongs) { favoriteSongs.take(15) }

    val artistGroups = remember(allSongs) {
        allSongs.filter { it.artist.isNotBlank() && it.artist != "<unknown>" }
            .groupBy { it.artist }
            .entries
            .sortedByDescending { it.value.size }
            .take(12)
    }

    val totalArtistsCount = remember(allSongs) {
        allSongs.map { it.artist }.distinct().count { it.isNotBlank() && it != "<unknown>" }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
                        MaterialTheme.colorScheme.background.copy(alpha = 0.98f),
                        MaterialTheme.colorScheme.background
                    )
                )
            ),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // ───────────────────────────────────────────────
        // 1. Header with dynamic greeting and quick actions
        // ───────────────────────────────────────────────
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(16.dp))
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
                        imageVector = Icons.Default.GraphicEq,
                        contentDescription = "Brasa Player",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Spacer(Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "BRASA PLAYER AURORA",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.3.sp
                    )
                    Text(
                        text = "$greeting $greetingEmoji",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isPlaying && currentSong != null) {
                            "Ouvindo: ${currentSong.title}"
                        } else if (allSongs.isNotEmpty()) {
                            "${allSongs.size} faixas na sua coleção"
                        } else {
                            "Seu universo musical sem pressa"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (allSongs.isNotEmpty()) {
                    IconButton(
                        onClick = onShuffleAll,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shuffle,
                            contentDescription = "Misturar tudo",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(Modifier.width(8.dp))

                    IconButton(
                        onClick = onOpenTracks,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Buscar faixas",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                } else {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
        }

        // ───────────────────────────────────────────────
        // 2. Interactive Metrics Row
        // ───────────────────────────────────────────────
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HomeMetricCard(
                    value = allSongs.size.toString(),
                    label = "faixas",
                    icon = Icons.Default.MusicNote,
                    accent = MaterialTheme.colorScheme.primary,
                    onClick = onOpenTracks,
                    modifier = Modifier.weight(1f)
                )
                HomeMetricCard(
                    value = favoriteSongs.size.toString(),
                    label = "favoritas",
                    icon = Icons.Default.Favorite,
                    accent = MaterialTheme.colorScheme.tertiary,
                    onClick = {
                        if (favoriteSongs.isNotEmpty()) onPlayFavorites() else onOpenTracks()
                    },
                    modifier = Modifier.weight(1f)
                )
                HomeMetricCard(
                    value = playlists.size.toString(),
                    label = "playlists",
                    icon = Icons.AutoMirrored.Filled.QueueMusic,
                    accent = MaterialTheme.colorScheme.secondary,
                    onClick = onOpenPlaylists,
                    modifier = Modifier.weight(1f)
                )
                HomeMetricCard(
                    value = totalArtistsCount.toString(),
                    label = "artistas",
                    icon = Icons.Default.Person,
                    accent = MaterialTheme.colorScheme.primary,
                    onClick = onOpenBrowser,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // ───────────────────────────────────────────────
        // 3. Category Filter Chips (Pílulas de filtragem)
        // ───────────────────────────────────────────────
        if (categories.size > 2) {
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp)
                ) {
                    items(categories) { cat ->
                        val isSelected = selectedCategory == cat
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategory = cat },
                            label = {
                                Text(
                                    text = cat,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                            )
                        )
                    }
                }
            }
        }

        // ───────────────────────────────────────────────
        // 4. Hero Card: Continue listening or Welcome / Empty
        // ───────────────────────────────────────────────
        item {
            if (currentSong != null) {
                ContinueListeningCard(
                    song = currentSong,
                    isPlaying = isPlaying,
                    currentPositionMs = currentPositionMs,
                    durationMs = durationMs,
                    onPlayPause = onPlayPause,
                    onOpenPlayer = onOpenPlayer,
                    onToggleFavorite = onToggleFavorite,
                    onShuffleAll = onShuffleAll
                )
            } else {
                WelcomeCard(
                    hasSongs = allSongs.isNotEmpty(),
                    songCount = allSongs.size,
                    onOpenTracks = onOpenTracks,
                    onOpenPlaylists = onOpenPlaylists,
                    onShuffleAll = onShuffleAll,
                    onScanMedia = onScanMedia
                )
            }
        }

        // ───────────────────────────────────────────────
        // 5. Quick Actions Hub (Acesso rápido)
        // ───────────────────────────────────────────────
        if (selectedCategory == "Tudo") {
            item {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Acesso rápido",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        QuickActionCard(
                            title = "Aleatório",
                            subtitle = "Toda a biblioteca",
                            icon = Icons.Default.Shuffle,
                            colors = listOf(
                                MaterialTheme.colorScheme.primaryContainer,
                                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.8f)
                            ),
                            iconTint = MaterialTheme.colorScheme.primary,
                            onClick = onShuffleAll,
                            modifier = Modifier.weight(1f)
                        )
                        QuickActionCard(
                            title = "Favoritas",
                            subtitle = "${favoriteSongs.size} faixas",
                            icon = Icons.Default.Favorite,
                            colors = listOf(
                                MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.7f),
                                MaterialTheme.colorScheme.surfaceVariant
                            ),
                            iconTint = MaterialTheme.colorScheme.tertiary,
                            onClick = {
                                if (favoriteSongs.isNotEmpty()) onPlayFavorites() else onOpenTracks()
                            },
                            modifier = Modifier.weight(1f)
                        )
                        QuickActionCard(
                            title = "Playlists",
                            subtitle = "${playlists.size} coleções",
                            icon = Icons.AutoMirrored.Filled.QueueMusic,
                            colors = listOf(
                                MaterialTheme.colorScheme.surfaceVariant,
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                            ),
                            iconTint = MaterialTheme.colorScheme.secondary,
                            onClick = onOpenPlaylists,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        QuickActionCard(
                            title = "Navegar",
                            subtitle = "Artistas & Álbuns",
                            icon = Icons.Default.Category,
                            colors = listOf(
                                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
                                MaterialTheme.colorScheme.surfaceVariant
                            ),
                            iconTint = MaterialTheme.colorScheme.primary,
                            onClick = onOpenBrowser,
                            modifier = Modifier.weight(1f)
                        )
                        QuickActionCard(
                            title = "Estatísticas",
                            subtitle = "Métricas de escuta",
                            icon = Icons.Default.BarChart,
                            colors = listOf(
                                MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.6f),
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                            ),
                            iconTint = MaterialTheme.colorScheme.tertiary,
                            onClick = onOpenStatistics,
                            modifier = Modifier.weight(1f)
                        )
                        QuickActionCard(
                            title = "Rádio",
                            subtitle = "Estações ao vivo",
                            icon = Icons.Default.Radio,
                            colors = listOf(
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                            ),
                            iconTint = MaterialTheme.colorScheme.primary,
                            onClick = onOpenRadio,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // ───────────────────────────────────────────────
        // 6. Section: Recently Played / Recently Added
        // ───────────────────────────────────────────────
        if ((selectedCategory == "Tudo" || selectedCategory == "Recentes") && recentSongs.isNotEmpty()) {
            item {
                SongSection(
                    title = if (isActualRecent) "Tocadas recentemente" else "Adicionadas recentemente",
                    subtitle = if (isActualRecent) "Seu histórico recente" else "Novas faixas da biblioteca",
                    songs = recentSongs,
                    currentSongId = currentSong?.id,
                    isPlaying = isPlaying,
                    onPlaySong = onPlaySong,
                    onActionClick = { onPlaySong(recentSongs, 0) },
                    actionText = "Tocar todas",
                    actionIcon = Icons.Default.PlayArrow
                )
            }
        }

        // ───────────────────────────────────────────────
        // 7. Section: Favorites
        // ───────────────────────────────────────────────
        if ((selectedCategory == "Tudo" || selectedCategory == "Favoritas") && favoriteList.isNotEmpty()) {
            item {
                SongSection(
                    title = "Suas favoritas",
                    subtitle = "Músicas que você mais ama",
                    songs = favoriteList,
                    currentSongId = currentSong?.id,
                    isPlaying = isPlaying,
                    onPlaySong = onPlaySong,
                    onActionClick = { onPlaySong(favoriteList.shuffled(), 0) },
                    actionText = "Aleatório",
                    actionIcon = Icons.Default.Shuffle,
                    accent = MaterialTheme.colorScheme.tertiary
                )
            }
        }

        // ───────────────────────────────────────────────
        // 8. Section: Most Played
        // ───────────────────────────────────────────────
        if ((selectedCategory == "Tudo" || selectedCategory == "Mais tocadas") && popularSongs.isNotEmpty()) {
            item {
                SongSection(
                    title = "Mais tocadas",
                    subtitle = "Os maiores sucessos da sua coleção",
                    songs = popularSongs,
                    currentSongId = currentSong?.id,
                    isPlaying = isPlaying,
                    onPlaySong = onPlaySong,
                    onActionClick = { onPlaySong(popularSongs, 0) },
                    actionText = "Tocar top faixas",
                    actionIcon = Icons.Default.PlayArrow,
                    showRankBadges = true
                )
            }
        }

        // ───────────────────────────────────────────────
        // 9. Section: Featured Artists
        // ───────────────────────────────────────────────
        if ((selectedCategory == "Tudo" || selectedCategory == "Artistas") && artistGroups.isNotEmpty()) {
            item {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Artistas em destaque",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Com base na sua coleção",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        TextButton(
                            onClick = onOpenBrowser,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Ver todos",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        items(artistGroups, key = { it.key }) { entry ->
                            ArtistHomeCard(
                                artistName = entry.key,
                                songs = entry.value,
                                onClick = { onPlaySong(entry.value, 0) }
                            )
                        }
                    }
                }
            }
        }

        // ───────────────────────────────────────────────
        // 10. Section: Playlists
        // ───────────────────────────────────────────────
        if (selectedCategory == "Tudo" || selectedCategory == "Playlists") {
            item {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Suas playlists",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${playlists.size} coleções organizadas",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        TextButton(
                            onClick = onCreatePlaylist,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = "Criar",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Quick Add Playlist card
                        item(key = "create_playlist_home_card") {
                            CreatePlaylistHomeCard(onClick = onCreatePlaylist)
                        }

                        items(playlists.take(10), key = { it.playlist.id }) { playlist ->
                            PlaylistPreviewCard(
                                playlist = playlist,
                                onClick = onOpenPlaylists
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ContinueListeningCard(
    song: Song,
    isPlaying: Boolean,
    currentPositionMs: Long,
    durationMs: Long,
    onPlayPause: () -> Unit,
    onOpenPlayer: () -> Unit,
    onToggleFavorite: (Song) -> Unit,
    onShuffleAll: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onOpenPlayer),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f),
                            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.65f),
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
                .padding(16.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Badge: TOCANDO AGORA / EM PAUSA
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.16f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = if (isPlaying) "REPRODUZINDO AGORA" else "CONTINUE OUVINDO",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }

                    Spacer(Modifier.weight(1f))

                    // Open full player hint
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable(onClick = onOpenPlayer)
                    ) {
                        Text(
                            text = "Tela cheia",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Abrir tela cheia",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(RoundedCornerShape(18.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        SongCoverArt(
                            song = song,
                            modifier = Modifier.fillMaxSize(),
                            cornerRadius = 18.dp
                        )
                    }

                    Spacer(Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = song.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.height(3.dp))
                        Text(
                            text = song.artist,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (song.album.isNotBlank() && song.album != song.title) {
                            Text(
                                text = song.album,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Favorite button
                    IconButton(
                        onClick = { onToggleFavorite(song) },
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            imageVector = if (song.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = if (song.isFavorite) "Remover dos favoritos" else "Adicionar aos favoritos",
                            tint = if (song.isFavorite) Color(0xFFFF4081) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(Modifier.width(4.dp))

                    // Play/Pause button
                    FilledIconButton(
                        onClick = onPlayPause,
                        modifier = Modifier.size(46.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pausar" else "Reproduzir",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // Progress Bar
                if (durationMs > 0) {
                    Spacer(Modifier.height(14.dp))
                    val progressFraction = (currentPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(fraction = progressFraction)
                                .fillMaxHeight()
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(
                                            MaterialTheme.colorScheme.primary,
                                            MaterialTheme.colorScheme.secondary
                                        )
                                    )
                                )
                        )
                    }

                    Spacer(Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = formatTimeMs(currentPositionMs),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = formatTimeMs(durationMs),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WelcomeCard(
    hasSongs: Boolean,
    songCount: Int,
    onOpenTracks: () -> Unit,
    onOpenPlaylists: () -> Unit,
    onShuffleAll: () -> Unit,
    onScanMedia: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        listOf(
                            MaterialTheme.colorScheme.primaryContainer,
                            MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (hasSongs) Icons.Default.MusicNote else Icons.Default.LibraryMusic,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text = if (hasSongs) "Sua música está pronta" else "Comece sua biblioteca",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (hasSongs) "$songCount faixas prontas para reproduzir" else "Nenhuma música adicionada ainda",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(14.dp))
            Text(
                text = if (hasSongs) {
                    "Que tal começar com todas as faixas no modo aleatório ou escolher sua playlist favorita?"
                } else {
                    "Importe arquivos de áudio do seu armazenamento ou escaneie o dispositivo para encontrar suas músicas."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                if (hasSongs) {
                    Button(
                        onClick = onShuffleAll,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shuffle,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("Misturar tudo")
                    }
                    OutlinedButton(
                        onClick = onOpenTracks,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Ver músicas")
                    }
                } else {
                    Button(
                        onClick = onScanMedia,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("Escanear áudio")
                    }
                    OutlinedButton(
                        onClick = onOpenTracks,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Abrir faixas")
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    colors: List<Color>,
    iconTint: Color = MaterialTheme.colorScheme.primary,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(112.dp)
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.linearGradient(colors))
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconTint.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(18.dp)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun SongSection(
    title: String,
    subtitle: String? = null,
    songs: List<Song>,
    currentSongId: Long?,
    isPlaying: Boolean,
    onPlaySong: (List<Song>, Int) -> Unit,
    onActionClick: (() -> Unit)? = null,
    actionText: String = "Tocar todas",
    actionIcon: ImageVector = Icons.Default.PlayArrow,
    accent: Color = MaterialTheme.colorScheme.primary,
    showRankBadges: Boolean = false
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (onActionClick != null && songs.isNotEmpty()) {
                TextButton(
                    onClick = onActionClick,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = actionIcon,
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = actionText,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = accent
                    )
                }
            } else {
                Text(
                    text = "${songs.size} faixas",
                    style = MaterialTheme.typography.labelMedium,
                    color = accent,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            itemsIndexed(songs, key = { index, song -> "${song.id}_$index" }) { index, song ->
                SongHomeCard(
                    song = song,
                    accent = accent,
                    isCurrentSong = currentSongId == song.id,
                    isPlaying = isPlaying,
                    rankBadge = if (showRankBadges) index + 1 else null,
                    onClick = { onPlaySong(songs, index) }
                )
            }
        }
    }
}

@Composable
private fun SongHomeCard(
    song: Song,
    accent: Color = MaterialTheme.colorScheme.primary,
    isCurrentSong: Boolean = false,
    isPlaying: Boolean = false,
    rankBadge: Int? = null,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(148.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrentSong) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
            }
        ),
        border = BorderStroke(
            width = if (isCurrentSong) 1.5.dp else 1.dp,
            color = if (isCurrentSong) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
        )
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Box(
                modifier = Modifier
                    .size(128.dp)
                    .clip(RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.BottomEnd
            ) {
                SongCoverArt(
                    song = song,
                    modifier = Modifier.fillMaxSize(),
                    cornerRadius = 14.dp
                )

                // Scrim overlay on cover bottom
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))
                            )
                        )
                )

                // Rank badge if present
                if (rankBadge != null) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(6.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (rankBadge <= 3) MaterialTheme.colorScheme.primary else Color.Black.copy(alpha = 0.7f)
                            )
                            .padding(horizontal = 7.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "#$rankBadge",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (rankBadge <= 3) MaterialTheme.colorScheme.onPrimary else Color.White
                        )
                    }
                }

                // Favorite heart indicator
                if (song.isFavorite) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(6.dp)
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.6f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Favorita",
                            tint = Color(0xFFFF4081),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                // Duration in bottom-left
                if (song.durationMs > 0) {
                    Text(
                        text = formatTimeMs(song.durationMs),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = Color.White.copy(alpha = 0.9f),
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(start = 6.dp, bottom = 6.dp)
                    )
                }

                // Play / Pause button in bottom-right
                Box(
                    modifier = Modifier
                        .padding(6.dp)
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(
                            if (isCurrentSong && isPlaying) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isCurrentSong && isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isCurrentSong && isPlaying) "Pausar" else "Reproduzir",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
            Text(
                text = song.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = if (isCurrentSong) FontWeight.Bold else FontWeight.SemiBold,
                color = if (isCurrentSong) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = song.artist,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ArtistHomeCard(
    artistName: String,
    songs: List<Song>,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(108.dp)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(92.dp)
                .clip(CircleShape)
                .border(
                    BorderStroke(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
                    CircleShape
                )
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer,
                            MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            val firstSongWithCover = songs.firstOrNull { !it.coverUri.isNullOrBlank() }
            if (firstSongWithCover != null) {
                SongCoverArt(
                    song = firstSongWithCover,
                    modifier = Modifier.fillMaxSize(),
                    cornerRadius = 46.dp
                )
            } else {
                Text(
                    text = artistName.firstOrNull()?.uppercase() ?: "A",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = artistName,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
        Text(
            text = "${songs.size} ${if (songs.size == 1) "faixa" else "faixas"}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun CreatePlaylistHomeCard(
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(170.dp)
            .height(78.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(Modifier.width(10.dp))
            Column {
                Text(
                    text = "Nova Playlist",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Criar agora",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun PlaylistPreviewCard(
    playlist: PlaylistWithSongs,
    onClick: () -> Unit
) {
    val gradientColors = PLAYLIST_GRADIENTS.getOrElse(playlist.playlist.gradientIndex) { PLAYLIST_GRADIENTS[0] }

    Card(
        modifier = Modifier
            .width(200.dp)
            .height(78.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Brush.linearGradient(gradientColors)),
                contentAlignment = Alignment.Center
            ) {
                if (playlist.songs.isNotEmpty()) {
                    SongCoverArt(
                        song = playlist.songs.first(),
                        modifier = Modifier.fillMaxSize(),
                        cornerRadius = 14.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.QueueMusic,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = playlist.playlist.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${playlist.songs.size} ${if (playlist.songs.size == 1) "faixa" else "faixas"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun HomeMetricCard(
    value: String,
    label: String,
    icon: ImageVector,
    accent: Color = MaterialTheme.colorScheme.primary,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
        ),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.25f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(15.dp)
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge,
                    color = accent,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.height(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
