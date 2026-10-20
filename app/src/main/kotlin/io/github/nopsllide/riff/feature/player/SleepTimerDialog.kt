package io.github.nopsllide.riff.feature.player

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.nopsllide.riff.core.player.SleepTimerMode

@Composable
fun SleepTimerDialog(
    currentMode: SleepTimerMode,
    onSelectMinutes: (Int) -> Unit,
    onSelectEndOfTrack: () -> Unit,
    onCancelTimer: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Filled.Timer,
                contentDescription = "Sleep Timer",
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text(text = "Sleep Timer")
        },
        text = {
            Column {
                TimerOption(
                    title = "Off",
                    isSelected = currentMode is SleepTimerMode.Off,
                    onClick = {
                        onCancelTimer()
                        onDismiss()
                    }
                )
                TimerOption(
                    title = "15 minutes",
                    isSelected = currentMode is SleepTimerMode.Minutes && (currentMode as SleepTimerMode.Minutes).remainingSeconds in (1..15 * 60),
                    onClick = {
                        onSelectMinutes(15)
                        onDismiss()
                    }
                )
                TimerOption(
                    title = "30 minutes",
                    isSelected = currentMode is SleepTimerMode.Minutes && (currentMode as SleepTimerMode.Minutes).remainingSeconds in (15 * 60 + 1..30 * 60),
                    onClick = {
                        onSelectMinutes(30)
                        onDismiss()
                    }
                )
                TimerOption(
                    title = "45 minutes",
                    isSelected = currentMode is SleepTimerMode.Minutes && (currentMode as SleepTimerMode.Minutes).remainingSeconds in (30 * 60 + 1..45 * 60),
                    onClick = {
                        onSelectMinutes(45)
                        onDismiss()
                    }
                )
                TimerOption(
                    title = "60 minutes",
                    isSelected = currentMode is SleepTimerMode.Minutes && (currentMode as SleepTimerMode.Minutes).remainingSeconds in (45 * 60 + 1..60 * 60),
                    onClick = {
                        onSelectMinutes(60)
                        onDismiss()
                    }
                )
                TimerOption(
                    title = "End of track",
                    isSelected = currentMode is SleepTimerMode.EndOfTrack,
                    onClick = {
                        onSelectEndOfTrack()
                        onDismiss()
                    }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
private fun TimerOption(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
