package io.github.rootscripts.riff.core.player

import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.PresetReverb
import androidx.media3.common.PlaybackParameters
import androidx.media3.exoplayer.ExoPlayer
import io.github.rootscripts.riff.core.model.AudioSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.abs

class EqualizerController {
    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null
    private var presetReverb: PresetReverb? = null
    private var exoPlayer: ExoPlayer? = null

    private val _settings = MutableStateFlow(AudioSettings())
    val settings: StateFlow<AudioSettings> = _settings.asStateFlow()

    fun attachPlayer(player: ExoPlayer, audioSessionId: Int) {
        this.exoPlayer = player
        releaseAudioFx()
        try {
            if (audioSessionId != 0) {
                equalizer = Equalizer(0, audioSessionId).apply {
                    enabled = _settings.value.isEnabled
                }
                bassBoost = BassBoost(0, audioSessionId).apply {
                    enabled = _settings.value.isEnabled
                }
                presetReverb = PresetReverb(0, audioSessionId).apply {
                    enabled = _settings.value.isEnabled
                }
                applySettings(_settings.value)
            }
        } catch (e: Exception) {
        }
    }

    fun applySettings(newSettings: AudioSettings) {
        _settings.value = newSettings

        val player = exoPlayer
        if (player != null) {
            val speed = newSettings.speed.coerceIn(0.5f, 2.0f)
            val pitchMultiplier = Math.pow(2.0, (newSettings.pitch / 12.0)).toFloat().coerceIn(0.5f, 2.0f)
            player.playbackParameters = PlaybackParameters(speed, pitchMultiplier)
        }

        val eq = equalizer
        if (eq != null && eq.enabled) {
            val numBands = eq.numberOfBands.toInt()
            val minBandLevel = eq.bandLevelRange[0]
            val maxBandLevel = eq.bandLevelRange[1]

            for (i in 0 until numBands) {
                val centerFreq = eq.getCenterFreq(i.toShort()) / 1000
                val targetGain = findClosestGain(centerFreq, newSettings.eqBands)
                val millibels = (targetGain * 100).toInt().coerceIn(minBandLevel.toInt(), maxBandLevel.toInt()).toShort()
                eq.setBandLevel(i.toShort(), millibels)
            }
        }

        val bb = bassBoost
        if (bb != null && bb.strengthSupported) {
            val strength = (newSettings.bassBoost * 10).coerceIn(0, 1000).toShort()
            bb.setStrength(strength)
        }
    }

    fun setSpeed(speed: Float) {
        applySettings(_settings.value.copy(speed = speed))
    }

    fun setPitch(pitch: Float) {
        applySettings(_settings.value.copy(pitch = pitch))
    }

    fun setBassBoost(value: Int) {
        applySettings(_settings.value.copy(bassBoost = value))
    }

    fun setEqBand(index: Int, gain: Float) {
        val current = _settings.value.eqBands.toMutableList()
        if (index in current.indices) {
            current[index] = gain.coerceIn(-12f, 12f)
            applySettings(_settings.value.copy(eqBands = current))
        }
    }

    fun setEqPreset(preset: List<Float>) {
        applySettings(_settings.value.copy(eqBands = preset))
    }

    fun reset() {
        applySettings(AudioSettings())
    }

    private fun findClosestGain(freqHz: Int, gains: List<Float>): Float {
        var closestIdx = 0
        var minDiff = Int.MAX_VALUE
        for (i in AudioSettings.FREQUENCIES.indices) {
            val diff = abs(AudioSettings.FREQUENCIES[i] - freqHz)
            if (diff < minDiff) {
                minDiff = diff
                closestIdx = i
            }
        }
        return gains.getOrElse(closestIdx) { 0.0f }
    }

    private fun releaseAudioFx() {
        try {
            equalizer?.release()
            bassBoost?.release()
            presetReverb?.release()
        } catch (e: Exception) {
        }
        equalizer = null
        bassBoost = null
        presetReverb = null
    }

    fun release() {
        releaseAudioFx()
        exoPlayer = null
    }
}
