package com.example.ui.screens

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.WavingHand
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PlaylistWithSongs
import com.example.data.model.Song
import com.example.ui.components.SongCoverArt

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
    modifier: Modifier = Modifier
) {
    val recent = recentlyPlayed.ifEmpty { allSongs }.take(12)
    val popular = mostPlayed.ifEmpty { allSongs }.take(12)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 18.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Sua música",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Um espaço para ouvir sem pressa.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    imageVector = Icons.Default.WavingHand,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(26.dp)
                )
            }
        }

        item {
            if (currentSong != null) {
                ContinueListeningCard(
                    song = currentSong,
                    isPlaying = isPlaying,
                    onPlayPause = onPlayPause
                )
            } else {
                WelcomeCard(
                    onOpenTracks = onOpenTracks,
                    onOpenPlaylists = onOpenPlaylists
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onOpenTracks,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(vertical = 13.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(Icons.Default.LibraryMusic, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Explorar músicas")
                }
                OutlinedButton(
                    onClick = onOpenPlaylists,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(vertical = 13.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.QueueMusic, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Playlists")
                }
            }
        }

        if (recent.isNotEmpty()) {
            item {
                SongSection(
                    title = "Continue ouvindo",
                    songs = recent,
                    onPlaySong = onPlaySong
                )
            }
        }

        if (favoriteSongs.isNotEmpty()) {
            item {
                SongSection(
                    title = "Seus favoritos",
                    songs = favoriteSongs.take(12),
                    onPlaySong = onPlaySong,
                    accent = MaterialTheme.colorScheme.tertiary
                )
            }
        }

        if (popular.isNotEmpty()) {
            item {
                SongSection(
                    title = "Mais tocadas",
                    songs = popular,
                    onPlaySong = onPlaySong
                )
            }
        }

        if (playlists.isNotEmpty()) {
            item {
                Text(
                    text = "Suas playlists",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(10.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(playlists.take(8), key = { it.playlist.id }) { playlist ->
                        PlaylistPreviewCard(playlist = playlist, onClick = onOpenPlaylists)
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
    onPlayPause: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(
                            MaterialTheme.colorScheme.primaryContainer,
                            MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                )
                .padding(18.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                SongCoverArt(song = song, modifier = Modifier.size(82.dp), cornerRadius = 18.dp)
                Spacer(Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "TOCANDO AGORA",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )
                    Spacer(Modifier.height(5.dp))
                    Text(
                        text = song.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = song.artist,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                FilledIconButton(
                    onClick = onPlayPause,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pausar" else "Reproduzir"
                    )
                }
            }
        }
    }
}

@Composable
private fun WelcomeCard(
    onOpenTracks: () -> Unit,
    onOpenPlaylists: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Comece a ouvir",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Importe suas faixas ou escolha uma playlist para começar.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(onClick = onOpenTracks, shape = RoundedCornerShape(12.dp)) {
                    Text("Ver músicas")
                }
                OutlinedButton(onClick = onOpenPlaylists, shape = RoundedCornerShape(12.dp)) {
                    Text("Ver playlists")
                }
            }
        }
    }
}

@Composable
private fun SongSection(
    title: String,
    songs: List<Song>,
    onPlaySong: (List<Song>, Int) -> Unit,
    accent: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(10.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(songs, key = { it.id }) { song ->
                SongHomeCard(
                    song = song,
                    accent = accent,
                    onClick = { onPlaySong(songs, songs.indexOf(song)) }
                )
            }
        }
    }
}

@Composable
private fun SongHomeCard(
    song: Song,
    accent: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(142.dp)
            .clickable(onClick = onClick)
    ) {
        SongCoverArt(
            song = song,
            modifier = Modifier
                .size(142.dp)
                .clip(RoundedCornerShape(18.dp)),
            cornerRadius = 18.dp
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = song.title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = song.artist,
            style = MaterialTheme.typography.bodySmall,
            color = accent,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun PlaylistPreviewCard(
    playlist: PlaylistWithSongs,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(190.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SongCoverArt(
                song = playlist.songs.firstOrNull(),
                modifier = Modifier.size(52.dp),
                cornerRadius = 14.dp
            )
            Spacer(Modifier.width(10.dp))
            Column {
                Text(
                    text = playlist.playlist.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${playlist.songs.size} faixas",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
