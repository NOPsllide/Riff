package io.github.nopsllide.riff.core.network

import io.github.nopsllide.riff.core.model.Artist
import io.github.nopsllide.riff.core.model.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request

@Serializable
private data class DeezerChartResponse(
    val data: List<DeezerTrackItem> = emptyList()
)

@Serializable
private data class DeezerTrackItem(
    val id: Long? = null,
    val title: String? = null,
    val duration: Long? = null,
    val artist: DeezerArtist? = null,
    val album: DeezerAlbum? = null
)

@Serializable
private data class DeezerArtist(
    val id: Long? = null,
    val name: String? = null,
    val picture_big: String? = null,
    val picture_medium: String? = null,
    val picture_xl: String? = null
)

@Serializable
private data class DeezerAlbum(
    val id: Long? = null,
    val title: String? = null,
    val cover_xl: String? = null,
    val cover_big: String? = null,
    val cover_medium: String? = null
)

@Serializable
private data class DeezerArtistResponse(
    val data: List<DeezerArtistItem> = emptyList()
)

@Serializable
private data class DeezerArtistItem(
    val id: Long? = null,
    val name: String? = null,
    val picture_big: String? = null,
    val picture_medium: String? = null,
    val picture_xl: String? = null
)

class ChartsClient(
    private val client: OkHttpClient = OkHttpClient()
) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun getHotPicks(limit: Int = 20): List<Track> = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("https://api.deezer.com/chart/0/tracks?limit=$limit")
            .header("User-Agent", "Riff-Android/42.3.0")
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext emptyList()
                val body = response.body?.string() ?: return@withContext emptyList()
                val parsed = json.decodeFromString<DeezerChartResponse>(body)

                parsed.data.mapNotNull { item ->
                    val title = item.title ?: return@mapNotNull null
                    val artist = item.artist?.name ?: "Unknown Artist"
                    val duration = item.duration ?: 180L
                    val thumb = item.album?.cover_xl
                        ?: item.album?.cover_big
                        ?: item.album?.cover_medium
                        ?: ""

                    Track(
                        id = "dz_${item.id ?: System.nanoTime()}",
                        title = title,
                        artist = artist,
                        album = item.album?.title ?: "",
                        duration = duration,
                        thumbnail = thumb,
                        url = "",
                        platform = "deezer"
                    )
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getPopularArtists(limit: Int = 20): List<Artist> = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("https://api.deezer.com/chart/0/artists?limit=30")
            .header("User-Agent", "Riff-Android/42.3.0")
            .build()

        val ignored = listOf("???", "hörbuch", "hörbücher", "bibi", "tkkg", "hörspiel", "teufelskicker", "thriller", "die drei")

        val chartArtists = try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) emptyList()
                else {
                    val body = response.body?.string() ?: ""
                    val parsed = json.decodeFromString<DeezerArtistResponse>(body)
                    parsed.data.filter { item ->
                        val name = item.name ?: return@filter false
                        ignored.none { name.contains(it, ignoreCase = true) }
                    }.mapNotNull { item ->
                        val name = item.name ?: return@mapNotNull null
                        val pic = item.picture_big
                            ?: item.picture_medium
                            ?: item.picture_xl
                            ?: ""
                        Artist(
                            id = "dz_art_${item.id ?: System.nanoTime()}",
                            name = name,
                            pictureUrl = pic
                        )
                    }
                }
            }
        } catch (e: Exception) {
            emptyList()
        }

        if (chartArtists.size >= 8) {
            chartArtists.take(limit)
        } else {
            val trackArtists = try {
                getHotPicks(30).map { track ->
                    Artist(
                        id = "trk_${track.id}",
                        name = track.artist,
                        pictureUrl = track.thumbnail
                    )
                }.distinctBy { it.name }.filter { it.name.isNotBlank() }
            } catch (e: Exception) {
                emptyList()
            }
            (chartArtists + trackArtists).distinctBy { it.name }.take(limit)
        }
    }
}
