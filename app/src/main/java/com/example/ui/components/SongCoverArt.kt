package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.Song

@Composable
fun SongCoverArt(
    song: Song?,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 12.dp,
    fallbackIconSize: Dp = 28.dp
) {
    val context = LocalContext.current
    val shape = RoundedCornerShape(cornerRadius)

    Box(
        modifier = modifier
            .clip(shape)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        if (song != null) {
            if (!song.coverUri.isNullOrBlank()) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(song.coverUri)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Capa do álbum de ${song.title}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                val drawableId = when (song.coverDrawableName) {
                    "cover_synthwave" -> context.resources.getIdentifier("cover_synthwave", "drawable", context.packageName)
                    "cover_lofi" -> context.resources.getIdentifier("cover_lofi", "drawable", context.packageName)
                    "cover_acoustic" -> context.resources.getIdentifier("cover_acoustic", "drawable", context.packageName)
                    "cover_electronic" -> context.resources.getIdentifier("cover_electronic", "drawable", context.packageName)
                    else -> 0
                }

                if (drawableId != 0) {
                    Image(
                        painter = painterResource(id = drawableId),
                        contentDescription = "Capa da música",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(fallbackIconSize)
                    )
                }
            }
        } else {
            Icon(
                imageVector = Icons.Default.MusicNote,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(fallbackIconSize)
            )
        }
    }
}
