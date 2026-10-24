package io.github.nopsllide.riff.core.designsystem.component

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

@Composable
fun SquigglySlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: () -> Unit,
    modifier: Modifier = Modifier,
    isPlaying: Boolean = false,
    activeColor: Color = Color.White,
    inactiveColor: Color = Color.White.copy(alpha = 0.30f),
    isWavy: Boolean = true
) {
    var isDragging by remember { mutableStateOf(false) }
    var dragProgress by remember { mutableFloatStateOf(value) }
    var phaseOffset by remember { mutableFloatStateOf(0f) }

    val waveHeightFraction by animateFloatAsState(
        targetValue = if (isPlaying && isWavy) 1f else 0f,
        animationSpec = tween(
            durationMillis = if (isPlaying && isWavy) 800 else 550,
            easing = if (isPlaying && isWavy) FastOutSlowInEasing else LinearOutSlowInEasing
        ),
        label = "waveHeightFraction"
    )

    val thumbAnim by animateFloatAsState(
        targetValue = if (isDragging) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "thumbAnim"
    )

    LaunchedEffect(isPlaying) {
        var lastTime = 0L
        while (true) {
            withFrameNanos { time ->
                if (lastTime != 0L && isPlaying) {
                    val dt = ((time - lastTime) / 1_000_000_000f).coerceIn(0.001f, 0.05f)
                    phaseOffset += 24f * dt
                }
                lastTime = time
            }
        }
    }

    val displayProgress = if (isDragging) dragProgress else value.coerceIn(0f, 1f)
    val path = remember { Path() }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = { offset ->
                        val w = size.width
                        if (w > 0) {
                            val padLeft = 8.dp.toPx()
                            val padRight = 8.dp.toPx()
                            val trackW = (w - padLeft - padRight).coerceAtLeast(1f)
                            val target = ((offset.x - padLeft) / trackW).coerceIn(0f, 1f)
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
                            val padLeft = 8.dp.toPx()
                            val padRight = 8.dp.toPx()
                            val trackW = (w - padLeft - padRight).coerceAtLeast(1f)
                            dragProgress = ((offset.x - padLeft) / trackW).coerceIn(0f, 1f)
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
                            val padLeft = 8.dp.toPx()
                            val padRight = 8.dp.toPx()
                            val trackW = (w - padLeft - padRight).coerceAtLeast(1f)
                            dragProgress = ((change.position.x - padLeft) / trackW).coerceIn(0f, 1f)
                            val deltaX = change.position.x - change.previousPosition.x
                            phaseOffset += deltaX * 0.4f
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
            val trackWidth = (width - padLeft - padRight).coerceAtLeast(1f)

            val strokeWidthPx = 4.dp.toPx()
            val waveLengthPx = 22.dp.toPx()
            val amplitudePx = 3.5f.dp.toPx()
            val progressPx = (padLeft + displayProgress * trackWidth).coerceIn(padLeft, padLeft + trackWidth)

            val transitionLen = 1.5f * waveLengthPx
            val computeAmplitude: (Float, Float) -> Float = { x, sign ->
                val coeff = ((progressPx + transitionLen / 2f - x) / transitionLen).coerceIn(0f, 1f)
                sign * waveHeightFraction * amplitudePx * coeff
            }

            val currentPhase = (phaseOffset * waveLengthPx / 20f) % waveLengthPx
            val waveStart = padLeft - currentPhase - waveLengthPx
            val waveEnd = width - padRight + waveLengthPx
            val dist = waveLengthPx / 2f

            path.rewind()
            path.moveTo(waveStart, centerY)

            var currentX = waveStart
            var waveSign = 1f
            var currentAmp = computeAmplitude(currentX, waveSign)

            while (currentX < waveEnd) {
                waveSign = -waveSign
                val nextX = currentX + dist
                val midX = currentX + dist / 2f
                val nextAmp = computeAmplitude(nextX, waveSign)

                path.cubicTo(
                    midX, centerY + currentAmp,
                    midX, centerY + nextAmp,
                    nextX, centerY + nextAmp
                )

                currentAmp = nextAmp
                currentX = nextX
            }

            val clipTop = amplitudePx + strokeWidthPx + 4.dp.toPx()

            clipRect(
                left = padLeft,
                top = centerY - clipTop,
                right = progressPx,
                bottom = centerY + clipTop
            ) {
                drawPath(
                    path = path,
                    color = activeColor,
                    style = Stroke(
                        width = strokeWidthPx,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
            }

            clipRect(
                left = progressPx,
                top = centerY - clipTop,
                right = width - padRight,
                bottom = centerY + clipTop
            ) {
                drawPath(
                    path = path,
                    color = inactiveColor,
                    style = Stroke(
                        width = strokeWidthPx,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
            }

            val thumbWidth = (4f + 3f * thumbAnim).dp.toPx()
            val thumbHeight = (20f + 8f * thumbAnim).dp.toPx()
            val thumbCorner = thumbWidth / 2f

            drawRoundRect(
                color = activeColor,
                topLeft = Offset(progressPx - thumbWidth / 2f, centerY - thumbHeight / 2f),
                size = Size(thumbWidth, thumbHeight),
                cornerRadius = CornerRadius(thumbCorner, thumbCorner)
            )
        }
    }
}
