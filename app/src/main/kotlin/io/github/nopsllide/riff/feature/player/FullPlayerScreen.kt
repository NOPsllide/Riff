package io.github.nopsllide.riff.feature.player

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    onToggleFavorite: () -> Unit = {},
    onDismiss: () -> Unit,
    onOpenLyrics: () -> Unit = {},
    onOpenEqualizer: () -> Unit = {},
    onOpenTvKaraoke: () -> Unit = {},
    onOpenQueue: () -> Unit = {},
    onOpenSleepTimer: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    if (currentTrack == null) return

    val lyricsListState = rememberLazyListState()

    val syncedLines = lyrics?.syncedLines ?: emptyList()
    val activeIndex = remember(currentPositionMs, syncedLines) {
        if (syncedLines.isNotEmpty()) {
            val idx = syncedLines.indexOfLast { it.timestampMs <= currentPositionMs }
            if (idx >= 0) idx else 0
        } else {
            0
        }
    }

    LaunchedEffect(activeIndex) {
        if (syncedLines.isNotEmpty() && activeIndex in syncedLines.indices) {
            lyricsListState.animateScrollToItem(activeIndex, scrollOffset = -220)
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
            .background(Color(0xFF181818))
    ) {
        val scale = (maxWidth.value / 606f).coerceIn(0.55f, 1.15f)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height((450 * scale).dp)
                .align(Alignment.TopCenter)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            ambientColor.copy(alpha = 0.32f),
                            ambientColor.copy(alpha = 0.14f),
                            ambientColor.copy(alpha = 0.04f),
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
            Spacer(modifier = Modifier.height((16 * scale).dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = (25 * scale).dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size((165 * scale).dp, (48 * scale).dp)
                        .clip(RoundedCornerShape(100.dp))
                        .background(Color.White.copy(alpha = 0.05f)),
                    contentAlignment = Alignment.Center
                ) {
                    BasicText(
                        text = "Now playing",
                        style = TextStyle(
                            color = Color.White,
                            fontSize = (21 * scale).sp,
                            fontWeight = FontWeight.Normal,
                            fontFamily = FontFamily.SansSerif
                        )
                    )
                }

                Box(
                    modifier = Modifier
                        .size((54 * scale).dp)
                        .clip(CircleShape)
                        .background(Color(0xFF212121))
                        .clickable { onDismiss() },
                    contentAlignment = Alignment.Center
                ) {
                    LucideXIcon(
                        size = (20 * scale).dp,
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height((24 * scale).dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = (26 * scale).dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    BasicText(
                        text = currentTrack.title,
                        style = TextStyle(
                            color = Color.White,
                            fontSize = (23 * scale).sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.SansSerif
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height((4 * scale).dp))
                    BasicText(
                        text = currentTrack.artist,
                        style = TextStyle(
                            color = Color(0xFF8E8E93),
                            fontSize = (21 * scale).sp,
                            fontWeight = FontWeight.Normal,
                            fontFamily = FontFamily.SansSerif
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width((16 * scale).dp))

                Box(
                    modifier = Modifier
                        .size((64 * scale).dp)
                        .clip(RoundedCornerShape((14 * scale).dp))
                        .background(Color(0xFF212121)),
                    contentAlignment = Alignment.Center
                ) {
                    LucideHeartOutlinedIcon(
                        size = (30 * scale).dp,
                        tint = Color(0xFF8E8E93)
                    )
                }
            }

            Spacer(modifier = Modifier.height((20 * scale).dp))

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (syncedLines.isNotEmpty()) {
                    LazyColumn(
                        state = lyricsListState,
                        contentPadding = PaddingValues(
                            start = (26 * scale).dp,
                            end = (26 * scale).dp,
                            top = (20 * scale).dp,
                            bottom = (90 * scale).dp
                        ),
                        verticalArrangement = Arrangement.spacedBy((18 * scale).dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        itemsIndexed(syncedLines) { index, line ->
                            val isCurrent = index == activeIndex
                            val style = if (isCurrent) {
                                TextStyle(
                                    color = Color.White,
                                    fontSize = (34 * scale).sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.SansSerif,
                                    shadow = Shadow(
                                        color = Color.White.copy(alpha = 0.65f),
                                        blurRadius = 24f
                                    )
                                )
                            } else {
                                TextStyle(
                                    color = Color(0xFF8E8E93),
                                    fontSize = (32 * scale).sp,
                                    fontWeight = FontWeight.Normal,
                                    fontFamily = FontFamily.SansSerif
                                )
                            }

                            BasicText(
                                text = line.text,
                                style = style,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSeekTo(line.timestampMs) }
                            )
                        }
                    }
                } else if (!lyrics?.plainText.isNullOrBlank()) {
                    val plainLines = lyrics?.plainText?.lines() ?: emptyList()
                    LazyColumn(
                        contentPadding = PaddingValues(
                            start = (26 * scale).dp,
                            end = (26 * scale).dp,
                            top = (20 * scale).dp,
                            bottom = (90 * scale).dp
                        ),
                        verticalArrangement = Arrangement.spacedBy((14 * scale).dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        itemsIndexed(plainLines) { _, lineText ->
                            BasicText(
                                text = lineText,
                                style = TextStyle(
                                    color = Color(0xFF8E8E93),
                                    fontSize = (28 * scale).sp,
                                    fontWeight = FontWeight.Normal,
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
                                fontSize = (22 * scale).sp,
                                fontWeight = FontWeight.Normal,
                                fontFamily = FontFamily.SansSerif,
                                textAlign = TextAlign.Center
                            )
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height((110 * scale).dp)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0x29181818),
                                    Color(0xFF181818).copy(alpha = 0.85f),
                                    Color(0xFF181818)
                                )
                            )
                        )
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = (26 * scale).dp)
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
                    inactiveColor = Color.White.copy(alpha = 0.18f),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BasicText(
                        text = formatDuration(displayPosition),
                        style = TextStyle(
                            color = Color.White,
                            fontSize = (16 * scale).sp,
                            fontWeight = FontWeight.Normal,
                            fontFamily = FontFamily.SansSerif
                        )
                    )

                    BasicText(
                        text = formatDuration(durationMs),
                        style = TextStyle(
                            color = Color.White,
                            fontSize = (16 * scale).sp,
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
                    .padding(horizontal = (26 * scale).dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size((48 * scale).dp),
                    contentAlignment = Alignment.Center
                ) {
                    LucideShuffleIcon(
                        size = (26 * scale).dp,
                        tint = Color.White
                    )
                }

                Box(
                    modifier = Modifier.size((48 * scale).dp),
                    contentAlignment = Alignment.Center
                ) {
                    LucideSkipBackIcon(
                        size = (26 * scale).dp,
                        tint = Color.White
                    )
                }

                Box(
                    modifier = Modifier
                        .size((88 * scale).dp, (88 * scale).dp)
                        .clip(RoundedCornerShape(100.dp))
                        .background(Color.White)
                        .clickable { onPlayPauseClick() },
                    contentAlignment = Alignment.Center
                ) {
                    if (isPlaying) {
                        LucidePauseIcon(
                            size = (26 * scale).dp,
                            tint = Color.Black
                        )
                    } else {
                        FigmaPlayActionIcon(
                            size = (26 * scale).dp,
                            tint = Color.Black
                        )
                    }
                }

                Box(
                    modifier = Modifier.size((48 * scale).dp),
                    contentAlignment = Alignment.Center
                ) {
                    LucideSkipForwardIcon(
                        size = (26 * scale).dp,
                        tint = Color.White
                    )
                }

                Box(
                    modifier = Modifier.size((48 * scale).dp),
                    contentAlignment = Alignment.Center
                ) {
                    LucideRepeatIcon(
                        size = (26 * scale).dp,
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height((24 * scale).dp))
        }
    }
}

private fun formatDuration(ms: Long): String {
    val totalSec = (ms / 1000).coerceAtLeast(0)
    val min = totalSec / 60
    val sec = totalSec % 60
    return String.format("%d:%02d", min, sec)
}
