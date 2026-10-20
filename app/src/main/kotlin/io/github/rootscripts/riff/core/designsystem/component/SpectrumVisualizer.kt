package io.github.rootscripts.riff.core.designsystem.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun MiniSpectrumVisualizer(
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    barCount: Int = 12,
    barWidth: Dp = 3.dp,
    barGap: Dp = 2.dp,
    color: Color = Color.White,
    frequencies: FloatArray? = null
) {
    var animPhase by remember { mutableStateOf(0f) }

    LaunchedEffect(isPlaying) {
        if (!isPlaying) return@LaunchedEffect
        while (true) {
            withFrameNanos { time ->
                animPhase = (time / 1_000_000_000f) * 6f
            }
        }
    }

    Canvas(
        modifier = modifier
            .width(60.dp)
            .height(24.dp)
    ) {
        val totalWidth = barCount * (barWidth.toPx() + barGap.toPx()) - barGap.toPx()
        val startX = (size.width - totalWidth) / 2f
        val maxHeight = size.height

        for (i in 0 until barCount) {
            val magnitude = if (frequencies != null && i < frequencies.size) {
                frequencies[i].coerceIn(0f, 1f)
            } else if (isPlaying) {
                val seed = sin(animPhase + i * 0.7f) * 0.5f + 0.5f
                seed.coerceIn(0.1f, 1.0f)
            } else {
                0.08f
            }

            val barHeight = (magnitude * (maxHeight - 4.dp.toPx())).coerceAtLeast(2.dp.toPx())
            val x = startX + i * (barWidth.toPx() + barGap.toPx())
            val y = maxHeight - barHeight

            drawRoundRect(
                color = color,
                topLeft = Offset(x, y),
                size = Size(barWidth.toPx(), barHeight),
                cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
            )
        }
    }
}

@Composable
fun FullscreenSpectrumVisualizer(
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    barCount: Int = 28,
    color: Color = Color.White,
    frequencies: FloatArray? = null
) {
    var animPhase by remember { mutableStateOf(0f) }

    LaunchedEffect(isPlaying) {
        if (!isPlaying) return@LaunchedEffect
        while (true) {
            withFrameNanos { time ->
                animPhase = (time / 1_000_000_000f) * 5f
            }
        }
    }

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val gap = (w / 115f).coerceIn(3.dp.toPx(), 6.dp.toPx())
        val totalGap = gap * (barCount - 1)
        val barWidth = ((w - totalGap) / barCount).coerceAtLeast(4.dp.toPx())
        val usedWidth = barCount * barWidth + totalGap
        val startX = ((w - usedWidth) / 2f).coerceAtLeast(0f)
        val minHeight = 4.dp.toPx()
        val usableHeight = (h - minHeight - 2.dp.toPx()).coerceAtLeast(10.dp.toPx())

        for (i in 0 until barCount) {
            val raw = if (frequencies != null && i < frequencies.size) {
                frequencies[i].coerceIn(0f, 1f)
            } else if (isPlaying) {
                val v1 = sin(animPhase + i * 0.45f) * 0.5f + 0.5f
                val v2 = sin(animPhase * 0.7f + i * 0.9f) * 0.5f + 0.5f
                ((v1 + v2) / 2f).coerceIn(0.05f, 1f)
            } else {
                0.04f
            }

            val compressed = Math.pow(raw.toDouble(), 1.3).toFloat() * 0.78f
            val barH = minHeight + compressed * usableHeight
            val x = startX + i * (barWidth + gap)
            val y = h - barH

            drawRoundRect(
                color = color,
                topLeft = Offset(x, y),
                size = Size(barWidth, barH),
                cornerRadius = CornerRadius((barWidth / 2f).coerceAtMost(3.dp.toPx()), (barWidth / 2f).coerceAtMost(3.dp.toPx()))
            )
        }
    }
}
