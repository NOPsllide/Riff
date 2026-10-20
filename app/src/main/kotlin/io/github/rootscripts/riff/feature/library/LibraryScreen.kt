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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.rootscripts.riff.core.designsystem.component.TrackRow
import io.github.rootscripts.riff.core.designsystem.theme.RiffSurfaceContainer
import io.github.rootscripts.riff.core.model.Playlist
import io.github.rootscripts.riff.core.model.Track

@Composable
fun LibraryScreen(
    favoriteTracks: List<Track>,
    historyTracks: List<Track>,
    playlists: List<Playlist>,
    currentTrack: Track?,
    onTrackClick: (Track, List<Track>) -> Unit,
    onFavoriteToggle: (Track) -> Unit,
    onClearHistory: () -> Unit,
    onCreatePlaylistClick: () -> Unit,
    onPlaylistClick: (Playlist) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf("favorites") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Library",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            if (selectedTab == "history" && historyTracks.isNotEmpty()) {
                IconButton(onClick = onClearHistory) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Clear History",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else if (selectedTab == "playlists") {
                IconButton(onClick = onCreatePlaylistClick) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Create Playlist",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            LibraryTabChip("Favorites (${favoriteTracks.size})", selectedTab == "favorites") {
                selectedTab = "favorites"
            }
            LibraryTabChip("Playlists (${playlists.size})", selectedTab == "playlists") {
                selectedTab = "playlists"
            }
            LibraryTabChip("History (${historyTracks.size})", selectedTab == "history") {
                selectedTab = "history"
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        when (selectedTab) {
            "favorites" -> {
                if (favoriteTracks.isEmpty()) {
                    EmptyStateView("No favorite tracks yet")
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 120.dp)
                    ) {
                        items(favoriteTracks) { track ->
                            TrackRow(
                                track = track,
                                isPlaying = currentTrack?.id == track.id,
                                onClick = { onTrackClick(track, favoriteTracks) },
                                onFavoriteToggle = { onFavoriteToggle(track) }
                            )
                        }
                    }
                }
            }
            "history" -> {
                if (historyTracks.isEmpty()) {
                    EmptyStateView("No playback history")
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 120.dp)
                    ) {
                        items(historyTracks) { track ->
                            TrackRow(
                                track = track,
                                isPlaying = currentTrack?.id == track.id,
                                onClick = { onTrackClick(track, historyTracks) },
                                onFavoriteToggle = { onFavoriteToggle(track) }
                            )
                        }
                    }
                }
            }
            "playlists" -> {
                if (playlists.isEmpty()) {
                    EmptyStateView("No playlists created yet. Tap + above to create one.")
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        items(playlists) { playlist ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = RiffSurfaceContainer),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable { onPlaylistClick(playlist) }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.LibraryMusic,
                                        contentDescription = "Playlist",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Spacer(modifier = Modifier.padding(8.dp))
                                    Column {
                                        Text(
                                            text = playlist.title,
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "${playlist.tracks.size} tracks",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyStateView(text: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun LibraryTabChip(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = { Text(title) },
        shape = RoundedCornerShape(9999.dp),
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    )
}
