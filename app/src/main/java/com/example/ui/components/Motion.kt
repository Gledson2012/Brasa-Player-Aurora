package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp

/**
 * Microinteração de toque: escala o elemento enquanto ele está pressionado.
 * Passe o mesmo [interactionSource] usado no `clickable` do componente
 * para que o estado de "pressionado" seja compartilhado.
 */
@Composable
fun Modifier.pressScale(
    interactionSource: MutableInteractionSource,
    pressedScale: Float = 0.96f
): Modifier {
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) pressedScale else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "press_scale"
    )
    return graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

/**
 * Microinteração: pulso elástico quando [active] muda para `true`
 * (ex.: coração de favoritar "pulinho" ao ativar).
 */
@Composable
fun Modifier.pulseOnChange(active: Boolean): Modifier {
    val scale = remember { Animatable(1f) }
    LaunchedEffect(active) {
        if (active) {
            scale.snapTo(0.7f)
            scale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
        }
    }
    val current = scale.value
    return graphicsLayer {
        scaleX = current
        scaleY = current
    }
}

/**
 * Ícone de play/pause com transição animada (fade + escala com bounce)
 * ao alternar entre os estados de reprodução e pausa.
 */
@Composable
fun AnimatedPlayPauseIcon(
    isPlaying: Boolean,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = Color.White,
    iconSize: Dp
) {
    AnimatedContent(
        targetState = isPlaying,
        transitionSpec = {
            val enter = fadeIn(animationSpec = tween(120)) + scaleIn(
                initialScale = 0.55f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
            val exit = fadeOut(animationSpec = tween(80)) + scaleOut(
                targetScale = 0.55f,
                animationSpec = tween(80)
            )
            enter.togetherWith(exit)
        },
        label = "play_pause_transition"
    ) { playing ->
        Icon(
            imageVector = if (playing) Icons.Default.Pause else Icons.Default.PlayArrow,
            contentDescription = contentDescription,
            tint = tint,
            modifier = modifier.size(iconSize)
        )
    }
}
