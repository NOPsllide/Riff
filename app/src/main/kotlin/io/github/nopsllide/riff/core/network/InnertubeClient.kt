package io.github.nopsllide.riff.core.network

import io.github.nopsllide.riff.core.model.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

class InnertubeClient(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()
) {
    private val json = Json { ignoreUnknownKeys = true }
    private val mediaTypeJson = "application/json; charset=utf-8".toMediaType()

    suspend fun search(query: String, limit: Int = 20): List<Track> = withContext(Dispatchers.IO) {
        val payload = """
            {
              "context": {
                "client": {
                  "clientName": "WEB_REMIX",
                  "clientVersion": "1.20250101.01.00",
                  "hl": "en",
                  "gl": "US"
                }
              },
              "query": ${json.encodeToString(query)},
              "params": "EgWKAQIIAWoMEA4QChADEAQQCRAF"
            }
        """.trimIndent()

        val request = Request.Builder()
            .url("https://music.youtube.com/youtubei/v1/search?prettyPrint=false")
            .header("Content-Type", "application/json")
            .header("Origin", "https://music.youtube.com")
            .header("Referer", "https://music.youtube.com/")
            .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
            .post(payload.toRequestBody(mediaTypeJson))
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext emptyList()
                val body = response.body?.string() ?: return@withContext emptyList()
                val root = json.parseToJsonElement(body).jsonObject
                parseSearchResults(root).take(limit)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun resolveStreamUrl(videoId: String): String? = withContext(Dispatchers.IO) {
        val payload = """
            {
              "context": {
                "client": {
                  "clientName": "ANDROID",
                  "clientVersion": "19.09.37",
                  "androidSdkVersion": 34,
                  "hl": "en",
                  "gl": "US"
                }
              },
              "videoId": "$videoId"
            }
        """.trimIndent()

        val request = Request.Builder()
            .url("https://www.youtube.com/youtubei/v1/player?prettyPrint=false")
            .header("Content-Type", "application/json")
            .header("User-Agent", "com.google.android.youtube/19.09.37 (Linux; U; Android 14)")
            .post(payload.toRequestBody(mediaTypeJson))
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext null
                val body = response.body?.string() ?: return@withContext null
                val root = json.parseToJsonElement(body).jsonObject
                val streamingData = root["streamingData"]?.jsonObject ?: return@withContext null
                val adaptiveFormats = streamingData["adaptiveFormats"]?.jsonArray ?: return@withContext null

                val audioFormats = adaptiveFormats.mapNotNull { it.jsonObject }
                    .filter {
                        val mime = it["mimeType"]?.jsonPrimitive?.contentOrNull ?: ""
                        mime.startsWith("audio/")
                    }

                val bestOpus = audioFormats.firstOrNull {
                    val mime = it["mimeType"]?.jsonPrimitive?.contentOrNull ?: ""
                    mime.contains("opus") && it["url"] != null
                }

                val chosen = bestOpus ?: audioFormats.firstOrNull { it["url"] != null }
                chosen?.get("url")?.jsonPrimitive?.contentOrNull
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun parseSearchResults(root: JsonObject): List<Track> {
        val tracks = mutableListOf<Track>()
        val contents = root["contents"]?.jsonObject
            ?.get("tabbedSearchResultsRenderer")?.jsonObject
            ?.get("tabs")?.jsonArray?.firstOrNull()?.jsonObject
            ?.get("tabRenderer")?.jsonObject
            ?.get("content")?.jsonObject
            ?.get("sectionListRenderer")?.jsonObject
            ?.get("contents")?.jsonArray ?: return emptyList()

        for (section in contents) {
            val shelf = section.jsonObject["musicShelfRenderer"]?.jsonObject ?: continue
            val items = shelf["contents"]?.jsonArray ?: continue

            for (item in items) {
                val r = item.jsonObject["musicResponsiveListItemRenderer"]?.jsonObject ?: continue
                val videoId = r["playlistItemData"]?.jsonObject?.get("videoId")?.jsonPrimitive?.contentOrNull
                    ?: r["overlay"]?.jsonObject
                        ?.get("musicItemThumbnailOverlayRenderer")?.jsonObject
                        ?.get("content")?.jsonObject
                        ?.get("musicPlayButtonRenderer")?.jsonObject
                        ?.get("playNavigationEndpoint")?.jsonObject
                        ?.get("watchEndpoint")?.jsonObject
                        ?.get("videoId")?.jsonPrimitive?.contentOrNull

                if (videoId.isNullOrBlank() || videoId.length != 11) continue

                val cols = r["flexColumns"]?.jsonArray ?: continue
                val col0 = cols.getOrNull(0)?.jsonObject
                    ?.get("musicResponsiveListItemFlexColumnRenderer")?.jsonObject
                    ?.get("text")?.jsonObject
                    ?.get("runs")?.jsonArray

                val title = col0?.mapNotNull { it.jsonObject["text"]?.jsonPrimitive?.contentOrNull }
                    ?.joinToString("")?.trim() ?: ""

                val col1 = cols.getOrNull(1)?.jsonObject
                    ?.get("musicResponsiveListItemFlexColumnRenderer")?.jsonObject
                    ?.get("text")?.jsonObject
                    ?.get("runs")?.jsonArray

                var artist = ""
                var album = ""
                var durationSec = 180L

                val runTexts = col1?.mapNotNull { it.jsonObject["text"]?.jsonPrimitive?.contentOrNull } ?: emptyList()
                val segments = mutableListOf<String>()
                var currentSegment = StringBuilder()

                for (segmentPart in runTexts) {
                    if (segmentPart == " • ") {
                        segments.add(currentSegment.toString().trim())
                        currentSegment = StringBuilder()
                    } else {
                        currentSegment.append(segmentPart)
                    }
                }
                segments.add(currentSegment.toString().trim())
                val cleanSegments = segments.filter { it.isNotBlank() && !it.equals("song", true) && !it.equals("video", true) }

                if (cleanSegments.isNotEmpty()) {
                    artist = cleanSegments[0].replace(Regex("\\s*-\\s*Topic$", RegexOption.IGNORE_CASE), "").trim()
                }
                if (cleanSegments.size > 1) {
                    val last = cleanSegments.last()
                    val dur = parseDurationString(last)
                    if (dur > 0) {
                        durationSec = dur
                        if (cleanSegments.size > 2) {
                            album = cleanSegments[1]
                        }
                    } else {
                        album = cleanSegments[1]
                    }
                }

                val thumbs = r["thumbnail"]?.jsonObject
                    ?.get("musicThumbnailRenderer")?.jsonObject
                    ?.get("thumbnail")?.jsonObject
                    ?.get("thumbnails")?.jsonArray

                var thumbUrl = thumbs?.lastOrNull()?.jsonObject?.get("url")?.jsonPrimitive?.contentOrNull
                    ?: "https://i.ytimg.com/vi/$videoId/hqdefault.jpg"

                thumbUrl = thumbUrl.replace(Regex("=w\\d+-h\\d+[^&]*$"), "=w544-h544")

                if (title.isNotBlank()) {
                    tracks.add(
                        Track(
                            id = videoId,
                            title = title,
                            artist = if (artist.isNotBlank()) artist else "Unknown Artist",
                            album = album,
                            duration = durationSec,
                            thumbnail = thumbUrl,
                            url = "https://www.youtube.com/watch?v=$videoId",
                            platform = "youtube"
                        )
                    )
                }
            }
        }
        return tracks
    }

    private fun parseDurationString(text: String): Long {
        val parts = text.split(":")
        if (parts.size !in 2..3) return 0L
        return try {
            var acc = 0L
            for (p in parts) {
                acc = acc * 60 + p.toLong()
            }
            acc
        } catch (e: Exception) {
            0L
        }
    }
}
