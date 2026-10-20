package io.github.nopsllide.riff.core.network

import io.github.nopsllide.riff.core.model.LyricLine
import io.github.nopsllide.riff.core.model.Lyrics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URLEncoder
import java.util.regex.Pattern

@Serializable
private data class LrcLibResponse(
    val id: Long? = null,
    val trackName: String? = null,
    val artistName: String? = null,
    val plainLyrics: String? = null,
    val syncedLyrics: String? = null
)

class LyricsClient(
    private val client: OkHttpClient = OkHttpClient()
) {
    private val json = Json { ignoreUnknownKeys = true }
    private val timePattern = Pattern.compile("\\[(\\d{2}):(\\d{2})\\.(\\d{2,3})\\](.*)")

    suspend fun getLyrics(title: String, artist: String): Lyrics = withContext(Dispatchers.IO) {
        val cleanTitle = sanitize(title)
        val cleanArtist = sanitize(artist)

        val directUrl = "https://lrclib.net/api/get?track_name=" +
                URLEncoder.encode(cleanTitle, "UTF-8") +
                "&artist_name=" +
                URLEncoder.encode(cleanArtist, "UTF-8")

        val directResult = fetchLrc(directUrl)
        if (directResult != null && (!directResult.syncedLyrics.isNullOrBlank() || !directResult.plainLyrics.isNullOrBlank())) {
            return@withContext parseLyricsResponse(directResult)
        }

        val searchUrl = "https://lrclib.net/api/search?q=" +
                URLEncoder.encode("$cleanArtist $cleanTitle".trim(), "UTF-8")

        val searchResult = fetchLrcList(searchUrl)
        val best = searchResult.firstOrNull { !it.syncedLyrics.isNullOrBlank() }
            ?: searchResult.firstOrNull { !it.plainLyrics.isNullOrBlank() }

        if (best != null) {
            return@withContext parseLyricsResponse(best)
        }

        Lyrics()
    }

    private fun fetchLrc(url: String): LrcLibResponse? {
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "Riff-Android/42.3.0")
            .build()

        return try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return null
                val body = response.body?.string() ?: return null
                json.decodeFromString<LrcLibResponse>(body)
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun fetchLrcList(url: String): List<LrcLibResponse> {
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "Riff-Android/42.3.0")
            .build()

        return try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return emptyList()
                val body = response.body?.string() ?: return emptyList()
                json.decodeFromString<List<LrcLibResponse>>(body)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun parseLyricsResponse(resp: LrcLibResponse): Lyrics {
        val synced = resp.syncedLyrics
        if (!synced.isNullOrBlank()) {
            val lines = mutableListOf<LyricLine>()
            synced.lineSequence().forEach { line ->
                val matcher = timePattern.matcher(line.trim())
                if (matcher.matches()) {
                    val min = matcher.group(1)?.toLongOrNull() ?: 0L
                    val sec = matcher.group(2)?.toLongOrNull() ?: 0L
                    val msRaw = matcher.group(3) ?: "0"
                    val ms = if (msRaw.length == 2) msRaw.toLong() * 10 else msRaw.toLong()
                    val totalMs = min * 60 * 1000 + sec * 1000 + ms
                    val text = matcher.group(4)?.trim() ?: ""
                    lines.add(LyricLine(totalMs, text))
                }
            }
            if (lines.isNotEmpty()) {
                return Lyrics(
                    syncedLines = lines,
                    plainText = resp.plainLyrics ?: "",
                    isSynced = true
                )
            }
        }

        return Lyrics(
            syncedLines = emptyList(),
            plainText = resp.plainLyrics ?: "",
            isSynced = false
        )
    }

    private fun sanitize(input: String): String {
        return input.replace(Regex("\\(.*?\\)|\\[.*?\\]"), "")
            .replace(Regex("[^\\w\\s]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }
}
