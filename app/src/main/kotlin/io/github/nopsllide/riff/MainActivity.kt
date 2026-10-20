package io.github.nopsllide.riff

import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.palette.graphics.Palette
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import io.github.nopsllide.riff.core.designsystem.theme.RiffTheme
import io.github.nopsllide.riff.navigation.RiffNavGraph
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as RiffApplication

        setContent {
            val currentTrack by app.playbackManager.currentTrack.collectAsState()
            var extractedColor by remember { mutableStateOf<Color?>(null) }
            var userSelectedColor by remember { mutableStateOf<Color?>(null) }

            LaunchedEffect(currentTrack?.thumbnail) {
                val thumb = currentTrack?.thumbnail
                if (!thumb.isNullOrBlank()) {
                    withContext(Dispatchers.IO) {
                        try {
                            val loader = ImageLoader(this@MainActivity)
                            val request = ImageRequest.Builder(this@MainActivity)
                                .data(thumb)
                                .allowHardware(false)
                                .build()
                            val result = (loader.execute(request) as? SuccessResult)?.drawable
                            val bitmap = (result as? BitmapDrawable)?.bitmap
                            if (bitmap != null) {
                                val palette = Palette.from(bitmap).generate()
                                val dom = palette.dominantSwatch?.rgb
                                    ?: palette.vibrantSwatch?.rgb
                                    ?: palette.mutedSwatch?.rgb
                                if (dom != null) {
                                    extractedColor = Color(dom)
                                }
                            }
                        } catch (e: Exception) {
                            extractedColor = null
                        }
                    }
                } else {
                    extractedColor = null
                }
            }

            val activeColor = userSelectedColor ?: extractedColor

            RiffTheme(customPrimary = activeColor) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    RiffNavGraph(
                        app = app,
                        customPrimaryColor = userSelectedColor,
                        onSelectCustomPrimaryColor = { userSelectedColor = it }
                    )
                }
            }
        }
    }
}
