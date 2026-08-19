package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.data.model.VisualizerStyle
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun VisualizerView(
    amplitudes: FloatArray,
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    style: VisualizerStyle = VisualizerStyle.BARS,
    height: Dp = 60.dp,
    barCount: Int = 28,
    primaryColor: Color = MaterialTheme.colorScheme.primary,
    secondaryColor: Color = MaterialTheme.colorScheme.secondary,
    tertiaryColor: Color = MaterialTheme.colorScheme.tertiary
) {
    val phaseAnim = remember { Animatable(0f) }

    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            phaseAnim.animateTo(
                targetValue = (2 * PI).toFloat(),
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 3000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                )
            )
        } else {
            phaseAnim.snapTo(0f)
        }
    }

    val gradientBrush = remember(primaryColor, secondaryColor, tertiaryColor) {
        Brush.horizontalGradient(
            colors = listOf(primaryColor, secondaryColor, tertiaryColor)
        )
    }

    val verticalGradientBrush = remember(primaryColor, secondaryColor) {
        Brush.verticalGradient(
            colors = listOf(secondaryColor, primaryColor)
        )
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
    ) {
        val width = size.width
        val canvasHeight = size.height

        when (style) {
            VisualizerStyle.BARS -> {
                val totalBars = barCount.coerceAtMost(amplitudes.size)
                val spacing = 3.dp.toPx()
                val totalSpacing = spacing * (totalBars - 1)
                val barWidth = ((width - totalSpacing) / totalBars).coerceAtLeast(2.dp.toPx())

                for (i in 0 until totalBars) {
                    val ampIndex = (i * amplitudes.size / totalBars).coerceIn(0, amplitudes.size - 1)
                    val rawAmp = if (isPlaying) amplitudes[ampIndex] else 0.08f
                    val barHeight = (rawAmp * canvasHeight * 0.9f).coerceIn(4.dp.toPx(), canvasHeight)

                    val x = i * (barWidth + spacing)
                    val y = canvasHeight - barHeight

                    drawRoundRect(
                        brush = verticalGradientBrush,
                        topLeft = Offset(x, y),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(barWidth / 2, barWidth / 2)
                    )
                }
            }

            VisualizerStyle.WAVEFORM -> {
                val path = Path()
                val steps = 60
                val midY = canvasHeight / 2f
                val baseAmp = if (isPlaying) canvasHeight * 0.38f else 4.dp.toPx()

                path.moveTo(0f, midY)
                for (i in 0..steps) {
                    val x = (i.toFloat() / steps) * width
                    val ampFactor = if (isPlaying && amplitudes.isNotEmpty()) {
                        val index = (i * amplitudes.size / steps).coerceIn(0, amplitudes.size - 1)
                        amplitudes[index]
                    } else 0.1f

                    val wave = sin((i * 0.2f) + phaseAnim.value) * baseAmp * ampFactor
                    val y = midY + wave.toFloat()
                    if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }

                drawPath(
                    path = path,
                    brush = gradientBrush,
                    style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            VisualizerStyle.SPECTRUM -> {
                val path = Path()
                val totalPoints = 40
                path.moveTo(0f, canvasHeight)

                for (i in 0..totalPoints) {
                    val x = (i.toFloat() / totalPoints) * width
                    val ampIndex = (i * amplitudes.size / totalPoints).coerceIn(0, amplitudes.size - 1)
                    val amp = if (isPlaying) amplitudes[ampIndex] else 0.05f
                    val y = canvasHeight - (amp * canvasHeight * 0.85f).coerceAtLeast(3.dp.toPx())
                    path.lineTo(x, y)
                }

                path.lineTo(width, canvasHeight)
                path.close()

                drawPath(
                    path = path,
                    brush = Brush.verticalGradient(
                        colors = listOf(primaryColor.copy(alpha = 0.8f), secondaryColor.copy(alpha = 0.2f))
                    ),
                    style = Fill
                )

                // Outline line on top
                val linePath = Path()
                for (i in 0..totalPoints) {
                    val x = (i.toFloat() / totalPoints) * width
                    val ampIndex = (i * amplitudes.size / totalPoints).coerceIn(0, amplitudes.size - 1)
                    val amp = if (isPlaying) amplitudes[ampIndex] else 0.05f
                    val y = canvasHeight - (amp * canvasHeight * 0.85f).coerceAtLeast(3.dp.toPx())
                    if (i == 0) linePath.moveTo(x, y) else linePath.lineTo(x, y)
                }

                drawPath(
                    path = linePath,
                    brush = gradientBrush,
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            VisualizerStyle.CIRCULAR_PULSE -> {
                val centerX = width / 2f
                val centerY = canvasHeight / 2f
                val maxRadius = minOf(centerX, centerY) * 0.9f
                val avgAmp = if (isPlaying) amplitudes.average().toFloat().coerceIn(0.1f, 1f) else 0.1f

                for (r in 1..4) {
                    val ringRadius = maxRadius * (r / 4f) * (0.8f + avgAmp * 0.3f)
                    val alpha = ((1f - (r / 5f)) * (0.3f + avgAmp * 0.7f)).coerceIn(0f, 1f)
                    drawCircle(
                        color = if (r % 2 == 0) primaryColor.copy(alpha = alpha) else secondaryColor.copy(alpha = alpha),
                        radius = ringRadius,
                        center = Offset(centerX, centerY),
                        style = Stroke(width = 2.5.dp.toPx())
                    )
                }
            }
        }
    }
}
