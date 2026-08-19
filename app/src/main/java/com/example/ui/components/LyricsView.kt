package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.lyrics.LyricLine
import com.example.data.lyrics.TrackLyrics
import com.example.data.model.Song

@Composable
fun SyncedLyricsView(
    song: Song?,
    lyrics: TrackLyrics?,
    isLoading: Boolean,
    currentPositionMs: Long,
    onSeekTo: (Long) -> Unit,
    onRefreshLyrics: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("synced_lyrics_view"),
        contentAlignment = Alignment.Center
    ) {
        when {
            isLoading -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(40.dp),
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 3.dp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Buscando letras sincronizadas…",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            lyrics == null || lyrics.lines.isEmpty() -> {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Nenhuma letra encontrada",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Esta faixa pode ser instrumental ou não possui sincronização LRC disponível.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    OutlinedButton(
                        onClick = onRefreshLyrics,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Tentar novamente",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Tentar novamente")
                    }
                }
            }

            else -> {
                val listState = rememberLazyListState()

                // Calculate current active line index based on playback position
                val activeIndex by remember(lyrics.lines, currentPositionMs) {
                    derivedStateOf {
                        val lines = lyrics.lines
                        var index = -1
                        for (i in lines.indices) {
                            if (currentPositionMs >= lines[i].timestampMs) {
                                index = i
                            } else {
                                break
                            }
                        }
                        if (index == -1 && lines.isNotEmpty()) 0 else index
                    }
                }

                // Smoothly auto-scroll when active line changes
                var userScrolling by remember { mutableStateOf(false) }

                LaunchedEffect(activeIndex) {
                    if (activeIndex >= 0 && activeIndex < lyrics.lines.size && !userScrolling) {
                        // Scroll to center item with offset
                        listState.animateScrollToItem(
                            index = (activeIndex - 1).coerceAtLeast(0),
                            scrollOffset = 0
                        )
                    }
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        contentPadding = PaddingValues(top = 80.dp, bottom = 120.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Header info
                        item {
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.padding(bottom = 16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MusicNote,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Letras • ${lyrics.source}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }

                        itemsIndexed(
                            items = lyrics.lines,
                            key = { index, line -> "${line.timestampMs}_$index" }
                        ) { index, line ->
                            val isActive = index == activeIndex
                            val isPast = index < activeIndex

                            val targetScale = if (isActive) 1.06f else 0.98f
                            val scale by animateFloatAsState(
                                targetValue = targetScale,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessLow
                                ),
                                label = "lyric_scale"
                            )

                            val targetTextColor = when {
                                isActive -> MaterialTheme.colorScheme.primary
                                isPast -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                            }
                            val textColor by animateColorAsState(
                                targetValue = targetTextColor,
                                animationSpec = spring(stiffness = Spring.StiffnessLow),
                                label = "lyric_color"
                            )

                            val backgroundColor = if (isActive) {
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                            } else {
                                Color.Transparent
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .scale(scale)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(backgroundColor)
                                    .clickable {
                                        onSeekTo(line.timestampMs)
                                    }
                                    .padding(horizontal = 16.dp, vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = line.text,
                                    style = if (isActive) {
                                        MaterialTheme.typography.titleLarge.copy(
                                            fontSize = 22.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            lineHeight = 28.sp
                                        )
                                    } else {
                                        MaterialTheme.typography.bodyLarge.copy(
                                            fontSize = 17.sp,
                                            fontWeight = FontWeight.Medium,
                                            lineHeight = 24.sp
                                        )
                                    },
                                    color = textColor,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }

                    // Top & Bottom Gradient Fades for Smooth Immersion
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .align(Alignment.TopCenter)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.surface,
                                        Color.Transparent
                                    )
                                )
                            )
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(70.dp)
                            .align(Alignment.BottomCenter)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        MaterialTheme.colorScheme.surface
                                    )
                                )
                            )
                    )
                }
            }
        }
    }
}
