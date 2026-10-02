package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class NavItem(
    val index: Int,
    val title: String,
    val icon: ImageVector,
    val tag: String
)

val NAV_ITEMS = listOf(
    NavItem(0, "Início", Icons.Default.Home, "nav_item_0"),
    NavItem(1, "Músicas", Icons.Default.MusicNote, "nav_item_1"),
    NavItem(2, "Playlists", Icons.AutoMirrored.Filled.QueueMusic, "nav_item_2"),
    NavItem(3, "Equalizador", Icons.Default.GraphicEq, "nav_item_3"),
    NavItem(4, "Temas", Icons.Default.Palette, "nav_item_4"),
    NavItem(5, "Rádio", Icons.Default.Radio, "nav_item_5")
)

/**
 * Modern Aurora Floating Navigation Bar.
 *
 * Features:
 * - Glassmorphic floating island container with glowing gradient border.
 * - Smooth spring scale animation on active icon and micro glowing pill.
 * - Dynamic live playing audio badge on "Músicas" or "Rádio" when sound is active.
 * - Tactile haptic feedback on tab changes.
 * - Balanced 6-item layout with optimized legibility on all screen sizes.
 */
@Composable
fun AuroraNavBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    isPlayingSong: Boolean = false,
    isPlayingRadio: Boolean = false
) {
    val context = LocalContext.current

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(26.dp),
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.28f)
            )
            .clip(RoundedCornerShape(26.dp))
            .border(
                BorderStroke(
                    width = 1.dp,
                    brush = Brush.horizontalGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.45f),
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.25f),
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)
                        )
                    )
                ),
                shape = RoundedCornerShape(26.dp)
            )
            .testTag("main_bottom_nav"),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
        tonalElevation = 6.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)
                        )
                    )
                )
                .padding(horizontal = 4.dp, vertical = 6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NAV_ITEMS.forEach { item ->
                    val isSelected = selectedTab == item.index
                    val hasLiveIndicator = when (item.index) {
                        1 -> isPlayingSong
                        5 -> isPlayingRadio
                        else -> false
                    }

                    AuroraNavItem(
                        item = item,
                        isSelected = isSelected,
                        hasLiveIndicator = hasLiveIndicator,
                        onClick = {
                            context.hapticTick()
                            onTabSelected(item.index)
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun AuroraNavItem(
    item: NavItem,
    isSelected: Boolean,
    hasLiveIndicator: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val iconScale by animateFloatAsState(
        targetValue = if (isSelected) 1.15f else 0.92f,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = 400f),
        label = "iconScale"
    )

    val contentColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f),
        animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
        label = "contentColor"
    )

    val pillBackground by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
        else Color.Transparent,
        animationSpec = tween(durationMillis = 200),
        label = "pillBackground"
    )

    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .background(pillBackground)
            .padding(vertical = 4.dp, horizontal = 2.dp)
            .testTag(item.tag),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.title,
                    tint = contentColor,
                    modifier = Modifier
                        .size(22.dp)
                        .scale(iconScale)
                )

                // Micro Live Playing Badge
                if (hasLiveIndicator) {
                    MicroLivePulse(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 1.dp, end = 1.dp)
                    )
                }
            }

            Spacer(Modifier.height(3.dp))

            Text(
                text = item.title,
                fontSize = 10.sp,
                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                color = contentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(2.dp))

            // Active Glowing Pill Underline
            AnimatedVisibility(
                visible = isSelected,
                enter = fadeIn(tween(180)),
                exit = fadeOut(tween(120))
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 12.dp, height = 2.5.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                )
            }
            if (!isSelected) {
                Spacer(Modifier.height(2.5.dp))
            }
        }
    }
}

@Composable
private fun MicroLivePulse(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "micro_pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(650),
            repeatMode = RepeatMode.Reverse
        ),
        label = "micro_alpha"
    )

    Box(
        modifier = modifier
            .size(6.dp)
            .clip(CircleShape)
            .background(Color(0xFF00E676).copy(alpha = alpha))
    )
}
