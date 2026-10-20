package io.github.rootscripts.riff.feature.equalizer

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.rootscripts.riff.core.model.AudioSettings

@Composable
fun EqualizerScreen(
    settings: AudioSettings,
    onBandChange: (Int, Float) -> Unit,
    onPresetSelect: (List<Float>) -> Unit,
    onBassBoostChange: (Int) -> Unit,
    onSpeedChange: (Float) -> Unit,
    onPitchChange: (Float) -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
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
                    text = "AUDIO SUITE & EQ",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                IconButton(onClick = onReset) {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = "Reset",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Presets",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PresetChip("Flat", settings.eqBands == AudioSettings.PRESET_FLAT) {
                    onPresetSelect(AudioSettings.PRESET_FLAT)
                }
                PresetChip("Bass Boost", settings.eqBands == AudioSettings.PRESET_BASS_BOOST) {
                    onPresetSelect(AudioSettings.PRESET_BASS_BOOST)
                }
                PresetChip("Vocal Clarity", settings.eqBands == AudioSettings.PRESET_VOCAL) {
                    onPresetSelect(AudioSettings.PRESET_VOCAL)
                }
                PresetChip("Electronic", settings.eqBands == AudioSettings.PRESET_ELECTRONIC) {
                    onPresetSelect(AudioSettings.PRESET_ELECTRONIC)
                }
                PresetChip("Rock", settings.eqBands == AudioSettings.PRESET_ROCK) {
                    onPresetSelect(AudioSettings.PRESET_ROCK)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "10-Band Parametric Equalizer",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            AudioSettings.FREQUENCIES.forEachIndexed { index, freq ->
                val gain = settings.eqBands.getOrElse(index) { 0.0f }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (freq >= 1000) "${freq / 1000}k" else "${freq}Hz",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.width(48.dp)
                    )

                    Slider(
                        value = gain,
                        onValueChange = { onBandChange(index, it) },
                        valueRange = -12f..12f,
                        modifier = Modifier.weight(1f),
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary
                        )
                    )

                    Text(
                        text = "${if (gain > 0) "+" else ""}${gain.toInt()}dB",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.width(44.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "DSP Effects Chain",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            EffectSlider(
                title = "Bass Boost",
                valueText = "${settings.bassBoost}%",
                value = settings.bassBoost.toFloat(),
                range = 0f..100f,
                onValueChange = { onBassBoostChange(it.toInt()) }
            )

            EffectSlider(
                title = "Playback Speed",
                valueText = "${String.format("%.2f", settings.speed)}x",
                value = settings.speed,
                range = 0.5f..2.0f,
                onValueChange = onSpeedChange
            )

            EffectSlider(
                title = "Pitch Shift",
                valueText = "${if (settings.pitch > 0) "+" else ""}${settings.pitch.toInt()} st",
                value = settings.pitch,
                range = -12f..12f,
                onValueChange = onPitchChange
            )
        }
    }
}

@Composable
private fun PresetChip(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = { Text(title) },
        shape = RoundedCornerShape(9999.dp),
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    )
}

@Composable
private fun EffectSlider(
    title: String,
    valueText: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = valueText,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
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
