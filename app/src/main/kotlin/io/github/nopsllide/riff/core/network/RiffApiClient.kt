package io.github.nopsllide.riff.core.network

import io.github.nopsllide.riff.core.model.Artist
import io.github.nopsllide.riff.core.model.Lyrics
import io.github.nopsllide.riff.core.model.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

@Serializable
private data class TrendingResponse(
    val tracks: List<Track> = emptyList(),
    val artists: List<Artist> = emptyList()
)

@Serializable
private data class SearchResponse(
    val tracks: List<Track> = emptyList()
)

@Serializable
private data class StreamResponse(
    val streamUrl: String = "",
    val format: String = "",
    val bitrate: Long = 0L
)

class RiffApiClient(
    private var primaryBaseUrl: String = "http://127.0.0.1:8080/api/v1",
    private var fallbackBaseUrl: String = "http://10.0.2.2:8080/api/v1",
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(2, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .build()
) {
    private val json = Json { ignoreUnknownKeys = true }

    @Volatile
    private var resolvedBaseUrl: String? = null

    fun setBaseUrl(url: String) {
        resolvedBaseUrl = url.trimEnd('/')
    }

    fun getBaseUrl(): String = resolvedBaseUrl ?: primaryBaseUrl

    private fun executeRequest(pathWithQuery: String): String? {
        val active = resolvedBaseUrl
        val candidateUrls = if (active != null) {
            listOf(active + pathWithQuery)
        } else {
            listOf(
                "$primaryBaseUrl$pathWithQuery",
                "$fallbackBaseUrl$pathWithQuery"
            )
        }

        for (url in candidateUrls) {
            try {
                val request = Request.Builder().url(url).build()
                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val body = response.body?.string()
                        if (!body.isNullOrBlank()) {
                            if (resolvedBaseUrl == null) {
                                val base = url.substringBefore("/charts")
                                    .substringBefore("/search")
                                    .substringBefore("/stream")
                                    .substringBefore("/lyrics")
                                resolvedBaseUrl = base
                            }
                            return body
                        }
                    }
                }
            } catch (t: Throwable) {
            }
        }
        return null
    }

    suspend fun getTrending(limit: Int = 20): List<Track> = withContext(Dispatchers.IO) {
        val body = executeRequest("/charts/trending?limit=$limit") ?: return@withContext emptyList()
        try {
            val resp = json.decodeFromString<TrendingResponse>(body)
            resp.tracks
        } catch (t: Throwable) {
            try {
                json.decodeFromString<List<Track>>(body)
            } catch (e: Throwable) {
                emptyList()
            }
        }
    }

    suspend fun getPopularArtists(limit: Int = 20): List<Artist> = withContext(Dispatchers.IO) {
        val body = executeRequest("/charts/artists?limit=$limit") ?: return@withContext emptyList()
        try {
            val resp = json.decodeFromString<TrendingResponse>(body)
            resp.artists
        } catch (t: Throwable) {
            try {
                json.decodeFromString<List<Artist>>(body)
            } catch (e: Throwable) {
                emptyList()
            }
        }
    }

    suspend fun search(query: String, platform: String = "all", limit: Int = 20): List<Track> = withContext(Dispatchers.IO) {
        val encodedQuery = URLEncoder.encode(query, "UTF-8")
        val body = executeRequest("/search?q=$encodedQuery&platform=$platform&limit=$limit") ?: return@withContext emptyList()
        try {
            val resp = json.decodeFromString<SearchResponse>(body)
            resp.tracks
        } catch (t: Throwable) {
            try {
                json.decodeFromString<List<Track>>(body)
            } catch (e: Throwable) {
                emptyList()
            }
        }
    }

    suspend fun getStreamUrl(trackId: String, title: String = "", artist: String = "", platform: String = "riff"): String? = withContext(Dispatchers.IO) {
        val encodedId = URLEncoder.encode(trackId, "UTF-8")
        val encodedTitle = URLEncoder.encode(title, "UTF-8")
        val encodedArtist = URLEncoder.encode(artist, "UTF-8")
        val body = executeRequest("/stream?id=$encodedId&title=$encodedTitle&artist=$encodedArtist&platform=$platform") ?: return@withContext null
        try {
            val resp = json.decodeFromString<StreamResponse>(body)
            if (resp.streamUrl.isNotBlank()) resp.streamUrl else null
        } catch (t: Throwable) {
            null
        }
    }

    suspend fun getLyrics(trackId: String, title: String, artist: String, durationMs: Long = 0L): Lyrics? = withContext(Dispatchers.IO) {
        val encodedId = URLEncoder.encode(trackId, "UTF-8")
        val encodedTitle = URLEncoder.encode(title, "UTF-8")
        val encodedArtist = URLEncoder.encode(artist, "UTF-8")
        val durationSec = durationMs / 1000
        val body = executeRequest("/lyrics?id=$encodedId&title=$encodedTitle&artist=$encodedArtist&duration=$durationSec") ?: return@withContext null
        try {
            json.decodeFromString<Lyrics>(body)
        } catch (t: Throwable) {
            null
        }
    }
}
