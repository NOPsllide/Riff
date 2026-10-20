package io.github.nopsllide.riff.core.network

import io.github.nopsllide.riff.core.model.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URLEncoder

@Serializable
private data class ScSearchResponse(
    val collection: List<ScTrackItem> = emptyList()
)

@Serializable
private data class ScTrackItem(
    val id: Long? = null,
    val title: String? = null,
    val duration: Long? = null,
    val artwork_url: String? = null,
    val permalink_url: String? = null,
    val user: ScUser? = null
)

@Serializable
private data class ScUser(
    val username: String? = null
)

class SoundCloudClient(
    private val client: OkHttpClient = OkHttpClient()
) {
    private val json = Json { ignoreUnknownKeys = true }
    private val clientId = "a3dd183a35798ec3a832a110834b1410"

    suspend fun search(query: String, limit: Int = 15): List<Track> = withContext(Dispatchers.IO) {
        val encoded = URLEncoder.encode(query, "UTF-8")
        val url = "https://api-v2.soundcloud.com/search/tracks?q=$encoded&client_id=$clientId&limit=$limit"

        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "Mozilla/5.0")
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext emptyList()
                val body = response.body?.string() ?: return@withContext emptyList()
                val parsed = json.decodeFromString<ScSearchResponse>(body)

                parsed.collection.mapNotNull { item ->
                    val id = item.id ?: return@mapNotNull null
                    val title = item.title ?: return@mapNotNull null
                    val artist = item.user?.username ?: "SoundCloud Artist"
                    val durationMs = item.duration ?: 0L
                    val thumb = item.artwork_url?.replace("-large.", "-t500x500.") ?: ""

                    Track(
                        id = "sc_$id",
                        title = title,
                        artist = artist,
                        duration = durationMs / 1000,
                        thumbnail = thumb,
                        url = item.permalink_url ?: "",
                        platform = "soundcloud"
                    )
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
