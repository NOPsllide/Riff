package io.github.rootscripts.riff.core.designsystem.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val RiffDarkColorScheme = darkColorScheme(
    primary = RiffPrimary,
    onPrimary = RiffOnPrimary,
    primaryContainer = RiffPrimaryContainer,
    onPrimaryContainer = RiffOnPrimaryContainer,
    secondary = RiffSecondary,
    onSecondary = RiffOnSecondary,
    secondaryContainer = RiffSecondaryContainer,
    onSecondaryContainer = RiffOnSecondaryContainer,
    tertiary = RiffTertiary,
    onTertiary = RiffOnTertiary,
    tertiaryContainer = RiffTertiaryContainer,
    onTertiaryContainer = RiffOnTertiaryContainer,
    background = RiffBackground,
    onBackground = RiffOnSurface,
    surface = RiffSurface,
    onSurface = RiffOnSurface,
    surfaceVariant = RiffSurfaceContainerHigh,
    onSurfaceVariant = RiffOnSurfaceVariant,
    outline = RiffOutline,
    outlineVariant = RiffOutlineVariant,
    error = RiffError,
    onError = RiffOnError
)

val LocalDynamicPrimary = staticCompositionLocalOf { RiffPrimary }

@Composable
fun RiffTheme(
    dynamicColor: Boolean = true,
    customPrimary: Color? = null,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val baseScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            dynamicDarkColorScheme(context)
        }
        else -> RiffDarkColorScheme
    }

    val activeScheme = if (customPrimary != null) {
        baseScheme.copy(
            primary = customPrimary,
            primaryContainer = customPrimary.copy(alpha = 0.25f),
            onPrimaryContainer = customPrimary
        )
    } else {
        baseScheme
    }

    val effectivePrimary = customPrimary ?: activeScheme.primary

    CompositionLocalProvider(LocalDynamicPrimary provides effectivePrimary) {
        MaterialTheme(
            colorScheme = activeScheme,
            typography = RiffTypography,
            shapes = RiffShapes,
            content = content
        )
    }
}
