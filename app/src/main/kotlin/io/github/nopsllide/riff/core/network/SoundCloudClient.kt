package io.github.nopsllide.riff.core.network

import io.github.nopsllide.riff.core.model.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

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
    val user: ScUser? = null,
    val media: ScMedia? = null
)

@Serializable
private data class ScMedia(
    val transcodings: List<ScTranscoding> = emptyList()
)

@Serializable
private data class ScTranscoding(
    val url: String? = null,
    val duration: Long? = null,
    val format: ScFormat? = null
)

@Serializable
private data class ScFormat(
    val protocol: String? = null,
    val mime_type: String? = null
)

@Serializable
private data class ScUser(
    val username: String? = null
)

class SoundCloudClient(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()
) {
    private val json = Json { ignoreUnknownKeys = true }
    private var activeClientId = "mPlu6PcFn4MUnGqFmsvUDt4oEA1CeA72"

    private suspend fun getValidClientId(): String = withContext(Dispatchers.IO) {
        val current = activeClientId
        if (isClientIdValid(current)) {
            return@withContext current
        }
        val fresh = fetchFreshClientId()
        if (fresh != null) {
            activeClientId = fresh
            return@withContext fresh
        }
        current
    }

    private fun isClientIdValid(cid: String): Boolean {
        val testUrl = "https://api-v2.soundcloud.com/search/tracks?q=test&client_id=$cid&limit=1"
        val req = Request.Builder().url(testUrl).header("User-Agent", "Mozilla/5.0").build()
        return try {
            client.newCall(req).execute().use { it.isSuccessful }
        } catch (e: Exception) {
            false
        }
    }

    private fun fetchFreshClientId(): String? {
        val req = Request.Builder()
            .url("https://soundcloud.com")
            .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
            .build()
        return try {
            val html = client.newCall(req).execute().use { it.body?.string() } ?: return null
            val regex = Regex("https://[a-zA-Z0-9.-]+\\.sndcdn\\.com/assets/[a-zA-Z0-9.-]+\\.js")
            val scripts = regex.findAll(html).map { it.value }.toList()
            for (scriptUrl in scripts.reversed().take(3)) {
                val sReq = Request.Builder().url(scriptUrl).header("User-Agent", "Mozilla/5.0").build()
                val js = client.newCall(sReq).execute().use { it.body?.string() } ?: continue
                val idMatch = Regex("client_id:\"([a-zA-Z0-9]{32})\"").find(js)
                    ?: Regex("client_id=([a-zA-Z0-9]{32})").find(js)
                if (idMatch != null) {
                    return idMatch.groupValues[1]
                }
            }
            null
        } catch (e: Exception) {
            null
        }
    }

    suspend fun search(query: String, limit: Int = 15): List<Track> = withContext(Dispatchers.IO) {
        val encoded = try {
            URLEncoder.encode(query, "UTF-8")
        } catch (e: Exception) {
            query
        }
        val cid = getValidClientId()
        val url = "https://api-v2.soundcloud.com/search/tracks?q=$encoded&client_id=$cid&limit=$limit"

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

    private fun pickBestTranscoding(transcodings: List<ScTranscoding>): ScTranscoding? {
        if (transcodings.isEmpty()) return null
        val nonPreview = transcodings.filter { it.url?.contains("preview") == false }
        val pool = if (nonPreview.isNotEmpty()) nonPreview else transcodings
        return pool.firstOrNull { it.format?.protocol == "progressive" }
            ?: pool.firstOrNull { it.format?.protocol == "hls" }
            ?: pool.firstOrNull()
    }

    suspend fun resolveStreamUrl(trackId: String): String? = withContext(Dispatchers.IO) {
        val cleanId = trackId.removePrefix("sc_")
        val cid = getValidClientId()
        val trackUrl = "https://api-v2.soundcloud.com/tracks/$cleanId?client_id=$cid"
        val req = Request.Builder()
            .url(trackUrl)
            .header("User-Agent", "Mozilla/5.0")
            .build()
        try {
            client.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) return@withContext null
                val body = resp.body?.string() ?: return@withContext null
                val parsed = json.decodeFromString<ScTrackItem>(body)
                val transcodings = parsed.media?.transcodings ?: emptyList()
                val target = pickBestTranscoding(transcodings) ?: return@withContext null
                val tUrl = target.url ?: return@withContext null
                resolveTranscoding(tUrl, cid)
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun searchAndResolve(query: String): String? = withContext(Dispatchers.IO) {
        val encoded = try {
            URLEncoder.encode(query, "UTF-8")
        } catch (e: Exception) {
            query
        }
        val cid = getValidClientId()
        val url = "https://api-v2.soundcloud.com/search/tracks?q=$encoded&client_id=$cid&limit=3"
        val req = Request.Builder()
            .url(url)
            .header("User-Agent", "Mozilla/5.0")
            .build()
        try {
            client.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) return@withContext null
                val body = resp.body?.string() ?: return@withContext null
                val parsed = json.decodeFromString<ScSearchResponse>(body)
                for (item in parsed.collection) {
                    val transcodings = item.media?.transcodings ?: emptyList()
                    val target = pickBestTranscoding(transcodings) ?: continue
                    val tUrl = target.url ?: continue
                    val stream = resolveTranscoding(tUrl, cid)
                    if (stream != null) return@withContext stream
                }
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun resolveTranscoding(transcodingUrl: String, cid: String): String? {
        val url = if (transcodingUrl.contains("?")) "$transcodingUrl&client_id=$cid" else "$transcodingUrl?client_id=$cid"
        val req = Request.Builder()
            .url(url)
            .header("User-Agent", "Mozilla/5.0")
            .build()
        return try {
            client.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) return null
                val body = resp.body?.string() ?: return null
                val obj = json.parseToJsonElement(body).jsonObject
                obj["url"]?.jsonPrimitive?.contentOrNull
            }
        } catch (e: Exception) {
            null
        }
    }
}
