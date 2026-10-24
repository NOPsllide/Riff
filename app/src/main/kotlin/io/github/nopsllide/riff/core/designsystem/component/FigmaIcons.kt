package io.github.nopsllide.riff.core.designsystem.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.PathParser

const val PATH_AUDIO_LINES = "M2 10V13M6 6V17M10 3V21M14 8V15M18 5V18M22 10V13"
const val PATH_SEARCH = "M21 21L16.66 16.66M19 11C19 15.4183 15.4183 19 11 19C6.58172 19 3 15.4183 3 11C3 6.58172 6.58172 3 11 3C15.4183 3 19 6.58172 19 11Z"
const val PATH_YT_TRIANGLE = "M9.58613 11.5033V4.76916C11.9768 5.89407 13.8283 6.98077 16.0181 8.15236C14.212 9.15401 11.9768 10.2779 9.58613 11.5033Z"
const val PATH_YT_BODY = "M22.9092 1.41994C22.4968 0.876629 21.794 0.453707 21.0457 0.313691C18.8463 -0.103969 5.12518 -0.105157 2.92698 0.313691C2.32693 0.426179 1.7926 0.698079 1.3336 1.12053C-0.600435 2.91561 0.00559941 12.542 0.471777 14.1014C0.667809 14.7763 0.921228 15.2631 1.24038 15.5826C1.65157 16.0051 2.21456 16.2959 2.86123 16.4264C4.67214 16.801 14.0016 17.0104 21.0074 16.4826C21.6529 16.3701 22.2243 16.0699 22.6749 15.6295C24.4631 13.8416 24.3412 3.67456 22.9092 1.41994Z"
const val PATH_YTM_CIRCLE = "M12 21.25C17.1086 21.25 21.25 17.1086 21.25 12C21.25 6.89125 17.1086 2.75 12 2.75C6.89125 2.75 2.75 6.89125 2.75 12C2.75 17.1086 6.89125 21.25 12 21.25Z"
const val PATH_YTM_TRIANGLE = "M9.25 16.25V7.75L16.75 12L9.25 16.25Z"
const val PATH_SC_CLOUD = "M19.5 18.75H12C11.55 18.75 11.25 18.45 11.25 18V6.075C11.25 5.7 11.55 5.4 11.925 5.325C12.225 5.25 12.525 5.25 12.75 5.25C15.15 5.25 17.55 7.275 18.375 9.9C18.75 9.825 19.125 9.75 19.5 9.75C21.975 9.75 24 11.775 24 14.25C24 16.725 21.975 18.75 19.5 18.75Z"
const val PATH_SC_BARS = "M9.75 18.75C9.3 18.75 9 18.45 9 18V7.5C9 7.05 9.3 6.75 9.75 6.75C10.2 6.75 10.5 7.05 10.5 7.5V18C10.5 18.45 10.2 18.75 9.75 18.75ZM7.5 18C7.05 18 6.75 17.7 6.75 17.25V12C6.75 11.55 7.05 11.25 7.5 11.25C7.95 11.25 8.25 11.55 8.25 12V17.25C8.25 17.7 7.95 18 7.5 18ZM5.25 18.75C4.8 18.75 4.5 18.45 4.5 18V9.75C4.5 9.3 4.8 9 5.25 9C5.7 9 6 9.3 6 9.75V18C6 18.45 5.7 18.75 5.25 18.75ZM3 18C2.55 18 2.25 17.7 2.25 17.25V12.75C2.25 12.3 2.55 12 3 12C3.45 12 3.75 12.3 3.75 12.75V17.25C3.75 17.7 3.45 18 3 18ZM0.75 17.25C0.3 17.25 0 16.95 0 16.5V13.5C0 13.05 0.3 12.75 0.75 12.75C1.2 12.75 1.5 13.05 1.5 13.5V16.5C1.5 16.95 1.2 17.25 0.75 17.25Z"
const val PATH_FIGMA_PLAY = "M5.02722 4.99998C5.02712 4.64807 5.12037 4.30237 5.29757 3.99777C5.47477 3.69318 5.72964 3.44047 6.03644 3.26518C6.34324 3.08988 6.69112 2.99821 7.04494 2.9994C7.39876 3.0006 7.746 3.09462 8.0516 3.27198L20.1139 10.27C20.4183 10.4457 20.6711 10.6977 20.8468 11.001C21.0226 11.3042 21.1153 11.6481 21.1156 11.9981C21.1159 12.3482 21.0238 12.6922 20.8486 12.9957C20.6733 13.2993 20.421 13.5518 20.1169 13.728L8.0516 20.728C7.746 20.9053 7.39876 20.9994 7.04494 21.0006C6.69112 21.0018 6.34324 20.9101 6.03644 20.7348C5.72964 20.5595 5.47477 20.3068 5.29757 20.0022C5.12037 19.6976 5.02712 19.3519 5.02722 19V4.99998Z"
const val PATH_SKIP_FORWARD = "M21 4.00002V20M6.029 4.28502C5.72551 4.10292 5.37913 4.00462 5.02523 4.00016C4.67133 3.99569 4.32259 4.08522 4.0146 4.2596C3.70661 4.43398 3.45041 4.68697 3.27216 4.99274C3.09391 5.2985 3 5.64609 3 6.00002V18C3 18.3539 3.09391 18.7015 3.27216 19.0073C3.45041 19.3131 3.70661 19.5661 4.0146 19.7404C4.32259 19.9148 4.67133 20.0043 5.02523 19.9999C5.37913 19.9954 5.72551 19.8971 6.029 19.715L16.026 13.717C16.3228 13.5397 16.5685 13.2885 16.7393 12.9879C16.91 12.6873 16.9999 12.3476 17.0002 12.0019C17.0005 11.6562 16.9112 11.3163 16.741 11.0154C16.5708 10.7145 16.3255 10.4628 16.029 10.285L6.029 4.28502Z"
const val PATH_TELESCOPE = "M13.42 16.6573L5.18 18.4146C4.86016 18.484 4.52585 18.4247 4.24938 18.2496C3.97292 18.0744 3.77652 17.7975 3.70267 17.4786L2.98667 14.612C2.90249 14.2666 2.95029 13.9022 3.12071 13.5902C3.29113 13.2782 3.57189 13.0411 3.908 12.9253L21.9133 7.00528M18.08 15.6626L23.856 14.4306M21.3333 28L17.1933 19.72M8.21067 11.5106L9.696 17.452M10.6667 28L14.8067 19.72M21.98 7.91995C21.8086 7.23393 21.9167 6.50792 22.2805 5.90158C22.6443 5.29524 23.234 4.85822 23.92 4.68662L25.3733 4.32395C25.7162 4.23843 26.0789 4.29248 26.3819 4.47423C26.6849 4.65598 26.9034 4.95057 26.9893 5.29328L29.0093 13.3733C29.0953 13.7163 29.0414 14.0794 28.8596 14.3827C28.6778 14.686 28.383 14.9047 28.04 14.9906L26.5867 15.3533C25.9006 15.5247 25.1746 15.4166 24.5683 15.0528C23.962 14.689 23.5249 14.0993 23.3533 13.4133L21.98 7.91995ZM18.6667 17.3333C18.6667 18.806 17.4728 20 16 20C14.5272 20 13.3333 18.806 13.3333 17.3333C13.3333 15.8605 14.5272 14.6666 16 14.6666C17.4728 14.6666 18.6667 15.8605 18.6667 17.3333Z"
const val PATH_HEART_MSG = "M3.98933 21.7894C4.18539 22.2839 4.22903 22.8258 4.11467 23.3454L2.69467 27.732C2.64891 27.9545 2.66074 28.185 2.72903 28.4016C2.79733 28.6182 2.91982 28.8137 3.08489 28.9697C3.24996 29.1257 3.45215 29.237 3.67227 29.2929C3.89239 29.3488 4.12315 29.3476 4.34267 29.2894L8.89333 27.9587C9.38362 27.8615 9.89137 27.904 10.3587 28.0814C13.2058 29.411 16.4312 29.6923 19.4655 28.8757C22.4999 28.059 25.1484 26.1969 26.9436 23.6179C28.7388 21.0388 29.5655 17.9086 29.2777 14.7794C28.99 11.6503 27.6063 8.72333 25.3708 6.51497C23.1352 4.3066 20.1916 2.95876 17.0592 2.70923C13.9268 2.45971 10.8068 3.32455 8.24992 5.15116C5.693 6.97777 3.86337 9.64876 3.08386 12.6929C2.30434 15.737 2.62503 18.9586 3.98933 21.7894ZM10.4373 17.4267C9.72258 16.679 9.32523 15.6836 9.32869 14.6492C9.33215 13.6149 9.73615 12.6221 10.4559 11.8792C11.1756 11.1364 12.1552 10.7012 13.1889 10.6651C14.2226 10.6289 15.2301 10.9946 16 11.6854C16.5744 11.1698 17.2856 10.8314 18.048 10.711C18.8103 10.5906 19.5913 10.6933 20.2966 11.0068C21.0019 11.3202 21.6014 11.831 22.0229 12.4776C22.4444 13.1242 22.6698 13.8789 22.672 14.6507C22.6752 15.6851 22.2776 16.6805 21.5627 17.428L16.9667 22.2547C16.842 22.3857 16.692 22.49 16.5258 22.5613C16.3596 22.6325 16.1806 22.6692 15.9997 22.669C15.8189 22.6689 15.6399 22.632 15.4738 22.5605C15.3077 22.4891 15.1578 22.3846 15.0333 22.2534L10.4373 17.4267Z"
const val PATH_FOLDER_OPEN = "M8 18.6667L10 14.8C10.2174 14.3682 10.5482 14.0036 10.9568 13.7453C11.3655 13.4869 11.8367 13.3445 12.32 13.3333H26.6667M26.6667 13.3333C27.074 13.3326 27.4762 13.4253 27.8422 13.6041C28.2082 13.783 28.5283 14.0433 28.7781 14.3652C29.0278 14.687 29.2005 15.0618 29.2829 15.4607C29.3653 15.8597 29.3552 16.2722 29.2533 16.6667L27.2 24.6667C27.0514 25.2421 26.7149 25.7514 26.2439 26.1137C25.7728 26.4761 25.1943 26.6707 24.6 26.6667H5.33333C4.62609 26.6667 3.94781 26.3857 3.44771 25.8856C2.94762 25.3855 2.66666 24.7073 2.66666 24V6.66667C2.66666 5.95943 2.94762 5.28115 3.44771 4.78105C3.94781 4.28096 4.62609 4.00001 5.33333 4.00001H10.5333C10.9793 3.99563 11.4193 4.10319 11.8129 4.31283C12.2066 4.52248 12.5414 4.82751 12.7867 5.20001L13.8667 6.80001C14.1095 7.16871 14.44 7.47137 14.8287 7.68081C15.2173 7.89025 15.6519 7.99993 16.0933 8.00001H24C24.7072 8.00001 25.3855 8.28096 25.8856 8.78105C26.3857 9.28115 26.6667 9.95943 26.6667 10.6667V13.3333Z"
const val PATH_CLOCK = "M16 7.99996V16L21.3333 18.6666M29.3333 16C29.3333 23.3638 23.3638 29.3333 16 29.3333C8.63621 29.3333 2.66667 23.3638 2.66667 16C2.66667 8.63616 8.63621 2.66663 16 2.66663C23.3638 2.66663 29.3333 8.63616 29.3333 16Z"
const val PATH_COG = "M14.6667 13.6933L9.33332 4.45329M14.6667 18.3066L9.33332 27.5466M16 29.3333V26.6666M16 26.6666C21.891 26.6666 26.6667 21.891 26.6667 16C26.6667 10.1089 21.891 5.33329 16 5.33329M16 26.6666C10.109 26.6666 5.33332 21.891 5.33332 16M16 2.66663V5.33329M16 5.33329C10.109 5.33329 5.33332 10.1089 5.33332 16M18.6667 16H29.3333M18.6667 16C18.6667 17.4727 17.4727 18.6666 16 18.6666C14.5272 18.6666 13.3333 17.4727 13.3333 16C13.3333 14.5272 14.5272 13.3333 16 13.3333C17.4727 13.3333 18.6667 14.5272 18.6667 16ZM22.6667 27.5466L21.3333 25.24M22.6667 4.45329L21.3333 6.75996M2.66666 16H5.33332M27.5467 22.6666L25.24 21.3333M27.5467 9.33329L25.24 10.6666M4.45332 22.6666L6.75999 21.3333M4.45332 9.33329L6.75999 10.6666"
const val PATH_LUCIDE_X = "M18 6L6 18M6 6L18 18"
const val PATH_LUCIDE_HEART = "M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"
const val PATH_LUCIDE_SHUFFLE = "M16 3h5v5M4 20L21 3M21 16v5h-5M15 15l6 6M4 4l5 5"
const val PATH_LUCIDE_SKIP_BACK = "M19 20L9 12l10-8v16zM5 19V5"
const val PATH_LUCIDE_REPEAT = "m17 2 4 4-4 4M3 11v-1a4 4 0 0 1 4-4h14M7 22l-4-4 4-4M21 13v1a4 4 0 0 1-4 4H3"
const val PATH_LUCIDE_PAUSE = "M6 4h4v16H6zM14 4h4v16h-4z"
const val PATH_LUCIDE_QUEUE = "M21 15V6M18.5 18a2.5 2.5 0 1 0 0-5 2.5 2.5 0 0 0 0 5ZM12 12H3M16 6H3M12 18H3"

