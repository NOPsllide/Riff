package io.github.nopsllide.riff.core.designsystem.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.PathParser

const val YOUTUBE_PATH = "M21.58 7.19c-.23-.86-.91-1.54-1.77-1.77C18.25 5 12 5 12 5s-6.25 0-7.81.42c-.86.23-1.54.91-1.77 1.77C2 8.75 2 12 2 12s0 3.25.42 4.81c.23.86.91 1.54 1.77 1.77C5.75 19 12 19 12 19s6.25 0 7.81-.42c.86-.23 1.54-.91 1.77-1.77C22 15.25 22 12 22 12s0-3.25-.42-4.81zM10 15V9l5.2 3L10 15z"
const val YTMUSIC_PATH = "M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 18c-4.41 0-8-3.59-8-8s3.59-8 8-8 8 3.59 8 8-3.59 8-8 8zm-2-12.5v9l6-4.5-6-4.5z"
const val SOUNDCLOUD_PATH = "M11.5 14.5c0 .28-.22.5-.5.5s-.5-.22-.5-.5v-4c0-.28.22-.5.5-.5s.5.22.5.5v4zm-2 0c0 .28-.22.5-.5.5s-.5-.22-.5-.5v-3c0-.28.22-.5.5-.5s.5.22.5.5v3zm-2 0c0 .28-.22.5-.5.5s-.5-.22-.5-.5v-2c0-.28.22-.5.5-.5s.5.22.5.5v2zm-2 0c0 .28-.22.5-.5.5s-.5-.22-.5-.5v-1c0-.28.22-.5.5-.5s.5.22.5.5v1zm14.5-.5c0 1.93-1.57 3.5-3.5 3.5H13V9.1c.7-.6 1.6-1.1 2.6-1.1 2.2 0 4.1 1.6 4.4 3.8.3-.2.7-.3 1-.3 1.4 0 2.5 1.1 2.5 2.5z"

@Composable
fun SvgIcon(
    pathData: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    size: Dp = 18.dp,
    tint: Color = LocalContentColor.current,
    viewportSize: Float = 24f
) {
    val composePath = remember(pathData) {
        val androidPath = PathParser.createPathFromPathData(pathData)
        androidPath.asComposePath()
    }

    Canvas(modifier = modifier.size(size)) {
        val scale = this.size.width / viewportSize
        drawContext.canvas.save()
        drawContext.canvas.scale(scale, scale)
        drawPath(composePath, color = tint)
        drawContext.canvas.restore()
    }
}

@Composable
fun YouTubeIcon(
    modifier: Modifier = Modifier,
    size: Dp = 18.dp,
    tint: Color = LocalContentColor.current
) {
    SvgIcon(
        pathData = YOUTUBE_PATH,
        contentDescription = "YouTube",
        modifier = modifier,
        size = size,
        tint = tint
    )
}

@Composable
fun YouTubeMusicIcon(
    modifier: Modifier = Modifier,
    size: Dp = 18.dp,
    tint: Color = LocalContentColor.current
) {
    SvgIcon(
        pathData = YTMUSIC_PATH,
        contentDescription = "YouTube Music",
        modifier = modifier,
        size = size,
        tint = tint
    )
}

@Composable
fun SoundCloudIcon(
    modifier: Modifier = Modifier,
    size: Dp = 18.dp,
    tint: Color = LocalContentColor.current
) {
    SvgIcon(
        pathData = SOUNDCLOUD_PATH,
        contentDescription = "SoundCloud",
        modifier = modifier,
        size = size,
        tint = tint
    )
}
