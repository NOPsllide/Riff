package io.github.nopsllide.riff.feature.player

import android.graphics.drawable.BitmapDrawable
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.palette.graphics.Palette
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.request.SuccessResult
import io.github.nopsllide.riff.core.designsystem.component.FigmaPlayActionIcon
import io.github.nopsllide.riff.core.designsystem.component.LucidePauseIcon
import io.github.nopsllide.riff.core.designsystem.component.LucideQueueIcon
import io.github.nopsllide.riff.core.designsystem.component.LucideRepeatIcon
import io.github.nopsllide.riff.core.designsystem.component.LucideShuffleIcon
import io.github.nopsllide.riff.core.designsystem.component.LucideSkipBackIcon
import io.github.nopsllide.riff.core.designsystem.component.LucideSkipForwardIcon
import io.github.nopsllide.riff.core.designsystem.component.LucideXIcon
import io.github.nopsllide.riff.core.designsystem.component.SquigglySlider
import io.github.nopsllide.riff.core.model.Lyrics
import io.github.nopsllide.riff.core.model.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun FullPlayerScreen(
    currentTrack: Track?,
    isPlaying: Boolean,
    isBuffering: Boolean,
    currentPositionMs: Long,
    durationMs: Long,
    isShuffle: Boolean = false,
    isRepeat: Boolean = false,
    isBassPulseEnabled: Boolean = false,
    lyrics: Lyrics? = null,
    isLyricsLoading: Boolean = false,
    dominantColor: Color? = null,
    onPlayPauseClick: () -> Unit,
    onNextClick: () -> Unit = {},
    onPrevClick: () -> Unit = {},
    onSeekTo: (Long) -> Unit,
    onToggleShuffle: () -> Unit = {},
    onToggleRepeat: () -> Unit = {},
    onQueueClick: () -> Unit = {},
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var extractedDominantColor by remember { mutableStateOf<Color?>(null) }

    LaunchedEffect(currentTrack?.thumbnail) {
        val thumb = currentTrack?.thumbnail
        if (!thumb.isNullOrBlank()) {
            withContext(Dispatchers.IO) {
                try {
                    val loader = ImageLoader(context)
                    val req = ImageRequest.Builder(context)
                        .data(thumb)
                        .allowHardware(false)
                        .size(128, 128)
                        .build()
                    val result = (loader.execute(req) as? SuccessResult)?.drawable
                    val bitmap = (result as? BitmapDrawable)?.bitmap
                    if (bitmap != null) {
                        val palette = Palette.from(bitmap).generate()
                        val swatch = palette.dominantSwatch
                            ?: palette.vibrantSwatch
                            ?: palette.mutedSwatch
                        if (swatch != null) {
                            extractedDominantColor = Color(swatch.rgb)
                        }
                    }
                } catch (t: Throwable) {
                }
            }
        } else {
            extractedDominantColor = null
        }
    }

    val activeAmbientColor by animateColorAsState(
        targetValue = extractedDominantColor ?: dominantColor ?: Color(0xFF222428),
        animationSpec = tween(durationMillis = 600),
        label = "ambientColor"
    )

    val lyricsListState = rememberLazyListState()
    var showLyrics by remember { mutableStateOf(false) }

    val syncedLines = lyrics?.syncedLines ?: emptyList()
    val activeIndex = remember(currentPositionMs, syncedLines) {
        if (syncedLines.isNotEmpty()) {
            val adjustedPos = currentPositionMs + 120L
            val idx = syncedLines.indexOfLast { it.timestampMs <= adjustedPos }
            if (idx >= 0) idx else -1
        } else {
            -1
        }
    }

    val density = LocalDensity.current
    LaunchedEffect(activeIndex, showLyrics) {
        if (showLyrics && syncedLines.isNotEmpty() && activeIndex in syncedLines.indices) {
            try {
                val centerOffset = with(density) { -(130).dp.toPx().toInt() }
                lyricsListState.animateScrollToItem(activeIndex, scrollOffset = centerOffset)
            } catch (e: Exception) {
            }
        }
    }

    var isDraggingSlider by remember { mutableStateOf(false) }
    var scrubbedPositionMs by remember { mutableFloatStateOf(0f) }

    val effectiveFraction = if (isDraggingSlider) {
        if (durationMs > 0) (scrubbedPositionMs / durationMs.toFloat()).coerceIn(0f, 1f) else 0f
    } else {
        if (durationMs > 0) (currentPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f) else 0f
    }

    val displayPosition = if (isDraggingSlider) scrubbedPositionMs.toLong() else currentPositionMs
    val interactionSource = remember { MutableInteractionSource() }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {}
            )
            .background(Color(0xFF0C0C0E))
    ) {
        val scale = (maxWidth.value / 412f).coerceIn(0.75f, 1.25f)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            activeAmbientColor.copy(alpha = 0.50f),
                            activeAmbientColor.copy(alpha = 0.20f),
                            Color(0xFF0F0F12),
                            Color(0xFF08080A)
                        )
                    )
                )
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height((440 * scale).dp)
                .align(Alignment.TopCenter)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            activeAmbientColor.copy(alpha = 0.40f),
                            activeAmbientColor.copy(alpha = 0.12f),
                            Color.Transparent
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            Spacer(modifier = Modifier.height((12 * scale).dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = (20 * scale).dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .size((44 * scale).dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.08f))
                        .clickable { onDismiss() },
                    contentAlignment = Alignment.Center
                ) {
                    LucideXIcon(
                        size = (18 * scale).dp,
                        tint = Color.White
                    )
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .clip(RoundedCornerShape(100.dp))
                        .background(Color.White.copy(alpha = 0.08f))
                        .padding(horizontal = (16 * scale).dp, vertical = (8 * scale).dp),
                    contentAlignment = Alignment.Center
                ) {
                    BasicText(
                        text = "Şimdi Çalıyor",
                        style = TextStyle(
                            color = Color.White,
                            fontSize = (15 * scale).sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = FontFamily.SansSerif
                        )
                    )
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .clip(RoundedCornerShape(100.dp))
                        .background(if (showLyrics) Color.White else Color.White.copy(alpha = 0.10f))
                        .clickable { showLyrics = !showLyrics }
                        .padding(horizontal = (16 * scale).dp, vertical = (8 * scale).dp),
                    contentAlignment = Alignment.Center
                ) {
                    BasicText(
                        text = "Sözler",
                        style = TextStyle(
                            color = if (showLyrics) Color.Black else Color.White,
                            fontSize = (13 * scale).sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.SansSerif
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height((16 * scale).dp))

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = (20 * scale).dp),
                contentAlignment = Alignment.Center
            ) {
                val artworkScale by animateFloatAsState(
                    targetValue = if (isPlaying) 1f else 0.86f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    ),
                    label = "artworkScale"
                )
                val coverAlpha by animateFloatAsState(
                    targetValue = if (!showLyrics) 1f else 0f,
                    animationSpec = tween(durationMillis = 280),
                    label = "coverAlpha"
                )
                val lyricsAlpha by animateFloatAsState(
                    targetValue = if (showLyrics) 1f else 0f,
                    animationSpec = tween(durationMillis = 280),
                    label = "lyricsAlpha"
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .aspectRatio(1f)
                        .align(Alignment.Center)
                        .graphicsLayer {
                            scaleX = artworkScale
                            scaleY = artworkScale
                            alpha = coverAlpha
                        }
                        .shadow(
                            elevation = (28 * scale * artworkScale).dp,
                            shape = RoundedCornerShape((24 * scale).dp),
                            ambientColor = activeAmbientColor.copy(alpha = 0.55f),
                            spotColor = activeAmbientColor.copy(alpha = 0.65f)
                        )
                        .clip(RoundedCornerShape((24 * scale).dp))
                        .background(Color(0xFF1E1E1E))
                        .pointerInput(showLyrics) {
                            if (!showLyrics) {
                                awaitEachGesture {
                                    val down = awaitFirstDown(requireUnconsumed = false)
                                    var totalDragX = 0f
                                    var isDrag = false
                                    while (true) {
                                        val event = awaitPointerEvent()
                                        val change = event.changes.firstOrNull { it.id == down.id } ?: break
                                        if (change.isConsumed) break
                                        val dragX = change.position.x - down.position.x
                                        if (kotlin.math.abs(dragX) > 40f) {
                                            isDrag = true
                                            totalDragX = dragX
                                        }
                                        if (!change.pressed) {
                                            change.consume()
                                            if (isDrag) {
                                                if (totalDragX < -60f) {
                                                    onNextClick()
                                                } else if (totalDragX > 60f) {
                                                    onPrevClick()
                                                }
                                            } else {
                                                showLyrics = true
                                            }
                                            break
                                        }
                                    }
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (!currentTrack?.thumbnail.isNullOrBlank()) {
                        AsyncImage(
                            model = currentTrack?.thumbnail,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                if (showLyrics || lyricsAlpha > 0.01f) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .align(Alignment.Center)
                            .graphicsLayer {
                                alpha = lyricsAlpha
                            }
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                showLyrics = false
                            }
                    ) {
                        if (syncedLines.isNotEmpty()) {
                            LazyColumn(
                                state = lyricsListState,
                                contentPadding = PaddingValues(
                                    top = (50 * scale).dp,
                                    bottom = (100 * scale).dp
                                ),
                                verticalArrangement = Arrangement.spacedBy((18 * scale).dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                itemsIndexed(syncedLines) { index, line ->
                                    val isCurrent = index == activeIndex && activeIndex >= 0
                                    val lineAlpha by animateFloatAsState(
                                        targetValue = if (isCurrent) 1f else 0.32f,
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioNoBouncy,
                                            stiffness = Spring.StiffnessMediumLow
                                        ),
                                        label = "lineAlpha"
                                    )
                                    val lineScale by animateFloatAsState(
                                        targetValue = if (isCurrent) 1.05f else 0.96f,
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioMediumBouncy,
                                            stiffness = Spring.StiffnessMediumLow
                                        ),
                                        label = "lineScale"
                                    )

                                    BasicText(
                                        text = line.text,
                                        style = TextStyle(
                                            color = Color.White,
                                            fontSize = (22 * scale).sp,
                                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.SemiBold,
                                            fontFamily = FontFamily.SansSerif
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .graphicsLayer {
                                                alpha = lineAlpha
                                                scaleX = lineScale
                                                scaleY = lineScale
                                                transformOrigin = TransformOrigin(0f, 0.5f)
                                            }
                                            .clickable(
                                                interactionSource = remember { MutableInteractionSource() },
                                                indication = null
                                            ) {
                                                onSeekTo(line.timestampMs)
                                            }
                                            .padding(vertical = (4 * scale).dp)
                                    )
                                }
                            }
                        } else if (!lyrics?.plainText.isNullOrBlank()) {
                            val plainLines = lyrics?.plainText?.lines() ?: emptyList()
                            LazyColumn(
                                contentPadding = PaddingValues(
                                    top = (30 * scale).dp,
                                    bottom = (60 * scale).dp
                                ),
                                verticalArrangement = Arrangement.spacedBy((14 * scale).dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                itemsIndexed(plainLines) { _, lineText ->
                                    BasicText(
                                        text = lineText,
                                        style = TextStyle(
                                            color = Color.White.copy(alpha = 0.70f),
                                            fontSize = (17 * scale).sp,
                                            fontWeight = FontWeight.Medium,
                                            fontFamily = FontFamily.SansSerif
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        } else {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                BasicText(
                                    text = if (isLyricsLoading) "Şarkı sözleri yükleniyor..." else "Şarkı sözleri bulunamadı",
                                    style = TextStyle(
                                        color = Color(0xFF8E8E93),
                                        fontSize = (17 * scale).sp,
                                        fontWeight = FontWeight.SemiBold,
                                        fontFamily = FontFamily.SansSerif,
                                        textAlign = TextAlign.Center
                                    )
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .fillMaxWidth()
                                .height((48 * scale).dp)
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color(0xFF0C0C0E),
                                            Color.Transparent
                                        )
                                    )
                                )
                        )

                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .height((60 * scale).dp)
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            Color(0xFF0C0C0E)
                                        )
                                    )
                                )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height((16 * scale).dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = (24 * scale).dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    BasicText(
                        text = currentTrack?.title ?: "Şarkı seçilmedi",
                        style = TextStyle(
                            color = Color.White,
                            fontSize = (22 * scale).sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.SansSerif
                        ),
                        maxLines = 1,
                        modifier = Modifier.basicMarquee()
                    )
                    Spacer(modifier = Modifier.height((3 * scale).dp))
                    BasicText(
                        text = currentTrack?.artist ?: "Riff",
                        style = TextStyle(
                            color = Color(0xFF8E8E93),
                            fontSize = (15 * scale).sp,
                            fontWeight = FontWeight.Normal,
                            fontFamily = FontFamily.SansSerif
                        ),
                        maxLines = 1,
                        modifier = Modifier.basicMarquee()
                    )
                }

                Spacer(modifier = Modifier.width((12 * scale).dp))

                Box(
                    modifier = Modifier
                        .size((42 * scale).dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.08f))
                        .clickable { onQueueClick() },
                    contentAlignment = Alignment.Center
                ) {
                    LucideQueueIcon(
                        size = (20 * scale).dp,
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height((14 * scale).dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = (20 * scale).dp)
            ) {
                SquigglySlider(
                    value = effectiveFraction,
                    onValueChange = { frac ->
                        isDraggingSlider = true
                        scrubbedPositionMs = frac * durationMs.toFloat()
                    },
                    onValueChangeFinished = {
                        isDraggingSlider = false
                        onSeekTo(scrubbedPositionMs.toLong())
                    },
                    isPlaying = isPlaying,
                    activeColor = Color.White,
                    inactiveColor = Color.White.copy(alpha = 0.20f),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height((4 * scale).dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BasicText(
                        text = formatDuration(displayPosition),
                        style = TextStyle(
                            color = Color(0xFF8E8E93),
                            fontSize = (13 * scale).sp,
                            fontWeight = FontWeight.Normal,
                            fontFamily = FontFamily.SansSerif
                        )
                    )

                    BasicText(
                        text = formatDuration(durationMs),
                        style = TextStyle(
                            color = Color(0xFF8E8E93),
                            fontSize = (13 * scale).sp,
                            fontWeight = FontWeight.Normal,
                            fontFamily = FontFamily.SansSerif
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height((16 * scale).dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = (24 * scale).dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size((44 * scale).dp)
                        .clickable { onToggleShuffle() },
                    contentAlignment = Alignment.Center
                ) {
                    LucideShuffleIcon(
                        size = (22 * scale).dp,
                        tint = if (isShuffle) Color.White else Color(0xFF8E8E93)
                    )
                }

                Box(
                    modifier = Modifier
                        .size((48 * scale).dp)
                        .clickable { onPrevClick() },
                    contentAlignment = Alignment.Center
                ) {
                    LucideSkipBackIcon(
                        size = (28 * scale).dp,
                        tint = Color.White
                    )
                }

                Box(
                    modifier = Modifier
                        .size((72 * scale).dp)
                        .clip(RoundedCornerShape(100.dp))
                        .background(Color.White)
                        .clickable { onPlayPauseClick() },
                    contentAlignment = Alignment.Center
                ) {
                    if (isBuffering) {
                        M3ExpressiveLoadingIndicator(
                            modifier = Modifier.size((28 * scale).dp),
                            color = Color.Black,
                            strokeWidth = (2.8f * scale).dp
                        )
                    } else if (isPlaying) {
                        LucidePauseIcon(
                            size = (24 * scale).dp,
                            tint = Color.Black
                        )
                    } else {
                        FigmaPlayActionIcon(
                            size = (24 * scale).dp,
                            tint = Color.Black
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size((48 * scale).dp)
                        .clickable { onNextClick() },
                    contentAlignment = Alignment.Center
                ) {
                    LucideSkipForwardIcon(
                        size = (28 * scale).dp,
                        tint = Color.White
                    )
                }

                Box(
                    modifier = Modifier
                        .size((44 * scale).dp)
                        .clickable { onToggleRepeat() },
                    contentAlignment = Alignment.Center
                ) {
                    LucideRepeatIcon(
                        size = (22 * scale).dp,
                        tint = if (isRepeat) Color.White else Color(0xFF8E8E93)
                    )
                }
            }

            Spacer(modifier = Modifier.height((40 * scale).dp))
            Spacer(modifier = Modifier.navigationBarsPadding())
        }
    }
}

private fun formatDuration(ms: Long): String {
    val totalSec = (ms / 1000).coerceAtLeast(0)
    val min = totalSec / 60
    val sec = totalSec % 60
    return String.format("%d:%02d", min, sec)
}

@Composable
fun M3ExpressiveLoadingIndicator(
    modifier: Modifier = Modifier,
    color: Color = Color.Black,
    strokeWidth: Dp = 2.8f.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "m3Loading")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "m3Rotation"
    )
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2.0 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "m3Phase"
    )
    val amplitudeScale by infiniteTransition.animateFloat(
        initialValue = 0.75f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(750, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "m3Amp"
    )

    Canvas(modifier = modifier.graphicsLayer { rotationZ = rotation }) {
        val strokePx = strokeWidth.toPx()
        val maxAmp = 2.4f.dp.toPx() * amplitudeScale
        val baseRadius = (minOf(size.width, size.height) - strokePx) / 2f - maxAmp
        val cx = size.width / 2f
        val cy = size.height / 2f

        val path = Path()
        val steps = 120
        for (i in 0..steps) {
            val theta = (i.toFloat() / steps) * (2f * Math.PI.toFloat())
            val r = baseRadius + maxAmp * kotlin.math.cos(5f * theta + phase)
            val x = cx + r * kotlin.math.cos(theta)
            val y = cy + r * kotlin.math.sin(theta)
            if (i == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }
        path.close()

        drawPath(
            path = path,
            color = color,
            style = Stroke(
                width = strokePx,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
    }
}
