package io.github.nopsllide.riff.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import io.github.nopsllide.riff.RiffApplication
import io.github.nopsllide.riff.core.database.PlaylistEntity
import io.github.nopsllide.riff.core.database.TrackEntity
import io.github.nopsllide.riff.core.model.Lyrics
import io.github.nopsllide.riff.core.model.Playlist
import io.github.nopsllide.riff.core.model.Track
import io.github.nopsllide.riff.core.player.SleepTimerMode
import io.github.nopsllide.riff.feature.discover.DiscoverScreen
import io.github.nopsllide.riff.feature.equalizer.EqualizerScreen
import io.github.nopsllide.riff.feature.library.CreatePlaylistDialog
import io.github.nopsllide.riff.feature.library.LibraryScreen
import io.github.nopsllide.riff.feature.library.PlaylistDetailScreen
import io.github.nopsllide.riff.feature.lyrics.LyricsScreen
import io.github.nopsllide.riff.feature.player.FullPlayerScreen
import io.github.nopsllide.riff.feature.player.MiniPlayer
import io.github.nopsllide.riff.feature.player.QueueSheet
import io.github.nopsllide.riff.feature.player.SleepTimerDialog
import io.github.nopsllide.riff.feature.player.TvKaraokeScreen
import io.github.nopsllide.riff.feature.search.SearchScreen
import io.github.nopsllide.riff.feature.settings.SettingsScreen
import kotlinx.coroutines.launch
import java.util.UUID

