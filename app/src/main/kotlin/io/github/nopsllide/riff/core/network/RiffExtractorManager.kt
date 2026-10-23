package io.github.nopsllide.riff.core.network

import dev.maxrave.pipepipe.extractor.NewPipe
import dev.maxrave.pipepipe.extractor.ServiceList
import dev.maxrave.pipepipe.extractor.stream.StreamInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object RiffExtractorManager {
    private var isInitialized = false

    fun init() {
        if (!isInitialized) {
            try {
                NewPipe.init(RiffDownloader())
                isInitialized = true
            } catch (t: Throwable) {
            }
        }
    }

    suspend fun resolveYouTubeAudioStream(videoId: String): String? = withContext(Dispatchers.IO) {
        init()
        try {
            val streamInfo = StreamInfo.getInfo(ServiceList.YouTube, "https://music.youtube.com/watch?v=$videoId")
            val audioStreams = streamInfo.audioStreams
            if (!audioStreams.isNullOrEmpty()) {
                val best = audioStreams.maxByOrNull { it.averageBitrate } ?: audioStreams.first()
                val url = best.content
                if (!url.isNullOrBlank()) {
                    return@withContext url
                }
            }
            val videoStreams = streamInfo.videoStreams
            val bestVideo = videoStreams?.firstOrNull { it.content?.isNotBlank() == true }
            bestVideo?.content
        } catch (t: Throwable) {
            try {
                val streamInfo = StreamInfo.getInfo(ServiceList.YouTube, "https://www.youtube.com/watch?v=$videoId")
                val audioStreams = streamInfo.audioStreams
                if (!audioStreams.isNullOrEmpty()) {
                    val best = audioStreams.maxByOrNull { it.averageBitrate } ?: audioStreams.first()
                    val url = best.content
                    if (!url.isNullOrBlank()) {
                        return@withContext url
                    }
                }
                null
            } catch (t2: Throwable) {
                null
            }
        }
    }
}
