package io.github.nopsllide.riff

import android.app.Application
import io.github.nopsllide.riff.core.database.RiffDatabase
import io.github.nopsllide.riff.core.network.RiffApiClient
import io.github.nopsllide.riff.core.player.PlaybackManager

class RiffApplication : Application() {
    val riffApiClient by lazy { RiffApiClient() }
    val database by lazy { RiffDatabase.getInstance(this) }
    val playbackManager by lazy { PlaybackManager(this, riffApiClient) }

    override fun onTerminate() {
        playbackManager.release()
        super.onTerminate()
    }
}
