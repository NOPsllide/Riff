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
        val iosUrl = fetchFromClient(
            videoId = videoId,
            clientName = "IOS",
            clientVersion = "19.45.4",
            deviceModel = "iPhone16,2",
            osName = "iOS",
            osVersion = "18.1.0.22B83",
            userAgent = "com.google.ios.youtube/19.45.4 (iPhone16,2; U; CPU iOS 18_1 like Mac OS X; en_US)"
        )
        if (iosUrl != null) return@withContext iosUrl

        val vrUrl = fetchFromClient(
            videoId = videoId,
            clientName = "ANDROID_VR",
            clientVersion = "1.61.48",
            deviceModel = "Quest 3",
            osName = "Android",
            osVersion = "12",
            userAgent = "Mozilla/5.0 (Linux; Android 12; Quest 3) AppleWebKit/537.36 (KHTML, like Gecko) OculusBrowser/34.0.0.15.39.574676104 SamsungBrowser/4.0 Chrome/122.0.6261.137 Mobile VR Safari/537.36"
        )
        vrUrl
    }

    private fun fetchFromClient(
        videoId: String,
        clientName: String,
        clientVersion: String,
        deviceModel: String,
        osName: String,
        osVersion: String,
        userAgent: String
    ): String? {
        val payload = """
            {
              "context": {
                "client": {
                  "clientName": "$clientName",
                  "clientVersion": "$clientVersion",
                  "deviceModel": "$deviceModel",
                  "osName": "$osName",
                  "osVersion": "$osVersion",
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
            .header("User-Agent", userAgent)
            .post(payload.toRequestBody(mediaTypeJson))
            .build()

        return try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return null
                val body = response.body?.string() ?: return null
                val root = json.parseToJsonElement(body).jsonObject
                val streamingData = root["streamingData"]?.jsonObject ?: return null
                val adaptiveFormats = streamingData["adaptiveFormats"]?.jsonArray ?: return null

                val audioFormats = adaptiveFormats.mapNotNull { it.jsonObject }
                    .filter {
                        val mime = it["mimeType"]?.jsonPrimitive?.contentOrNull ?: ""
                        mime.startsWith("audio/") && it["url"]?.jsonPrimitive?.contentOrNull?.isNotBlank() == true
                    }

                val best = audioFormats.maxByOrNull {
                    it["bitrate"]?.jsonPrimitive?.contentOrNull?.toLongOrNull() ?: 0L
                } ?: audioFormats.firstOrNull()

                best?.get("url")?.jsonPrimitive?.contentOrNull
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

                val artist = col1?.firstOrNull()?.jsonObject
                    ?.get("text")?.jsonPrimitive?.contentOrNull?.trim() ?: "Unknown Artist"

                val album = if (col1 != null && col1.size > 2) {
                    col1[2].jsonObject["text"]?.jsonPrimitive?.contentOrNull?.trim() ?: ""
                } else ""

                val thumb = "https://i.ytimg.com/vi/$videoId/hqdefault.jpg"

                if (title.isNotBlank()) {
                    tracks.add(
                        Track(
                            id = videoId,
                            title = title,
                            artist = artist,
                            album = album,
                            duration = 0L,
                            thumbnail = thumb,
                            url = "https://www.youtube.com/watch?v=$videoId",
                            platform = "youtube"
                        )
                    )
                }
            }
        }
        return tracks
    }
}
