package io.github.rootscripts.riff.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import io.github.rootscripts.riff.core.model.Playlist

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String = "",
    val thumbnail: String = "",
    val trackIdsJson: String = "[]",
    val createdAt: Long = System.currentTimeMillis()
)
