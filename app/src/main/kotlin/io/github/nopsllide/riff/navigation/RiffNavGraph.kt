package io.github.nopsllide.riff.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import io.github.nopsllide.riff.RiffApplication
import io.github.nopsllide.riff.core.model.Artist
import io.github.nopsllide.riff.core.model.Lyrics
import io.github.nopsllide.riff.core.model.Track
import io.github.nopsllide.riff.feature.discover.DiscoverScreen
import io.github.nopsllide.riff.feature.player.FullPlayerScreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun RiffNavGraph(
    app: RiffApplication,
    customPrimaryColor: Color?,
    onSelectCustomPrimaryColor: (Color?) -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()

    val playbackManager = app.playbackManager
    val currentTrack by playbackManager.currentTrack.collectAsState()
    val isPlaying by playbackManager.isPlaying.collectAsState()
    val isBuffering by playbackManager.isBuffering.collectAsState()
    val currentPositionMs by playbackManager.currentPositionMs.collectAsState()
    val durationMs by playbackManager.durationMs.collectAsState()
    val isShuffle by playbackManager.isShuffle.collectAsState()
    val isRepeat by playbackManager.isRepeat.collectAsState()

    var hotTracks by remember { mutableStateOf<List<Track>>(emptyList()) }
    var popularArtists by remember { mutableStateOf<List<Artist>>(emptyList()) }
    var isHotLoading by remember { mutableStateOf(true) }

    var searchQuery by remember { mutableStateOf("") }
    var selectedPlatform by remember { mutableStateOf("youtube") }
    var searchResults by remember { mutableStateOf<List<Track>>(emptyList()) }
    var isSearchLoading by remember { mutableStateOf(false) }

    var currentLyrics by remember { mutableStateOf<Lyrics?>(null) }
    var isLyricsLoading by remember { mutableStateOf(false) }

    var isFullPlayerVisible by remember { mutableStateOf(false) }

    BackHandler(enabled = isFullPlayerVisible) {
        isFullPlayerVisible = false
    }

    LaunchedEffect(Unit) {
        isHotLoading = true
        val picks = app.riffApiClient.getTrending(20)
        hotTracks = picks
        popularArtists = app.riffApiClient.getPopularArtists(20)
        isHotLoading = false
    }

    LaunchedEffect(currentTrack?.id) {
        val track = currentTrack
        if (track != null) {
            isLyricsLoading = true
            currentLyrics = app.riffApiClient.getLyrics(track.id, track.title, track.artist)
            isLyricsLoading = false
        } else {
            currentLyrics = null
        }
    }

    LaunchedEffect(searchQuery, selectedPlatform) {
        if (searchQuery.length >= 2) {
            delay(350)
            isSearchLoading = true
            try {
                val results = app.riffApiClient.search(searchQuery, selectedPlatform, 20)
                searchResults = results
            } catch (t: Throwable) {
                searchResults = emptyList()
            } finally {
                isSearchLoading = false
            }
        } else {
            searchResults = emptyList()
        }
    }

    fun onTrackPlay(track: Track, newQueue: List<Track>) {
        playbackManager.playTrack(track, newQueue)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF181818))
    ) {
        DiscoverScreen(
            hotTracks = hotTracks,
            popularArtists = popularArtists,
            historyTracks = emptyList(),
            isLoading = isHotLoading,
            currentTrack = currentTrack,
            isPlaying = isPlaying,
            onPlayPauseToggle = {
                if (currentTrack == null && hotTracks.isNotEmpty()) {
                    onTrackPlay(hotTracks.first(), hotTracks)
                } else {
                    playbackManager.togglePlayPause()
                }
            },
            onNextTrack = { playbackManager.playNext() },
            onOpenFullPlayer = { isFullPlayerVisible = true },
            searchQuery = searchQuery,
            onQueryChange = { searchQuery = it },
            selectedPlatform = selectedPlatform,
            onPlatformSelect = { selectedPlatform = it },
            searchResults = searchResults,
            isSearchLoading = isSearchLoading,
            onTrackClick = { t, q -> onTrackPlay(t, q) },
            onArtistClick = { artist -> searchQuery = artist.name },
            onFavoriteToggle = {},
            onRefreshWave = {
                scope.launch {
                    isHotLoading = true
                    val picks = app.riffApiClient.getTrending(20)
                    hotTracks = if (picks.isNotEmpty()) picks.shuffled() else emptyList()
                    popularArtists = app.riffApiClient.getPopularArtists(20)
                    isHotLoading = false
                }
            },
            onPlayWave = { list, startIdx ->
                if (list.isNotEmpty()) {
                    val idx = startIdx.coerceIn(0, list.size - 1)
                    onTrackPlay(list[idx], list)
                }
            }
        )

        AnimatedVisibility(
            visible = isFullPlayerVisible,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = tween(durationMillis = 420, easing = CubicBezierEasing(0.32f, 0.72f, 0f, 1f))
            ) + fadeIn(animationSpec = tween(durationMillis = 280)),
            exit = slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(durationMillis = 360, easing = CubicBezierEasing(0.32f, 0.72f, 0f, 1f))
            ) + fadeOut(animationSpec = tween(durationMillis = 220))
        ) {
            FullPlayerScreen(
                currentTrack = currentTrack ?: hotTracks.firstOrNull(),
                isPlaying = isPlaying,
                isBuffering = isBuffering,
                currentPositionMs = currentPositionMs,
                durationMs = durationMs,
                isShuffle = isShuffle,
                isRepeat = isRepeat,
                lyrics = currentLyrics,
                isLyricsLoading = isLyricsLoading,
                dominantColor = customPrimaryColor,
                onPlayPauseClick = {
                    val track = playbackManager.currentTrack.value ?: hotTracks.firstOrNull()
                    if (playbackManager.isPlaying.value) {
                        playbackManager.pause()
                    } else if (playbackManager.exoPlayer.mediaItemCount == 0 && track != null) {
                        playbackManager.playTrack(track, hotTracks)
                    } else {
                        playbackManager.togglePlayPause()
                    }
                },
                onNextClick = {
                    if (playbackManager.currentTrack.value == null && hotTracks.isNotEmpty()) {
                        playbackManager.playTrack(hotTracks.first(), hotTracks)
                    } else {
                        playbackManager.playNext()
                    }
                },
                onPrevClick = { playbackManager.playPrevious() },
                onSeekTo = { playbackManager.seekTo(it) },
                onToggleShuffle = { playbackManager.toggleShuffle() },
                onToggleRepeat = { playbackManager.toggleRepeat() },
                onDismiss = { isFullPlayerVisible = false }
            )
        }
    }
}
