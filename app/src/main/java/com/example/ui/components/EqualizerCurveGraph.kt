package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun EqualizerCurveGraph(
    bandLevels: List<Int>, // 5 bands in dB (-10 to +10)
    isEnabled: Boolean,
    modifier: Modifier = Modifier,
    height: Dp = 100.dp,
    primaryColor: Color = MaterialTheme.colorScheme.primary,
    secondaryColor: Color = MaterialTheme.colorScheme.tertiary
) {
    // Animate band levels smoothly
    val b0 by animateFloatAsState(targetValue = bandLevels.getOrElse(0) { 0 }.toFloat(), animationSpec = tween(200), label = "b0")
    val b1 by animateFloatAsState(targetValue = bandLevels.getOrElse(1) { 0 }.toFloat(), animationSpec = tween(200), label = "b1")
    val b2 by animateFloatAsState(targetValue = bandLevels.getOrElse(2) { 0 }.toFloat(), animationSpec = tween(200), label = "b2")
    val b3 by animateFloatAsState(targetValue = bandLevels.getOrElse(3) { 0 }.toFloat(), animationSpec = tween(200), label = "b3")
    val b4 by animateFloatAsState(targetValue = bandLevels.getOrElse(4) { 0 }.toFloat(), animationSpec = tween(200), label = "b4")

    val animatedBands = listOf(b0, b1, b2, b3, b4)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val canvasHeight = size.height
            val midY = canvasHeight / 2f

            val strokeColor = if (isEnabled) primaryColor else Color.Gray.copy(alpha = 0.5f)
            val gradientFillColor = if (isEnabled) primaryColor.copy(alpha = 0.22f) else Color.Transparent

            // Grid center 0 dB line
            drawLine(
                color = strokeColor.copy(alpha = 0.15f),
                start = Offset(0f, midY),
                end = Offset(width, midY),
                strokeWidth = 1.dp.toPx()
            )

            // Grid +/- 5 dB lines
            val top5 = midY - (5f / 10f) * (canvasHeight * 0.42f)
            val bot5 = midY + (5f / 10f) * (canvasHeight * 0.42f)
            drawLine(
                color = strokeColor.copy(alpha = 0.08f),
                start = Offset(0f, top5),
                end = Offset(width, top5),
                strokeWidth = 1.dp.toPx()
            )
            drawLine(
                color = strokeColor.copy(alpha = 0.08f),
                start = Offset(0f, bot5),
                end = Offset(width, bot5),
                strokeWidth = 1.dp.toPx()
            )

            val count = animatedBands.size
            val points = mutableListOf<Offset>()

            for (i in 0 until count) {
                val x = (i.toFloat() / (count - 1).coerceAtLeast(1)) * width
                // gain is between -10 and +10 dB
                val gain = animatedBands[i]
                // map gain: +10dB -> near top (0.08 of height), -10dB -> near bottom (0.92 of height)
                val normalized = (gain / 10f).coerceIn(-1.0f, 1.0f)
                val y = midY - (normalized * (canvasHeight * 0.42f))
                points.add(Offset(x, y))
            }

            // Create smooth curve Path using cubic Beziers
            val curvePath = Path()
            val fillPath = Path()

            if (points.isNotEmpty()) {
                curvePath.moveTo(points[0].x, points[0].y)
                fillPath.moveTo(points[0].x, canvasHeight)
                fillPath.lineTo(points[0].x, points[0].y)

                for (i in 0 until points.size - 1) {
                    val p0 = points[i]
                    val p1 = points[i + 1]
                    val controlX1 = p0.x + (p1.x - p0.x) / 2f
                    val controlY1 = p0.y
                    val controlX2 = p0.x + (p1.x - p0.x) / 2f
                    val controlY2 = p1.y

                    curvePath.cubicTo(controlX1, controlY1, controlX2, controlY2, p1.x, p1.y)
                    fillPath.cubicTo(controlX1, controlY1, controlX2, controlY2, p1.x, p1.y)
                }

                fillPath.lineTo(points.last().x, canvasHeight)
                fillPath.close()

                // Draw gradient under the curve
                if (isEnabled) {
                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                gradientFillColor,
                                primaryColor.copy(alpha = 0.05f),
                                Color.Transparent
                            )
                        )
                    )
                }

                // Draw main curve stroke
                drawPath(
                    path = curvePath,
                    brush = Brush.horizontalGradient(
                        colors = listOf(primaryColor, secondaryColor, primaryColor)
                    ),
                    style = Stroke(
                        width = 3.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                )

                // Draw glowing anchor points on the frequency nodes
                points.forEach { pt ->
                    if (isEnabled) {
                        drawCircle(
                            color = primaryColor.copy(alpha = 0.35f),
                            radius = 6.dp.toPx(),
                            center = pt
                        )
                        drawCircle(
                            color = primaryColor,
                            radius = 3.5.dp.toPx(),
                            center = pt
                        )
                    } else {
                        drawCircle(
                            color = Color.Gray.copy(alpha = 0.6f),
                            radius = 3.dp.toPx(),
                            center = pt
                        )
                    }
                }
            }
        }
    }
}
