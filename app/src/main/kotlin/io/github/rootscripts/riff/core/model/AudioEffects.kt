package io.github.rootscripts.riff.core.model

import kotlinx.serialization.Serializable

@Serializable
data class AudioSettings(
    val speed: Float = 1.0f,
    val pitch: Float = 0.0f,
    val reverb: Float = 0.0f,
    val distortion: Float = 0.0f,
    val echo: Float = 0.0f,
    val volume: Float = 0.85f,
    val bassBoost: Int = 0,
    val eqBands: List<Float> = List(10) { 0.0f },
    val isEnabled: Boolean = true
) {
    companion object {
        val FREQUENCIES = listOf(32, 64, 125, 250, 500, 1000, 2000, 4000, 8000, 16000)

        val PRESET_FLAT = List(10) { 0.0f }
        val PRESET_BASS_BOOST = listOf(8.0f, 6.0f, 4.0f, 2.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f)
        val PRESET_VOCAL = listOf(-2.0f, -1.0f, 0.0f, 2.0f, 4.0f, 5.0f, 3.0f, 1.0f, 0.0f, -1.0f)
        val PRESET_ELECTRONIC = listOf(5.0f, 4.0f, 1.0f, 0.0f, -2.0f, 2.0f, 1.0f, 3.0f, 4.0f, 5.0f)
        val PRESET_ROCK = listOf(4.5f, 3.0f, -1.5f, -2.0f, 0.5f, 2.5f, 4.0f, 4.5f, 4.5f, 4.0f)
    }
}
