package io.github.rootscripts.riff.core.model

import kotlinx.serialization.Serializable

@Serializable
data class Playlist(
    val id: String,
    val title: String,
    val description: String = "",
    val trackCount: Int = 0,
    val thumbnail: String = "",
    val tracks: List<Track> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
)
