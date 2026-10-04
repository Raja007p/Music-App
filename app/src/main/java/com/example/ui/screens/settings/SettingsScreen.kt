package com.example.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentColor
import com.example.ui.theme.AppThemeMode
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite

@Composable
fun SettingsScreen(
    currentThemeMode: AppThemeMode,
    currentAccent: AccentColor,
    isResumePlaybackEnabled: Boolean = true,
    isVideoBackgroundPlayEnabled: Boolean = true,
    onSetThemeMode: (AppThemeMode) -> Unit,
    onSetAccentColor: (AccentColor) -> Unit,
    onToggleResumePlayback: (Boolean) -> Unit = {},
    onToggleVideoBackgroundPlay: (Boolean) -> Unit = {},
    onRescanMedia: () -> Unit,
    onOpenEqualizer: () -> Unit
) {
    var showAboutDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showBackupDialog by remember { mutableStateOf(false) }
    var autoPlayNext by remember { mutableStateOf(true) }
    var gaplessPlayback by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 90.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = TextWhite
                )
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Theme Mode Selector Card
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(DarkSurfaceElevated)
                        .border(1.dp, DarkBorder, RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Text(
                            text = "App Theme",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            listOf(
                                AppThemeMode.DARK to "Dark",
                                AppThemeMode.AMOLED to "AMOLED",
                                AppThemeMode.LIGHT to "Light"
                            ).forEach { (mode, label) ->
                                val isSelected = currentThemeMode == mode
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(42.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(
                                            if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFF1E243A)
                                        )
                                        .clickable { onSetThemeMode(mode) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isSelected) Color.White else TextMuted,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Accent Color Selector Card
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(DarkSurfaceElevated)
                        .border(1.dp, DarkBorder, RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Text(
                            text = "Accent Color",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(AccentColor.entries) { accent ->
                                val isSelected = currentAccent == accent
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(accent.color)
                                        .border(
                                            if (isSelected) 3.dp else 0.dp,
                                            Color.White,
                                            CircleShape
                                        )
                                        .clickable { onSetAccentColor(accent) }
                                )
                            }
                        }
                    }
                }
            }

            // Playback Options
            item {
                SettingsSectionItem(
                    title = "Audio & Equalizer",
                    icon = Icons.Default.VolumeUp,
                    subtitle = "10-band EQ, Bass boost & Virtualizer",
                    onClick = onOpenEqualizer
                )
            }

            item {
                SettingsToggleItem(
                    title = "Resume Playback",
                    subtitle = "Remember last played song and video position on restart",
                    checked = isResumePlaybackEnabled,
                    onCheckedChange = onToggleResumePlayback
                )
            }

            item {
                SettingsToggleItem(
                    title = "Background Playback",
                    subtitle = "Continue playing audio and video with notification controls",
                    checked = isVideoBackgroundPlayEnabled,
                    onCheckedChange = onToggleVideoBackgroundPlay
                )
            }

            item {
                SettingsToggleItem(
                    title = "Auto-play Next Track",
                    subtitle = "Automatically play the next song in queue",
                    checked = autoPlayNext,
                    onCheckedChange = { autoPlayNext = it }
                )
            }

            item {
                SettingsToggleItem(
                    title = "Gapless Playback",
                    subtitle = "Seamless audio transitions without silence",
                    checked = gaplessPlayback,
                    onCheckedChange = { gaplessPlayback = it }
                )
            }

            // Rescan Library
            item {
                SettingsSectionItem(
                    title = "Rescan Media Library",
                    icon = Icons.Default.Refresh,
                    subtitle = "Scan device storage for new songs and videos",
                    onClick = onRescanMedia
                )
            }

            // Backup & Restore
            item {
                SettingsSectionItem(
                    title = "Backup & Restore",
                    icon = Icons.Default.Backup,
                    subtitle = "Export or import playlists and settings",
                    onClick = { showBackupDialog = true }
                )
            }

            // Privacy
            item {
                SettingsSectionItem(
                    title = "Privacy & Permissions",
                    icon = Icons.Default.Security,
                    subtitle = "Zero cloud tracking, 100% offline player",
                    onClick = { showPrivacyDialog = true }
                )
            }

            // About
            item {
                SettingsSectionItem(
                    title = "About TuneFlow",
                    icon = Icons.Default.Info,
                    subtitle = "v1.0.0 • Offline Music & Video Player",
                    onClick = { showAboutDialog = true }
                )
            }
        }
    }

    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            containerColor = DarkSurfaceElevated,
            title = {
                Text("TuneFlow v1.0.0", color = TextWhite, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    text = "A modern all-in-one offline Music & Video Player for Android.\n\n" +
                            "Features:\n" +
                            "• Resume playback for songs and videos\n" +
                            "• Background play with notification and lockscreen widget\n" +
                            "• 10-band parametric Equalizer with Bass Boost\n" +
                            "• Dedicated Sleep Timer with visual countdown\n" +
                            "• Smart & custom Playlists and persistent queue\n" +
                            "• Fullscreen Video Player with gesture & PiP controls\n" +
                            "• 100% offline-first with zero telemetry",
                    color = TextMuted,
                    fontSize = 13.sp,
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) {
                    Text("OK", color = MaterialTheme.colorScheme.primary)
                }
            }
        )
    }

    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            containerColor = DarkSurfaceElevated,
            title = {
                Text("Privacy & Storage", color = TextWhite, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    text = "TuneFlow only requests read access to device audio and video files via standard Android MediaStore APIs. " +
                            "None of your media, playlists, or usage information is ever uploaded to any cloud server or third party. " +
                            "All statistics, playlists, and equalizers remain strictly on your local device.",
                    color = TextMuted,
                    fontSize = 13.sp,
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                TextButton(onClick = { showPrivacyDialog = false }) {
                    Text("Got It", color = MaterialTheme.colorScheme.primary)
                }
            }
        )
    }

    if (showBackupDialog) {
        AlertDialog(
            onDismissRequest = { showBackupDialog = false },
            containerColor = DarkSurfaceElevated,
            title = {
                Text("Backup & Restore", color = TextWhite, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    text = "You can export your custom playlists, favorites, and equalizer presets as a local JSON backup file, or restore them anytime.",
                    color = TextMuted,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                TextButton(onClick = { showBackupDialog = false }) {
                    Text("Export", color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showBackupDialog = false }) {
                    Text("Close", color = TextMuted)
                }
            }
        )
    }
}

@Composable
fun SettingsSectionItem(
    title: String,
    icon: ImageVector,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(DarkSurfaceElevated)
            .border(1.dp, DarkBorder, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = TextWhite,
                    fontSize = 15.sp
                )
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = TextMuted,
                    fontSize = 12.sp
                )
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = TextMuted,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun SettingsToggleItem(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(DarkSurfaceElevated)
            .border(1.dp, DarkBorder, RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = TextWhite,
                    fontSize = 15.sp
                )
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = TextMuted,
                    fontSize = 12.sp
                )
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                uncheckedTrackColor = Color(0xFF1E243A)
            )
        )
    }
}
