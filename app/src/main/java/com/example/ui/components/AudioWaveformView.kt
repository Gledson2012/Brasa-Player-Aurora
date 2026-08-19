package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.sin

@Composable
fun AudioWaveformView(
    samples: List<Float>,
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    height: Dp = 64.dp,
    primaryColor: Color,
    secondaryColor: Color,
    tertiaryColor: Color
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
    ) {
        val values = if (samples.isEmpty()) List(2) { 0.1f } else samples
        val centerY = size.height / 2f
        val step = size.width / (values.size - 1).coerceAtLeast(1)
        val amplitudeScale = if (isPlaying) size.height * 0.42f else size.height * 0.08f
        val top = Path()
        val bottom = Path()

        values.forEachIndexed { index, value ->
            val x = index * step
            val envelope = value.coerceIn(0.05f, 1f)
            val shimmer = if (isPlaying) 0.84f + 0.16f * sin(index * 0.55f) else 0.7f
            val halfHeight = (envelope * shimmer * amplitudeScale).coerceAtLeast(2.dp.toPx())
            val topY = centerY - halfHeight
            val bottomY = centerY + halfHeight
            if (index == 0) {
                top.moveTo(x, topY)
                bottom.moveTo(x, bottomY)
            } else {
                top.lineTo(x, topY)
                bottom.lineTo(x, bottomY)
            }
        }

        val fill = Path().apply {
            addPath(top)
            for (index in values.indices.reversed()) {
                val x = index * step
                val envelope = values[index].coerceIn(0.05f, 1f)
                val halfHeight = (envelope * amplitudeScale).coerceAtLeast(2.dp.toPx())
                lineTo(x, centerY + halfHeight)
            }
            close()
        }

        drawPath(
            path = fill,
            brush = Brush.verticalGradient(
                listOf(primaryColor.copy(alpha = 0.28f), secondaryColor.copy(alpha = 0.05f))
            )
        )
        drawPath(
            path = top,
            brush = Brush.horizontalGradient(listOf(primaryColor, secondaryColor, tertiaryColor)),
            style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
        )
        drawPath(
            path = bottom,
            brush = Brush.horizontalGradient(listOf(secondaryColor, primaryColor, tertiaryColor)),
            style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
        )
        drawLine(
            color = primaryColor.copy(alpha = 0.12f),
            start = Offset(0f, centerY),
            end = Offset(size.width, centerY),
            strokeWidth = 1.dp.toPx()
        )
    }
}