@Composable
fun RiffNavGraph(
    app: RiffApplication,
    customPrimaryColor: Color?,
    onSelectCustomPrimaryColor: (Color?) -> Unit,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()
    var currentRoute by remember { mutableStateOf(Screen.Discover.route) }

    val playbackManager = app.playbackManager
    val currentTrack by playbackManager.currentTrack.collectAsState()
    val queue by playbackManager.queue.collectAsState()
    val currentIndex by playbackManager.currentIndex.collectAsState()
    val isPlaying by playbackManager.isPlaying.collectAsState()
    val isBuffering by playbackManager.isBuffering.collectAsState()
    val currentPositionMs by playbackManager.currentPositionMs.collectAsState()
    val durationMs by playbackManager.durationMs.collectAsState()
    val isShuffle by playbackManager.isShuffle.collectAsState()
    val isRepeat by playbackManager.isRepeat.collectAsState()
    val sleepTimerMode by playbackManager.sleepTimer.collectAsState()
    val eqSettings by playbackManager.equalizerController.settings.collectAsState()

    val favoritesEntities by app.database.trackDao().getFavorites().collectAsState(initial = emptyList())
    val historyEntities by app.database.trackDao().getHistory().collectAsState(initial = emptyList())
    val playlistEntities by app.database.playlistDao().getAllPlaylists().collectAsState(initial = emptyList())

    val favoriteTracks = favoritesEntities.map { it.toTrack().copy(isFavorite = true) }
    val historyTracks = historyEntities.map { it.toTrack() }
    val playlists = playlistEntities.map { Playlist(id = it.id, title = it.title, trackCount = 0) }

    var hotTracks by remember { mutableStateOf<List<Track>>(emptyList()) }
    var isHotLoading by remember { mutableStateOf(true) }

    var searchQuery by remember { mutableStateOf("") }
    var selectedPlatform by remember { mutableStateOf("youtube") }
    var searchResults by remember { mutableStateOf<List<Track>>(emptyList()) }
    var isSearchLoading by remember { mutableStateOf(false) }

    var currentLyrics by remember { mutableStateOf<Lyrics?>(null) }
    var isLyricsLoading by remember { mutableStateOf(false) }

    var isWavyEnabled by remember { mutableStateOf(true) }
    var isBassPulseEnabled by remember { mutableStateOf(true) }

    var isFullPlayerVisible by remember { mutableStateOf(false) }
    var isLyricsVisible by remember { mutableStateOf(false) }
    var isEqualizerVisible by remember { mutableStateOf(false) }
    var isTvKaraokeVisible by remember { mutableStateOf(false) }
    var isQueueVisible by remember { mutableStateOf(false) }
    var isSleepTimerVisible by remember { mutableStateOf(false) }
    var isSettingsVisible by remember { mutableStateOf(false) }
    var isCreatePlaylistVisible by remember { mutableStateOf(false) }
    var selectedPlaylistDetail by remember { mutableStateOf<Playlist?>(null) }

    LaunchedEffect(Unit) {
        isHotLoading = true
        val picks = app.chartsClient.getHotPicks(20)
        hotTracks = if (picks.isNotEmpty()) picks else app.innertubeClient.search("Top Hits 2025", 15)
        isHotLoading = false
    }

    LaunchedEffect(currentTrack?.id) {
        val track = currentTrack
        if (track != null) {
            app.database.trackDao().insertOrUpdate(
                TrackEntity.fromTrack(track, isFavorite = favoriteTracks.any { it.id == track.id }, isHistory = true)
            )
            isLyricsLoading = true
            currentLyrics = app.lyricsClient.getLyrics(track.title, track.artist)
            isLyricsLoading = false
        } else {
            currentLyrics = null
        }
    }

    LaunchedEffect(searchQuery, selectedPlatform) {
        if (searchQuery.length >= 2) {
            isSearchLoading = true
            searchResults = if (selectedPlatform == "youtube") {
                app.innertubeClient.search(searchQuery, 20)
            } else {
                app.soundCloudClient.search(searchQuery, 20)
            }
            isSearchLoading = false
        } else {
            searchResults = emptyList()
        }
    }

    fun onFavoriteToggle(track: Track) {
        scope.launch {
            val isFav = favoriteTracks.any { it.id == track.id }
            app.database.trackDao().insertOrUpdate(
                TrackEntity.fromTrack(track, isFavorite = !isFav)
            )
        }
    }

    fun onTrackPlay(track: Track, newQueue: List<Track>) {
        playbackManager.playTrack(track, newQueue)
    }

    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(
            bottomBar = {
                RiffBottomNav(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        currentRoute = route
                        navController.navigate(route) {
                            popUpTo(Screen.Discover.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                NavHost(
                    navController = navController,
                    startDestination = Screen.Discover.route,
                    modifier = Modifier.fillMaxSize()
                ) {
                    composable(Screen.Discover.route) {
                        DiscoverScreen(
                            hotTracks = hotTracks,
                            historyTracks = historyTracks,
                            isLoading = isHotLoading,
                            currentTrack = currentTrack,
                            searchQuery = searchQuery,
                            onQueryChange = { searchQuery = it },
                            selectedPlatform = selectedPlatform,
                            onPlatformSelect = { selectedPlatform = it },
                            searchResults = searchResults,
                            isSearchLoading = isSearchLoading,
                            onTrackClick = { t, q -> onTrackPlay(t, q) },
                            onFavoriteToggle = { onFavoriteToggle(it) },
                            onRefreshWave = {
                                scope.launch {
                                    isHotLoading = true
                                    val picks = app.chartsClient.getHotPicks(20)
                                    hotTracks = if (picks.isNotEmpty()) picks.shuffled() else app.innertubeClient.search("Top Hits 2025", 15)
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
                    }
                    composable(Screen.Search.route) {
                        SearchScreen(
                            query = searchQuery,
                            onQueryChange = { searchQuery = it },
                            onSearch = {},
                            selectedPlatform = selectedPlatform,
                            onPlatformSelect = { selectedPlatform = it },
                            results = searchResults,
                            isLoading = isSearchLoading,
                            currentTrack = currentTrack,
                            onTrackClick = { t, q -> onTrackPlay(t, q) },
                            onFavoriteToggle = { onFavoriteToggle(it) }
                        )
                    }
                    composable(Screen.Library.route) {
                        LibraryScreen(
                            favoriteTracks = favoriteTracks,
                            historyTracks = historyTracks,
                            playlists = playlists,
                            currentTrack = currentTrack,
                            onTrackClick = { t, q -> onTrackPlay(t, q) },
                            onFavoriteToggle = { onFavoriteToggle(it) },
                            onClearHistory = {
                                scope.launch { app.database.trackDao().clearHistory() }
                            },
                            onCreatePlaylistClick = { isCreatePlaylistVisible = true },
                            onPlaylistClick = { selectedPlaylistDetail = it }
                        )
                    }
                }

                if (currentTrack != null && !isFullPlayerVisible && !isTvKaraokeVisible) {
                    MiniPlayer(
                        currentTrack = currentTrack,
                        isPlaying = isPlaying,
                        isBuffering = isBuffering,
                        onPlayPauseClick = { playbackManager.togglePlayPause() },
                        onNextClick = { playbackManager.playNext() },
                        onFavoriteToggle = { currentTrack?.let { onFavoriteToggle(it) } },
                        onClick = { isFullPlayerVisible = true },
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 8.dp)
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = isFullPlayerVisible,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it })
        ) {
            FullPlayerScreen(
                currentTrack = currentTrack,
                isPlaying = isPlaying,
                isBuffering = isBuffering,
                currentPositionMs = currentPositionMs,
                durationMs = durationMs,
                isShuffle = isShuffle,
                isRepeat = isRepeat,
                isWavyEnabled = isWavyEnabled,
                isBassPulseEnabled = isBassPulseEnabled,
                onPlayPauseClick = { playbackManager.togglePlayPause() },
                onNextClick = { playbackManager.playNext() },
                onPrevClick = { playbackManager.playPrevious() },
                onSeekTo = { playbackManager.seekTo(it) },
                onToggleShuffle = { playbackManager.toggleShuffle() },
                onToggleRepeat = { playbackManager.toggleRepeat() },
                onToggleFavorite = { currentTrack?.let { onFavoriteToggle(it) } },
                onDismiss = { isFullPlayerVisible = false },
                onOpenLyrics = { isLyricsVisible = true },
                onOpenEqualizer = { isEqualizerVisible = true },
                onOpenTvKaraoke = {
                    isFullPlayerVisible = false
                    isTvKaraokeVisible = true
                },
                onOpenQueue = { isQueueVisible = true },
                onOpenSleepTimer = { isSleepTimerVisible = true }
            )
        }

        AnimatedVisibility(
            visible = isQueueVisible,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it })
        ) {
            QueueSheet(
                queue = queue,
                currentIndex = currentIndex,
                onTrackClick = { track, _ -> playbackManager.playTrack(track) },
                onRemoveTrack = { playbackManager.removeFromQueue(it) },
                onClearQueue = { playbackManager.clearQueue() },
                onDismiss = { isQueueVisible = false }
            )
        }

        AnimatedVisibility(
            visible = isTvKaraokeVisible,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it })
        ) {
            TvKaraokeScreen(
                currentTrack = currentTrack,
                isPlaying = isPlaying,
                currentPositionMs = currentPositionMs,
                durationMs = durationMs,
                lyrics = currentLyrics,
                onPlayPauseClick = { playbackManager.togglePlayPause() },
                onNextClick = { playbackManager.playNext() },
                onPrevClick = { playbackManager.playPrevious() },
                onSeekTo = { playbackManager.seekTo(it) },
                onClose = { isTvKaraokeVisible = false }
            )
        }

        AnimatedVisibility(
            visible = isLyricsVisible,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it })
        ) {
            LyricsScreen(
                lyrics = currentLyrics,
                isLoading = isLyricsLoading,
                currentPositionMs = currentPositionMs,
                onSeekTo = { playbackManager.seekTo(it) },
                onDismiss = { isLyricsVisible = false },
                trackTitle = currentTrack?.title ?: "",
                trackArtist = currentTrack?.artist ?: ""
            )
        }

        AnimatedVisibility(
            visible = isEqualizerVisible,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it })
        ) {
            EqualizerScreen(
                settings = eqSettings,
                onBandChange = { idx, gain -> playbackManager.equalizerController.setEqBand(idx, gain) },
                onPresetSelect = { playbackManager.equalizerController.setEqPreset(it) },
                onBassBoostChange = { playbackManager.equalizerController.setBassBoost(it) },
                onSpeedChange = { playbackManager.equalizerController.setSpeed(it) },
                onPitchChange = { playbackManager.equalizerController.setPitch(it) },
                onReset = { playbackManager.equalizerController.reset() },
                onDismiss = { isEqualizerVisible = false }
            )
        }

        AnimatedVisibility(
            visible = isSettingsVisible,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it })
        ) {
            SettingsScreen(
                customPrimary = customPrimaryColor,
                onSelectPrimary = onSelectCustomPrimaryColor,
                isWavyEnabled = isWavyEnabled,
                onToggleWavy = { isWavyEnabled = it },
                isBassPulseEnabled = isBassPulseEnabled,
                onToggleBassPulse = { isBassPulseEnabled = it },
                onOpenSleepTimer = { isSleepTimerVisible = true },
                onDismiss = { isSettingsVisible = false }
            )
        }

        if (isSleepTimerVisible) {
            SleepTimerDialog(
                currentMode = sleepTimerMode,
                onSelectMinutes = { playbackManager.setSleepTimer(it) },
                onSelectEndOfTrack = { playbackManager.setSleepTimerEndOfTrack() },
                onCancelTimer = { playbackManager.cancelSleepTimer() },
                onDismiss = { isSleepTimerVisible = false }
            )
        }

        if (isCreatePlaylistVisible) {
            CreatePlaylistDialog(
                onConfirm = { name ->
                    isCreatePlaylistVisible = false
                    scope.launch {
                        app.database.playlistDao().insertPlaylist(
                            PlaylistEntity(id = UUID.randomUUID().toString(), title = name)
                        )
                    }
                },
                onDismiss = { isCreatePlaylistVisible = false }
            )
        }

        if (selectedPlaylistDetail != null) {
            val pl = selectedPlaylistDetail!!
            PlaylistDetailScreen(
                playlist = pl,
                currentTrack = currentTrack,
                onPlayAll = {
                    if (pl.tracks.isNotEmpty()) {
                        playbackManager.playTrack(pl.tracks.first(), pl.tracks)
                    }
                },
                onShuffleAll = {
                    if (pl.tracks.isNotEmpty()) {
                        playbackManager.toggleShuffle()
                        playbackManager.playTrack(pl.tracks.random(), pl.tracks)
                    }
                },
                onTrackClick = { playbackManager.playTrack(it, pl.tracks) },
                onFavoriteToggle = { onFavoriteToggle(it) },
                onDeduplicate = {},
                onDeletePlaylist = {
                    scope.launch {
                        app.database.playlistDao().deletePlaylist(pl.id)
                        selectedPlaylistDetail = null
                    }
                },
                onBack = { selectedPlaylistDetail = null }
            )
        }
    }
}
