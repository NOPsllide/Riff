package io.github.nopsllide.riff.core.model

import kotlinx.serialization.Serializable

@Serializable
data class Track(
    val id: String,
    val title: String,
    val artist: String,
    val album: String = "",
    val duration: Long = 0L,
    val thumbnail: String = "",
    val url: String = "",
    val platform: String = "riff",
    val streamUrl: String? = null,
    val isFavorite: Boolean = false,
    val addedAt: Long = System.currentTimeMillis()
)