@Composable
fun FigmaStrokedIcon(
    pathData: String,
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    tint: Color = Color.White,
    strokeWidth: Float = 2f,
    viewportSize: Float = 24f
) {
    val composePath = remember(pathData) {
        PathParser.createPathFromPathData(pathData).asComposePath()
    }

    Canvas(modifier = modifier.size(size)) {
        val scale = this.size.width / viewportSize
        drawContext.canvas.save()
        drawContext.canvas.scale(scale, scale)
        drawPath(
            path = composePath,
            color = tint,
            style = Stroke(
                width = strokeWidth,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
        drawContext.canvas.restore()
    }
}

@Composable
fun FigmaFilledIcon(
    pathData: String,
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    tint: Color = Color.White,
    viewportWidth: Float = 24f,
    viewportHeight: Float = 24f
) {
    val composePath = remember(pathData) {
        PathParser.createPathFromPathData(pathData).asComposePath()
    }

    Canvas(modifier = modifier.size(size)) {
        val scaleX = this.size.width / viewportWidth
        val scaleY = this.size.height / viewportHeight
        drawContext.canvas.save()
        drawContext.canvas.scale(scaleX, scaleY)
        drawPath(
            path = composePath,
            color = tint,
            style = Fill
        )
        drawContext.canvas.restore()
    }
}

@Composable
fun AudioLinesLogoIcon(modifier: Modifier = Modifier, size: Dp = 24.dp, tint: Color = Color.White) {
    FigmaStrokedIcon(pathData = PATH_AUDIO_LINES, modifier = modifier, size = size, tint = tint, strokeWidth = 2f, viewportSize = 24f)
}

@Composable
fun LucideSearchIcon(modifier: Modifier = Modifier, size: Dp = 20.dp, tint: Color = Color(0xFF8E8E93)) {
    FigmaStrokedIcon(pathData = PATH_SEARCH, modifier = modifier, size = size, tint = tint, strokeWidth = 2f, viewportSize = 24f)
}

@Composable
fun FigmaYouTubeLogoIcon(modifier: Modifier = Modifier, size: Dp = 22.dp, tint: Color = Color(0xFF8E8E93)) {
    val composeTriangle = remember { PathParser.createPathFromPathData(PATH_YT_TRIANGLE).asComposePath() }
    val composeBody = remember { PathParser.createPathFromPathData(PATH_YT_BODY).asComposePath() }

    Canvas(modifier = modifier.size(size)) {
        val scaleX = this.size.width / 24f
        val scaleY = this.size.height / 17f
        drawContext.canvas.save()
        drawContext.canvas.scale(scaleX, scaleY)
        drawPath(composeBody, color = tint, style = Fill)
        drawPath(composeTriangle, color = Color(0xFF181818), style = Fill)
        drawContext.canvas.restore()
    }
}

@Composable
fun FigmaYouTubeMusicLogoIcon(modifier: Modifier = Modifier, size: Dp = 20.dp, tint: Color = Color(0xFF8E8E93)) {
    val circlePath = remember { PathParser.createPathFromPathData(PATH_YTM_CIRCLE).asComposePath() }
    val trianglePath = remember { PathParser.createPathFromPathData(PATH_YTM_TRIANGLE).asComposePath() }

    Canvas(modifier = modifier.size(size)) {
        val scale = this.size.width / 24f
        drawContext.canvas.save()
        drawContext.canvas.scale(scale, scale)
        drawPath(circlePath, color = tint, style = Stroke(width = 2f, cap = StrokeCap.Round, join = StrokeJoin.Round))
        drawPath(trianglePath, color = tint, style = Stroke(width = 2f, cap = StrokeCap.Round, join = StrokeJoin.Round))
        drawContext.canvas.restore()
    }
}

@Composable
fun FigmaSoundCloudLogoIcon(modifier: Modifier = Modifier, size: Dp = 20.dp, tint: Color = Color(0xFF8E8E93)) {
    val cloudPath = remember { PathParser.createPathFromPathData(PATH_SC_CLOUD).asComposePath() }
    val barsPath = remember { PathParser.createPathFromPathData(PATH_SC_BARS).asComposePath() }

    Canvas(modifier = modifier.size(size)) {
        val scale = this.size.width / 24f
        drawContext.canvas.save()
        drawContext.canvas.scale(scale, scale)
        drawPath(cloudPath, color = tint, style = Fill)
        drawPath(barsPath, color = tint, style = Fill)
        drawContext.canvas.restore()
    }
}

@Composable
fun FigmaPlayActionIcon(modifier: Modifier = Modifier, size: Dp = 20.dp, tint: Color = Color.Black) {
    FigmaFilledIcon(pathData = PATH_FIGMA_PLAY, modifier = modifier, size = size, tint = tint, viewportWidth = 25f, viewportHeight = 24f)
}

@Composable
fun FigmaSkipForwardActionIcon(modifier: Modifier = Modifier, size: Dp = 22.dp, tint: Color = Color.White) {
    FigmaStrokedIcon(pathData = PATH_SKIP_FORWARD, modifier = modifier, size = size, tint = tint, strokeWidth = 2f, viewportSize = 24f)
}

@Composable
fun LucideTelescopeIcon(modifier: Modifier = Modifier, size: Dp = 26.dp, tint: Color = Color.White) {
    FigmaStrokedIcon(pathData = PATH_TELESCOPE, modifier = modifier, size = size, tint = tint, strokeWidth = 2f, viewportSize = 32f)
}

@Composable
fun LucideMessageCircleHeartIcon(modifier: Modifier = Modifier, size: Dp = 26.dp, tint: Color = Color(0xFF8E8E93)) {
    FigmaStrokedIcon(pathData = PATH_HEART_MSG, modifier = modifier, size = size, tint = tint, strokeWidth = 2f, viewportSize = 32f)
}

@Composable
fun LucideFolderOpenIcon(modifier: Modifier = Modifier, size: Dp = 26.dp, tint: Color = Color(0xFF8E8E93)) {
    FigmaStrokedIcon(pathData = PATH_FOLDER_OPEN, modifier = modifier, size = size, tint = tint, strokeWidth = 2f, viewportSize = 32f)
}

@Composable
fun LucideClockIcon(modifier: Modifier = Modifier, size: Dp = 26.dp, tint: Color = Color(0xFF8E8E93)) {
    FigmaStrokedIcon(pathData = PATH_CLOCK, modifier = modifier, size = size, tint = tint, strokeWidth = 2f, viewportSize = 32f)
}

@Composable
fun LucideCogIcon(modifier: Modifier = Modifier, size: Dp = 26.dp, tint: Color = Color(0xFF8E8E93)) {
    FigmaStrokedIcon(pathData = PATH_COG, modifier = modifier, size = size, tint = tint, strokeWidth = 2f, viewportSize = 32f)
}

@Composable
fun LucideXIcon(modifier: Modifier = Modifier, size: Dp = 24.dp, tint: Color = Color.White) {
    FigmaStrokedIcon(pathData = PATH_LUCIDE_X, modifier = modifier, size = size, tint = tint, strokeWidth = 2f, viewportSize = 24f)
}

@Composable
fun LucideHeartOutlinedIcon(modifier: Modifier = Modifier, size: Dp = 34.dp, tint: Color = Color(0xFF8E8E93)) {
    FigmaStrokedIcon(pathData = PATH_LUCIDE_HEART, modifier = modifier, size = size, tint = tint, strokeWidth = 2f, viewportSize = 24f)
}

@Composable
fun LucideShuffleIcon(modifier: Modifier = Modifier, size: Dp = 28.dp, tint: Color = Color.White) {
    FigmaStrokedIcon(pathData = PATH_LUCIDE_SHUFFLE, modifier = modifier, size = size, tint = tint, strokeWidth = 2f, viewportSize = 24f)
}

@Composable
fun LucideSkipBackIcon(modifier: Modifier = Modifier, size: Dp = 28.dp, tint: Color = Color.White) {
    FigmaStrokedIcon(pathData = PATH_LUCIDE_SKIP_BACK, modifier = modifier, size = size, tint = tint, strokeWidth = 2f, viewportSize = 24f)
}

@Composable
fun LucideSkipForwardIcon(modifier: Modifier = Modifier, size: Dp = 28.dp, tint: Color = Color.White) {
    FigmaStrokedIcon(pathData = PATH_SKIP_FORWARD, modifier = modifier, size = size, tint = tint, strokeWidth = 2f, viewportSize = 24f)
}

@Composable
fun LucideRepeatIcon(modifier: Modifier = Modifier, size: Dp = 28.dp, tint: Color = Color.White) {
    FigmaStrokedIcon(pathData = PATH_LUCIDE_REPEAT, modifier = modifier, size = size, tint = tint, strokeWidth = 2f, viewportSize = 24f)
}

@Composable
fun LucidePauseIcon(modifier: Modifier = Modifier, size: Dp = 24.dp, tint: Color = Color.Black) {
    FigmaFilledIcon(pathData = PATH_LUCIDE_PAUSE, modifier = modifier, size = size, tint = tint, viewportWidth = 24f, viewportHeight = 24f)
}

@Composable
fun LucideQueueIcon(modifier: Modifier = Modifier, size: Dp = 24.dp, tint: Color = Color.White) {
    FigmaStrokedIcon(pathData = PATH_LUCIDE_QUEUE, modifier = modifier, size = size, tint = tint, strokeWidth = 2f, viewportSize = 24f)
}
