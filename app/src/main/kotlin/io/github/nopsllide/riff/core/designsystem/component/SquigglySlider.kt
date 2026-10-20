package io.github.nopsllide.riff.core.designsystem.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.sin

@Composable
fun SquigglySlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: () -> Unit,
    modifier: Modifier = Modifier,
    isPlaying: Boolean = false,
    activeColor: Color = MaterialTheme.colorScheme.primary,
    inactiveColor: Color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
    isWavy: Boolean = true
) {
    var isDragging by remember { mutableStateOf(false) }
    var dragProgress by remember { mutableFloatStateOf(value) }
    var scrubPhase by remember { mutableFloatStateOf(0f) }

    val knobAnim by animateFloatAsState(
        targetValue = if (isDragging) 1f else 0f,
        animationSpec = tween(durationMillis = 150),
        label = "knobAnim"
    )

    LaunchedEffect(isPlaying) {
        var lastTime = 0L
        while (true) {
            withFrameNanos { time ->
                if (lastTime != 0L && isPlaying) {
                    val dt = ((time - lastTime) / 1_000_000_000f).coerceIn(0.001f, 0.1f)
                    scrubPhase += 3.5f * dt
                }
                lastTime = time
            }
        }
    }

    val displayProgress = if (isDragging) dragProgress else value.coerceIn(0f, 1f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = { offset ->
                        val w = size.width
                        if (w > 0) {
                            val target = (offset.x / w).coerceIn(0f, 1f)
                            onValueChange(target)
                            onValueChangeFinished()
                        }
                    }
                )
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        isDragging = true
                        val w = size.width
                        if (w > 0) {
                            dragProgress = (offset.x / w).coerceIn(0f, 1f)
                            onValueChange(dragProgress)
                        }
                    },
                    onDragEnd = {
                        isDragging = false
                        onValueChangeFinished()
                    },
                    onDragCancel = {
                        isDragging = false
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        val w = size.width
                        if (w > 0) {
                            dragProgress = (change.position.x / w).coerceIn(0f, 1f)
                            scrubPhase += (change.position.x - change.previousPosition.x) * ((2f * PI.toFloat()) / 42f)
                            onValueChange(dragProgress)
                        }
                    }
                )
            }
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val width = size.width
            val height = size.height
            val centerY = height / 2f

            val padLeft = 8.dp.toPx()
            val padRight = 8.dp.toPx()
            val trackWidth = width - padLeft - padRight

            val knobW = (7.5f + (9.0f - 7.5f) * knobAnim).dp.toPx()
            val knobH = (24f + (32f - 24f) * knobAnim).dp.toPx()
            val knobR = (3.75f + (4.5f - 3.75f) * knobAnim).dp.toPx()

            val knobCenterX = padLeft + displayProgress * trackWidth
            val knobLeft = knobCenterX - knobW / 2f
            val knobRight = knobCenterX + knobW / 2f

            val waveStart = padLeft
            val waveEnd = (knobLeft - 4.dp.toPx()).coerceAtLeast(waveStart)

            val inactiveStart = (knobRight + 4.dp.toPx()).coerceAtMost(width - padRight)
            val inactiveEnd = width - padRight

            if (inactiveEnd > inactiveStart) {
                drawLine(
                    color = inactiveColor,
                    start = Offset(inactiveStart, centerY),
                    end = Offset(inactiveEnd, centerY),
                    strokeWidth = 10.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }

            if (waveEnd > waveStart) {
                val wavePath = Path()
                if (!isWavy) {
                    wavePath.moveTo(waveStart, centerY)
                    wavePath.lineTo(waveEnd, centerY)
                } else {
                    val k = (2f * PI.toFloat()) / 42f
                    val step = 2f
                    var x = waveStart
                    wavePath.moveTo(waveStart, centerY)
                    while (x <= waveEnd) {
                        val angle = scrubPhase - (waveEnd - x) * k
                        val y = centerY + sin(angle) * 5.dp.toPx()
                        wavePath.lineTo(x, y)
                        x += step
                    }
                    val finalY = centerY + sin(scrubPhase) * 5.dp.toPx()
                    wavePath.lineTo(waveEnd, finalY)
                }

                drawPath(
                    path = wavePath,
                    color = activeColor,
                    style = Stroke(
                        width = 8.5f.dp.toPx(),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
            }

            drawRoundRect(
                color = activeColor,
                topLeft = Offset(knobLeft, centerY - knobH / 2f),
                size = Size(knobW, knobH),
                cornerRadius = CornerRadius(knobR, knobR)
            )
        }
    }
}
