package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Modern skeleton loading placeholder with shimmer animation.
 * Use this to show elegant loading states instead of simple progress indicators.
 */
@Composable
fun SkeletonLoading(
    modifier: Modifier = Modifier,
    shimmerEnabled: Boolean = true
) {
    val colors = listOf(
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
    )

    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmerTranslate"
    )

    val brush = Brush.linearGradient(
        colors = colors,
        start = Offset(translateAnim - 200f, translateAnim - 200f),
        end = Offset(translateAnim, translateAnim)
    )

    if (shimmerEnabled) {
        Box(
            modifier = modifier
                .clip(RoundedCornerShape(12.dp))
                .background(brush)
        )
    } else {
        Box(
            modifier = modifier
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
        )
    }
}

/**
 * Track item skeleton - mimics the structure of a track list item
 */
@Composable
fun TrackItemSkeleton(
    modifier: Modifier = Modifier,
    animDelay: Int = 0
) {
    AnimatedVisibility(
        visible = true,
        enter = fadeIn(animationSpec = tween(durationMillis = 400, delayMillis = animDelay)) +
                slideInVertically(animationSpec = tween(durationMillis = 400, delayMillis = animDelay)) { it / 4 },
        modifier = modifier
    ) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Album art placeholder
        SkeletonLoading(
            modifier = Modifier.size(56.dp),
            shimmerEnabled = true
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Title placeholder
            SkeletonLoading(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(16.dp)
            )

            // Artist placeholder
            SkeletonLoading(
                modifier = Modifier
                    .fillMaxWidth(0.45f)
                    .height(14.dp)
            )
        }

        // Duration placeholder
        SkeletonLoading(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
        )
    }
    } // end AnimatedVisibility
}

/**
 * Playlist item skeleton - mimics the structure of a playlist card
 */
@Composable
fun PlaylistItemSkeleton(
    modifier: Modifier = Modifier,
    animDelay: Int = 0
) {
    AnimatedVisibility(
        visible = true,
        enter = fadeIn(animationSpec = tween(durationMillis = 400, delayMillis = animDelay)) +
                slideInVertically(animationSpec = tween(durationMillis = 400, delayMillis = animDelay)) { it / 4 },
        modifier = modifier
    ) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Cover art placeholder (16:9 ratio)
        SkeletonLoading(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(14.dp))
        )

        // Title placeholder
        SkeletonLoading(
            modifier = Modifier
                .fillMaxWidth(0.75f)
                .height(16.dp)
        )

        // Description placeholder
        SkeletonLoading(
            modifier = Modifier
                .fillMaxWidth(0.55f)
                .height(12.dp)
        )
    }
    } // end AnimatedVisibility
}

/**
 * Mini player skeleton - mimics the structure of the mini player bar
 */
@Composable
fun MiniPlayerSkeleton(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Album art placeholder
        SkeletonLoading(
            modifier = Modifier.size(48.dp),
            shimmerEnabled = true
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Title placeholder
            SkeletonLoading(
                modifier = Modifier
                    .fillMaxWidth(0.65f)
                    .height(14.dp)
            )

            // Artist placeholder
            SkeletonLoading(
                modifier = Modifier
                    .fillMaxWidth(0.4f)
                    .height(12.dp)
            )
        }

        // Play button placeholder
        SkeletonLoading(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
        )
    }
}

/**
 * Equalizer band skeleton - mimics the structure of an equalizer slider
 */
@Composable
fun EqualizerBandSkeleton(
    modifier: Modifier = Modifier,
    animDelay: Int = 0
) {
    AnimatedVisibility(
        visible = true,
        enter = fadeIn(animationSpec = tween(durationMillis = 400, delayMillis = animDelay)) +
                slideInVertically(animationSpec = tween(durationMillis = 400, delayMillis = animDelay)) { it / 4 },
        modifier = modifier
    ) {
    Column(
        modifier = Modifier
            .width(60.dp)
            .height(180.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
    ) {
        // Gain value placeholder
        SkeletonLoading(
            modifier = Modifier
                .width(40.dp)
                .height(14.dp)
        )

        // Slider track placeholder
        SkeletonLoading(
            modifier = Modifier
                .width(8.dp)
                .weight(1f)
                .clip(RoundedCornerShape(4.dp))
        )

        // Frequency label placeholder
        SkeletonLoading(
            modifier = Modifier
                .width(48.dp)
                .height(12.dp)
        )
    }
    } // end AnimatedVisibility
}

/**
 * Section header skeleton
 */
@Composable
fun SectionHeaderSkeleton(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Icon placeholder
        SkeletonLoading(
            modifier = Modifier.size(40.dp)
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Title placeholder
            SkeletonLoading(
                modifier = Modifier
                    .fillMaxWidth(0.4f)
                    .height(18.dp)
            )

            // Subtitle placeholder
            SkeletonLoading(
                modifier = Modifier
                    .fillMaxWidth(0.25f)
                    .height(12.dp)
            )
        }
    }
}
