package io.github.rootscripts.riff.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.rootscripts.riff.core.designsystem.theme.RiffSurfaceContainer

val RiffThemeSwatches = listOf(
    Color(0xFFB69DF8),
    Color(0xFF7AA7FF),
    Color(0xFF56C5C5),
    Color(0xFF6DD58C),
    Color(0xFFF5B83D),
    Color(0xFFF28B82),
    Color(0xFFF48FB1)
)

@Composable
fun SettingsScreen(
    customPrimary: Color?,
    onSelectPrimary: (Color?) -> Unit,
    isWavyEnabled: Boolean,
    onToggleWavy: (Boolean) -> Unit,
    isBassPulseEnabled: Boolean,
    onToggleBassPulse: (Boolean) -> Unit,
    onOpenSleepTimer: () -> Unit,
    onClearCache: (() -> Unit)? = null,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf("theme") }

    var uiOpacity by remember { mutableFloatStateOf(100f) }
    var fontScale by remember { mutableFloatStateOf(100f) }
    var cornerRadius by remember { mutableFloatStateOf(16f) }
    var showBarWave by remember { mutableStateOf(true) }
    var showShapedCover by remember { mutableStateOf(true) }
    var tvkVisOpacity by remember { mutableFloatStateOf(85f) }
    var showArtistsInSearch by remember { mutableStateOf(true) }
    var smoothPresetTransitions by remember { mutableStateOf(true) }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(vertical = 16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                Text(
                    text = "SETTINGS",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.size(48.dp))
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CategoryChip("Theme", selectedCategory == "theme", Icons.Filled.Palette) {
                    selectedCategory = "theme"
                }
                CategoryChip("Interface", selectedCategory == "interface", Icons.Filled.Widgets) {
                    selectedCategory = "interface"
                }
                CategoryChip("Player", selectedCategory == "player", Icons.Filled.Tune) {
                    selectedCategory = "player"
                }
                CategoryChip("Storage", selectedCategory == "storage", Icons.Filled.Storage) {
                    selectedCategory = "storage"
                }
                CategoryChip("About", selectedCategory == "about", Icons.Filled.Info) {
                    selectedCategory = "about"
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                when (selectedCategory) {
                    "theme" -> {
                        Text(
                            text = "Accent Color (Material 3 Expressive)",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Card(
                            colors = CardDefaults.cardColors(containerColor = RiffSurfaceContainer),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onSelectPrimary(null) }
                                        .padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Dynamic (Extracted from Album Art)",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    if (customPrimary == null) {
                                        Icon(
                                            imageVector = Icons.Filled.Check,
                                            contentDescription = "Active",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    RiffThemeSwatches.forEach { color ->
                                        val isSelected = customPrimary == color
                                        Box(
                                            modifier = Modifier
                                                .size(38.dp)
                                                .clip(CircleShape)
                                                .background(color)
                                                .border(
                                                    width = if (isSelected) 2.5.dp else 0.dp,
                                                    color = if (isSelected) Color.White else Color.Transparent,
                                                    shape = CircleShape
                                                )
                                                .clickable { onSelectPrimary(color) },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (isSelected) {
                                                Icon(
                                                    imageVector = Icons.Filled.Check,
                                                    contentDescription = "Selected",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = "Visual Effects",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Card(
                            colors = CardDefaults.cardColors(containerColor = RiffSurfaceContainer),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                SettingSwitchRow(
                                    title = "Wavy Progress Bar",
                                    subtitle = "Material You squiggly scrubber wave animation",
                                    checked = isWavyEnabled,
                                    onCheckedChange = onToggleWavy
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                SettingSwitchRow(
                                    title = "Bass Pulse Animation",
                                    subtitle = "Cover art pulses subtly (1.035x) with music rhythm",
                                    checked = isBassPulseEnabled,
                                    onCheckedChange = onToggleBassPulse
                                )
                            }
                        }
                    }

                    "interface" -> {
                        Text(
                            text = "Display & Sizing",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Card(
                            colors = CardDefaults.cardColors(containerColor = RiffSurfaceContainer),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                SettingSliderRow(
                                    title = "Corner Radius",
                                    valueText = "${cornerRadius.toInt()}px",
                                    value = cornerRadius,
                                    range = 4f..32f,
                                    onValueChange = { cornerRadius = it }
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                SettingSliderRow(
                                    title = "Interface Opacity",
                                    valueText = "${uiOpacity.toInt()}%",
                                    value = uiOpacity,
                                    range = 40f..100f,
                                    onValueChange = { uiOpacity = it }
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                SettingSliderRow(
                                    title = "Font Scale",
                                    valueText = "${fontScale.toInt()}%",
                                    value = fontScale,
                                    range = 80f..130f,
                                    onValueChange = { fontScale = it }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = "Playbar & Visualizers",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Card(
                            colors = CardDefaults.cardColors(containerColor = RiffSurfaceContainer),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                SettingSwitchRow(
                                    title = "Show wave visualizer in bottom bar",
                                    subtitle = "12-band mini equalizer in playback bar",
                                    checked = showBarWave,
                                    onCheckedChange = { showBarWave = it }
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                SettingSwitchRow(
                                    title = "Shaped cover in bottom bar",
                                    subtitle = "Morphing Material 3 shape around album cover",
                                    checked = showShapedCover,
                                    onCheckedChange = { showShapedCover = it }
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                SettingSliderRow(
                                    title = "TV Karaoke visualizer opacity",
                                    valueText = "${tvkVisOpacity.toInt()}%",
                                    value = tvkVisOpacity,
                                    range = 10f..100f,
                                    onValueChange = { tvkVisOpacity = it }
                                )
                            }
                        }
                    }

                    "player" -> {
                        Text(
                            text = "Playback & Engine",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Card(
                            colors = CardDefaults.cardColors(containerColor = RiffSurfaceContainer),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onOpenSleepTimer() }
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Filled.Timer,
                                            contentDescription = "Sleep Timer",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.size(12.dp))
                                        Text(
                                            text = "Sleep Timer",
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                SettingSwitchRow(
                                    title = "Show artists in search",
                                    subtitle = "Include artist matches in YouTube and SoundCloud queries",
                                    checked = showArtistsInSearch,
                                    onCheckedChange = { showArtistsInSearch = it }
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                SettingSwitchRow(
                                    title = "Smooth preset transitions",
                                    subtitle = "Smooth volume ramps when switching audio presets",
                                    checked = smoothPresetTransitions,
                                    onCheckedChange = { smoothPresetTransitions = it }
                                )
                            }
                        }
                    }

                    "storage" -> {
                        Text(
                            text = "Storage & Cache",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Card(
                            colors = CardDefaults.cardColors(containerColor = RiffSurfaceContainer),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "Clear Cache & Temp Files",
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "Clears album thumbnails and temp streams",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    OutlinedButton(
                                        onClick = { onClearCache?.invoke() },
                                        shape = RoundedCornerShape(9999.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                                    ) {
                                        Text("Clear")
                                    }
                                }
                            }
                        }
                    }

                    "about" -> {
                        Text(
                            text = "About Riff",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Card(
                            colors = CardDefaults.cardColors(containerColor = RiffSurfaceContainer),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Riff v42.3.0",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Made for humans • Zero telemetry",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Native Android port built with Kotlin & Jetpack Compose. Pure hardware-accelerated playback with AndroidX Media3 (ExoPlayer).",
                                    style = MaterialTheme.typography.bodySmall.copy(lineHeight = 20.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryChip(
    title: String,
    isSelected: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(18.dp)
            )
        },
        label = { Text(title) },
        shape = RoundedCornerShape(9999.dp),
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    )
}

@Composable
private fun SettingSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.primary,
                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
            )
        )
    }
}

@Composable
private fun SettingSliderRow(
    title: String,
    valueText: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = valueText,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}
