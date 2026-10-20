package io.github.nopsllide.riff.core.designsystem.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun M3ExpressiveBlob(
    color: Color,
    modifier: Modifier = Modifier,
    alpha: Float = 0.35f
) {
    val transition = rememberInfiniteTransition(label = "m3Blob")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    Canvas(modifier = modifier) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val baseR = (size.width.coerceAtMost(size.height) / 2f) * 0.82f

        val path = Path()
        val count = 180
        val step = (2f * PI.toFloat()) / count

        for (i in 0 until count) {
            val theta = i * step
            val r = baseR +
                    sin(theta * 3f + phase) * (baseR * 0.12f) +
                    cos(theta * 5f - phase * 0.7f) * (baseR * 0.08f)
            val px = cx + cos(theta) * r
            val py = cy + sin(theta) * r

            if (i == 0) {
                path.moveTo(px, py)
            } else {
                path.lineTo(px, py)
            }
        }
        path.close()

        drawPath(
            path = path,
            color = color.copy(alpha = alpha)
        )
    }
}
