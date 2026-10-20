package io.github.rootscripts.riff.feature.library

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.rootscripts.riff.core.designsystem.component.TrackRow
import io.github.rootscripts.riff.core.model.Playlist
import io.github.rootscripts.riff.core.model.Track

@Composable
fun PlaylistDetailScreen(
    playlist: Playlist,
    currentTrack: Track?,
    onPlayAll: () -> Unit,
    onShuffleAll: () -> Unit,
    onTrackClick: (Track) -> Unit,
    onFavoriteToggle: (Track) -> Unit,
    onDeduplicate: () -> Unit,
    onDeletePlaylist: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row {
                    IconButton(onClick = onDeduplicate) {
                        Text(
                            text = "Dedupe",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = onDeletePlaylist) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Delete Playlist",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
                Text(
                    text = playlist.title,
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${playlist.tracks.size} tracks",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onPlayAll,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(9999.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(imageVector = Icons.Filled.PlayArrow, contentDescription = "Play")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Play")
                    }

                    OutlinedButton(
                        onClick = onShuffleAll,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(9999.dp)
                    ) {
                        Icon(imageVector = Icons.Filled.Shuffle, contentDescription = "Shuffle")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Shuffle")
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (playlist.tracks.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "This playlist is empty",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 120.dp)
                ) {
                    items(playlist.tracks) { track ->
                        TrackRow(
                            track = track,
                            isPlaying = currentTrack?.id == track.id,
                            onClick = { onTrackClick(track) },
                            onFavoriteToggle = { onFavoriteToggle(track) }
                        )
                    }
                }
            }
        }
    }
}
