package io.github.rootscripts.riff.feature.lyrics

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.rootscripts.riff.core.model.LyricLine
import io.github.rootscripts.riff.core.model.Lyrics

@Composable
fun LyricsScreen(
    lyrics: Lyrics?,
    isLoading: Boolean,
    currentPositionMs: Long,
    onSeekTo: (Long) -> Unit,
    onDismiss: () -> Unit,
    trackTitle: String,
    trackArtist: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = trackTitle,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                    Text(
                        text = trackArtist,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else if (lyrics == null || (!lyrics.isSynced && lyrics.plainText.isBlank())) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No lyrics found",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else if (lyrics.isSynced) {
                SyncedLyricsView(
                    lines = lyrics.syncedLines,
                    currentPositionMs = currentPositionMs,
                    onLineClick = onSeekTo,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = 24.dp)
                ) {
                    Text(
                        text = lyrics.plainText,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            lineHeight = 28.sp,
                            fontSize = 18.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
fun SyncedLyricsView(
    lines: List<LyricLine>,
    currentPositionMs: Long,
    onLineClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    val activeIndex = lines.indexOfLast { it.timestampMs <= currentPositionMs }
        .coerceAtLeast(0)

    LaunchedEffect(activeIndex) {
        if (activeIndex in lines.indices) {
            listState.animateScrollToItem(
                index = (activeIndex - 2).coerceAtLeast(0)
            )
        }
    }

    LazyColumn(
        state = listState,
        modifier = modifier,
        contentPadding = PaddingValues(vertical = 120.dp)
    ) {
        itemsIndexed(lines) { index, line ->
            val isActive = index == activeIndex
            val textColor by animateColorAsState(
                targetValue = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
                label = "lyricColor"
            )

            Text(
                text = line.text,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontSize = if (isActive) 24.sp else 20.sp,
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                    lineHeight = 34.sp
                ),
                color = textColor,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onLineClick(line.timestampMs) }
                    .padding(vertical = 10.dp)
            )
        }
    }
}
