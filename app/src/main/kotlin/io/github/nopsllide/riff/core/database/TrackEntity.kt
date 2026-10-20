package io.github.nopsllide.riff.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import io.github.nopsllide.riff.core.model.Track

@Entity(tableName = "tracks")
data class TrackEntity(
    @PrimaryKey val id: String,
    val title: String,
    val artist: String,
    val album: String = "",
    val duration: Long = 0L,
    val thumbnail: String = "",
    val url: String = "",
    val platform: String = "youtube",
    val isFavorite: Boolean = false,
    val isHistory: Boolean = false,
    val playedAt: Long = 0L,
    val addedAt: Long = System.currentTimeMillis()
) {
    fun toTrack(): Track = Track(
        id = id,
        title = title,
        artist = artist,
        album = album,
        duration = duration,
        thumbnail = thumbnail,
        url = url,
        platform = platform,
        isFavorite = isFavorite,
        addedAt = addedAt
    )

    companion object {
        fun fromTrack(track: Track, isFavorite: Boolean = track.isFavorite, isHistory: Boolean = false): TrackEntity =
            TrackEntity(
                id = track.id,
                title = track.title,
                artist = track.artist,
                album = track.album,
                duration = track.duration,
                thumbnail = track.thumbnail,
                url = track.url,
                platform = track.platform,
                isFavorite = isFavorite,
                isHistory = isHistory,
                playedAt = if (isHistory) System.currentTimeMillis() else 0L,
                addedAt = track.addedAt
            )
    }
}
