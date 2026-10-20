package io.github.rootscripts.riff

import android.app.Application
import io.github.rootscripts.riff.core.database.RiffDatabase
import io.github.rootscripts.riff.core.network.ChartsClient
import io.github.rootscripts.riff.core.network.InnertubeClient
import io.github.rootscripts.riff.core.network.LyricsClient
import io.github.rootscripts.riff.core.network.SoundCloudClient
import io.github.rootscripts.riff.core.player.PlaybackManager

class RiffApplication : Application() {
    val innertubeClient by lazy { InnertubeClient() }
    val soundCloudClient by lazy { SoundCloudClient() }
    val chartsClient by lazy { ChartsClient() }
    val lyricsClient by lazy { LyricsClient() }
    val database by lazy { RiffDatabase.getInstance(this) }
    val playbackManager by lazy { PlaybackManager(this, innertubeClient) }

    override fun onCreate() {
        super.onCreate()
    }

    override fun onTerminate() {
        playbackManager.release()
        super.onTerminate()
    }
}
