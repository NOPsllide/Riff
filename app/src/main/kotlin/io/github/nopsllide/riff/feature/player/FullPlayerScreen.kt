package io.github.nopsllide.riff.feature.player

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import io.github.nopsllide.riff.core.designsystem.component.FigmaPlayActionIcon
import io.github.nopsllide.riff.core.designsystem.component.LucideHeartOutlinedIcon
import io.github.nopsllide.riff.core.designsystem.component.LucidePauseIcon
import io.github.nopsllide.riff.core.designsystem.component.LucideRepeatIcon
import io.github.nopsllide.riff.core.designsystem.component.LucideShuffleIcon
import io.github.nopsllide.riff.core.designsystem.component.LucideSkipBackIcon
import io.github.nopsllide.riff.core.designsystem.component.LucideSkipForwardIcon
import io.github.nopsllide.riff.core.designsystem.component.LucideXIcon
import io.github.nopsllide.riff.core.designsystem.component.SquigglySlider
import io.github.nopsllide.riff.core.model.Lyrics
import io.github.nopsllide.riff.core.model.Track

@Composable
fun FullPlayerScreen(
    currentTrack: Track?,
    isPlaying: Boolean,
    isBuffering: Boolean,
    currentPositionMs: Long,
    durationMs: Long,
    isShuffle: Boolean = false,
    isRepeat: Boolean = false,
    isWavyEnabled: Boolean = true,
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
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val lyricsListState = rememberLazyListState()
    var showLyrics by remember { mutableStateOf(false) }

    val syncedLines = lyrics?.syncedLines ?: emptyList()
    val activeIndex = remember(currentPositionMs, syncedLines) {
        if (syncedLines.isNotEmpty()) {
            val idx = syncedLines.indexOfLast { it.timestampMs <= currentPositionMs }
            if (idx >= 0) idx else 0
        } else {
            0
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
    val ambientColor = dominantColor ?: Color(0xFF323232)

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF141414))
    ) {
        val scale = (maxWidth.value / 412f).coerceIn(0.75f, 1.25f)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height((420 * scale).dp)
                .align(Alignment.TopCenter)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            ambientColor.copy(alpha = 0.35f),
                            ambientColor.copy(alpha = 0.15f),
                            ambientColor.copy(alpha = 0.03f),
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

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = (20 * scale).dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
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
                        .size((44 * scale).dp)
                        .clip(CircleShape)
                        .background(Color(0xFF222222))
                        .clickable { onDismiss() },
                    contentAlignment = Alignment.Center
                ) {
                    LucideXIcon(
                        size = (18 * scale).dp,
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height((16 * scale).dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = (20 * scale).dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!currentTrack?.thumbnail.isNullOrBlank()) {
                    Box(
                        modifier = Modifier
                            .size((52 * scale).dp)
                            .clip(RoundedCornerShape((12 * scale).dp))
                            .background(Color(0xFF242424))
                    ) {
                        AsyncImage(
                            model = currentTrack?.thumbnail,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Spacer(modifier = Modifier.width((14 * scale).dp))
                }

                Column(modifier = Modifier.weight(1f)) {
                    BasicText(
                        text = currentTrack?.title ?: "Şarkı seçilmedi",
                        style = TextStyle(
                            color = Color.White,
                            fontSize = (19 * scale).sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.SansSerif
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height((2 * scale).dp))
                    BasicText(
                        text = currentTrack?.artist ?: "Riff",
                        style = TextStyle(
                            color = Color(0xFF8E8E93),
                            fontSize = (15 * scale).sp,
                            fontWeight = FontWeight.Normal,
                            fontFamily = FontFamily.SansSerif
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width((12 * scale).dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(100.dp))
                        .background(if (showLyrics) Color.White else Color.White.copy(alpha = 0.10f))
                        .clickable { showLyrics = !showLyrics }
                        .padding(horizontal = (14 * scale).dp, vertical = (7 * scale).dp),
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

                Spacer(modifier = Modifier.width((10 * scale).dp))

                LucideHeartOutlinedIcon(
                    size = (28 * scale).dp,
                    tint = Color(0xFF8E8E93)
                )
            }

            Spacer(modifier = Modifier.height((16 * scale).dp))

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = (20 * scale).dp),
                contentAlignment = Alignment.Center
            ) {
                Crossfade(
                    targetState = showLyrics,
                    animationSpec = tween(250),
                    label = "player_center_view"
                ) { isLyricsMode ->
                    if (!isLyricsMode) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.92f)
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape((24 * scale).dp))
                                .background(Color(0xFF222222))
                                .clickable { showLyrics = true },
                            contentAlignment = Alignment.Center
                        ) {
                            if (!currentTrack?.thumbnail.isNullOrBlank()) {
                                AsyncImage(
                                    model = currentTrack.thumbnail,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    } else {
                        Box(modifier = Modifier.fillMaxSize()) {
                            if (syncedLines.isNotEmpty()) {
                                LazyColumn(
                                    state = lyricsListState,
                                    contentPadding = PaddingValues(
                                        top = (40 * scale).dp,
                                        bottom = (80 * scale).dp
                                    ),
                                    verticalArrangement = Arrangement.spacedBy((18 * scale).dp),
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    itemsIndexed(syncedLines) { index, line ->
                                        val isCurrent = index == activeIndex
                                        val style = if (isCurrent) {
                                            TextStyle(
                                                color = Color.White,
                                                fontSize = (22 * scale).sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.SansSerif
                                            )
                                        } else {
                                            TextStyle(
                                                color = Color.White.copy(alpha = 0.35f),
                                                fontSize = (18 * scale).sp,
                                                fontWeight = FontWeight.SemiBold,
                                                fontFamily = FontFamily.SansSerif
                                            )
                                        }

                                        BasicText(
                                            text = line.text,
                                            style = style,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { onSeekTo(line.timestampMs) }
                                                .padding(vertical = (2 * scale).dp)
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
                                    .height((36 * scale).dp)
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                Color(0xFF141414),
                                                Color.Transparent
                                            )
                                        )
                                    )
                            )

                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .fillMaxWidth()
                                    .height((50 * scale).dp)
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                Color.Transparent,
                                                Color(0xFF141414)
                                            )
                                        )
                                    )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height((12 * scale).dp))

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
                    if (isPlaying) {
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

            Spacer(modifier = Modifier.height((18 * scale).dp))
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
