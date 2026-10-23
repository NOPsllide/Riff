package io.github.nopsllide.riff.core.player

import android.content.Context
import android.content.Intent
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import io.github.nopsllide.riff.core.model.Track
import io.github.nopsllide.riff.core.network.RiffApiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

sealed class SleepTimerMode {
    object Off : SleepTimerMode()
    object EndOfTrack : SleepTimerMode()
    data class Minutes(val remainingSeconds: Int) : SleepTimerMode()
}

class PlaybackManager(
    private val context: Context,
    private val riffApiClient: RiffApiClient,
    val equalizerController: EqualizerController = EqualizerController()
) {
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var playJob: Job? = null
    private var playRequestId: Long = 0L
    private var progressJob: Job? = null
    private var sleepTimerJob: Job? = null
    private var hasRetriedFallback = false

    val exoPlayer: ExoPlayer by lazy {
        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36")
            .setAllowCrossProtocolRedirects(true)
            .setConnectTimeoutMs(15000)
            .setReadTimeoutMs(20000)

        val mediaSourceFactory = DefaultMediaSourceFactory(context)
            .setDataSourceFactory(httpDataSourceFactory)

        ExoPlayer.Builder(context)
            .setMediaSourceFactory(mediaSourceFactory)
            .build().apply {
                addListener(object : Player.Listener {
                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        _isPlaying.value = isPlaying
                        if (isPlaying) startProgressTicker() else stopProgressTicker()
                    }

                    override fun onAudioSessionIdChanged(audioSessionId: Int) {
                        if (audioSessionId > 0) {
                            equalizerController.attachPlayer(this@apply, audioSessionId)
                        }
                    }

                    override fun onPlayerError(error: PlaybackException) {
                        _isBuffering.value = false
                        _isPlaying.value = false
                        if (!hasRetriedFallback) {
                            hasRetriedFallback = true
                            val track = _currentTrack.value
                            if (track != null) {
                                val currentRequestId = ++playRequestId
                                scope.launch {
                                    try {
                                        val streamUrl = riffApiClient.getStreamUrl(
                                            trackId = track.id,
                                            title = track.title,
                                            artist = track.artist,
                                            platform = track.platform
                                        )
                                        if (currentRequestId != playRequestId) return@launch
                                        if (!streamUrl.isNullOrBlank()) {
                                            _isBuffering.value = true
                                            val mediaItem = MediaItem.fromUri(streamUrl)
                                            setMediaItem(mediaItem)
                                            prepare()
                                            playWhenReady = true
                                        } else {
                                            _isBuffering.value = false
                                        }
                                    } catch (t: Throwable) {
                                        _isBuffering.value = false
                                    }
                                }
                            }
                        }
                    }

                    override fun onPlaybackStateChanged(playbackState: Int) {
                        _isBuffering.value = playbackState == Player.STATE_BUFFERING
                        if (playbackState == Player.STATE_READY) {
                            _durationMs.value = duration.coerceAtLeast(0L)
                            if (audioSessionId > 0) {
                                equalizerController.attachPlayer(this@apply, audioSessionId)
                            }
                        } else if (playbackState == Player.STATE_ENDED) {
                            if (_sleepTimer.value is SleepTimerMode.EndOfTrack) {
                                _sleepTimer.value = SleepTimerMode.Off
                                pause()
                            } else {
                                playNext()
                            }
                        }
                    }
                })
                if (audioSessionId > 0) {
                    equalizerController.attachPlayer(this, audioSessionId)
                }
            }
    }

    private fun ensureServiceStarted() {
        try {
            val intent = Intent(context, RiffPlaybackService::class.java)
            context.startService(intent)
        } catch (t: Throwable) {
        }
    }

    private val _currentTrack = MutableStateFlow<Track?>(null)
    val currentTrack: StateFlow<Track?> = _currentTrack.asStateFlow()

    private val _queue = MutableStateFlow<List<Track>>(emptyList())
    val queue: StateFlow<List<Track>> = _queue.asStateFlow()

    private val _currentIndex = MutableStateFlow(0)
    val currentIndex: StateFlow<Int> = _currentIndex.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _isBuffering = MutableStateFlow(false)
    val isBuffering: StateFlow<Boolean> = _isBuffering.asStateFlow()

    private val _currentPositionMs = MutableStateFlow(0L)
    val currentPositionMs: StateFlow<Long> = _currentPositionMs.asStateFlow()

    private val _durationMs = MutableStateFlow(0L)
    val durationMs: StateFlow<Long> = _durationMs.asStateFlow()

    private val _isShuffle = MutableStateFlow(false)
    val isShuffle: StateFlow<Boolean> = _isShuffle.asStateFlow()

    private val _isRepeat = MutableStateFlow(false)
    val isRepeat: StateFlow<Boolean> = _isRepeat.asStateFlow()

    private val _sleepTimer = MutableStateFlow<SleepTimerMode>(SleepTimerMode.Off)
    val sleepTimer: StateFlow<SleepTimerMode> = _sleepTimer.asStateFlow()

    fun playTrack(track: Track, newQueue: List<Track>? = null) {
        if (newQueue != null) {
            _queue.value = newQueue
            _currentIndex.value = newQueue.indexOfFirst { it.id == track.id }.coerceAtLeast(0)
        } else if (!_queue.value.any { it.id == track.id }) {
            _queue.value = listOf(track) + _queue.value
            _currentIndex.value = 0
        } else {
            _currentIndex.value = _queue.value.indexOfFirst { it.id == track.id }.coerceAtLeast(0)
        }

        _currentTrack.value = track
        _isBuffering.value = true
        hasRetriedFallback = false
        ensureServiceStarted()

        playJob?.cancel()
        val currentRequestId = ++playRequestId

        playJob = scope.launch {
            try {
                val streamUrl = track.streamUrl ?: riffApiClient.getStreamUrl(
                    trackId = track.id,
                    title = track.title,
                    artist = track.artist,
                    platform = track.platform
                )

                if (currentRequestId != playRequestId) return@launch

                if (!streamUrl.isNullOrBlank()) {
                    val mediaItem = MediaItem.fromUri(streamUrl)
                    exoPlayer.setMediaItem(mediaItem)
                    exoPlayer.prepare()
                    exoPlayer.playWhenReady = true
                } else {
                    _isBuffering.value = false
                }
            } catch (t: Throwable) {
                if (currentRequestId == playRequestId) {
                    _isBuffering.value = false
                }
            }
        }
    }

    fun togglePlayPause() {
        try {
            if (exoPlayer.isPlaying) {
                exoPlayer.pause()
            } else {
                if (exoPlayer.playbackState == Player.STATE_IDLE || exoPlayer.mediaItemCount == 0 || exoPlayer.playerError != null) {
                    val track = _currentTrack.value
                    if (track != null) {
                        playTrack(track)
                        return
                    }
                }
                if (exoPlayer.playbackState == Player.STATE_ENDED) {
                    exoPlayer.seekTo(0)
                }
                ensureServiceStarted()
                exoPlayer.play()
            }
        } catch (t: Throwable) {
        }
    }

    fun pause() {
        try {
            exoPlayer.pause()
        } catch (t: Throwable) {
        }
    }

    fun play() {
        try {
            ensureServiceStarted()
            exoPlayer.play()
        } catch (t: Throwable) {
        }
    }

    fun playNext() {
        try {
            val q = _queue.value
            if (q.isEmpty()) return

            if (_isRepeat.value) {
                exoPlayer.seekTo(0)
                exoPlayer.play()
                return
            }

            val nextIndex = if (_isShuffle.value) {
                val candidates = q.indices.filter { it != _currentIndex.value }
                if (candidates.isNotEmpty()) candidates.random() else _currentIndex.value
            } else {
                _currentIndex.value + 1
            }

            if (nextIndex in q.indices) {
                _currentIndex.value = nextIndex
                playTrack(q[nextIndex])
            }
        } catch (t: Throwable) {
        }
    }

    fun playPrevious() {
        try {
            if (exoPlayer.currentPosition > 3000L) {
                exoPlayer.seekTo(0)
                return
            }

            val q = _queue.value
            if (q.isEmpty()) return

            val prevIndex = _currentIndex.value - 1
            if (prevIndex in q.indices) {
                _currentIndex.value = prevIndex
                playTrack(q[prevIndex])
            } else {
                exoPlayer.seekTo(0)
            }
        } catch (t: Throwable) {
        }
    }

    fun seekTo(positionMs: Long) {
        try {
            _currentPositionMs.value = positionMs
            exoPlayer.seekTo(positionMs)
        } catch (t: Throwable) {
        }
    }

    fun toggleShuffle() {
        _isShuffle.value = !_isShuffle.value
    }

    fun toggleRepeat() {
        _isRepeat.value = !_isRepeat.value
        exoPlayer.repeatMode = if (_isRepeat.value) Player.REPEAT_MODE_ONE else Player.REPEAT_MODE_OFF
    }

    fun removeFromQueue(index: Int) {
        val current = _queue.value.toMutableList()
        if (index in current.indices) {
            current.removeAt(index)
            _queue.value = current
            if (index < _currentIndex.value) {
                _currentIndex.value = (_currentIndex.value - 1).coerceAtLeast(0)
            }
        }
    }

    fun clearQueue() {
        _queue.value = emptyList()
        _currentTrack.value = null
        exoPlayer.stop()
        exoPlayer.clearMediaItems()
    }

    fun setSleepTimer(minutes: Int) {
        sleepTimerJob?.cancel()
        if (minutes <= 0) {
            _sleepTimer.value = SleepTimerMode.Off
            return
        }
        val totalSeconds = minutes * 60
        _sleepTimer.value = SleepTimerMode.Minutes(totalSeconds)
        sleepTimerJob = scope.launch {
            var remaining = totalSeconds
            while (remaining > 0 && isActive) {
                delay(1000)
                remaining--
                _sleepTimer.value = SleepTimerMode.Minutes(remaining)
            }
            if (isActive) {
                _sleepTimer.value = SleepTimerMode.Off
                pause()
            }
        }
    }

    fun setSleepTimerEndOfTrack() {
        sleepTimerJob?.cancel()
        _sleepTimer.value = SleepTimerMode.EndOfTrack
    }

    fun cancelSleepTimer() {
        sleepTimerJob?.cancel()
        _sleepTimer.value = SleepTimerMode.Off
    }

    private fun startProgressTicker() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (isActive) {
                _currentPositionMs.value = exoPlayer.currentPosition
                _durationMs.value = exoPlayer.duration.coerceAtLeast(0L)
                delay(200)
            }
        }
    }

    private fun stopProgressTicker() {
        progressJob?.cancel()
        _currentPositionMs.value = exoPlayer.currentPosition
    }

    fun release() {
        playJob?.cancel()
        stopProgressTicker()
        sleepTimerJob?.cancel()
        exoPlayer.release()
        equalizerController.release()
    }
}
