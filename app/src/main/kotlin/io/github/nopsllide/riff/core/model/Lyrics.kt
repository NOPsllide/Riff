package io.github.nopsllide.riff.core.model

import kotlinx.serialization.Serializable

@Serializable
data class LyricLine(
    val timestampMs: Long,
    val text: String
)

@Serializable
data class Lyrics(
    val syncedLines: List<LyricLine> = emptyList(),
    val plainText: String = "",
    val isSynced: Boolean = false
)
