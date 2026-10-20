package io.github.rootscripts.riff.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackDao {
    @Query("SELECT * FROM tracks WHERE isFavorite = 1 ORDER BY addedAt DESC")
    fun getFavorites(): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks WHERE isHistory = 1 ORDER BY playedAt DESC LIMIT 100")
    fun getHistory(): Flow<List<TrackEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(track: TrackEntity)

    @Query("UPDATE tracks SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun setFavorite(id: String, isFavorite: Boolean)

    @Query("DELETE FROM tracks WHERE isHistory = 1")
    suspend fun clearHistory()
}
