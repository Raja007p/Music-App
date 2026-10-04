package com.example.ui.screens.settings

import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.ViewCarousel
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.AccentColor
import com.example.ui.theme.AppThemeMode
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.LibraryLayout
import com.example.ui.theme.NowPlayingLayout
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import com.example.ui.theme.VideoLayout

@Composable
fun SettingsScreen(
    currentThemeMode: AppThemeMode,
    currentAccent: AccentColor,
    currentNowPlayingLayout: NowPlayingLayout,
    currentLibraryLayout: LibraryLayout,
    currentVideoLayout: VideoLayout,
    onSetThemeMode: (AppThemeMode) -> Unit,
    onSetAccentColor: (AccentColor) -> Unit,
    onSetNowPlayingLayout: (NowPlayingLayout) -> Unit,
    onSetLibraryLayout: (LibraryLayout) -> Unit,
    onSetVideoLayout: (VideoLayout) -> Unit,
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
        // Top Header with App Logo
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_app_logo),
                contentDescription = "Logo",
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // UI Layout Options Section
            item {
                Text(
                    text = "UI Layout & Player Styles",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 13.sp
                    ),
                    modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(DarkSurfaceElevated)
                        .border(1.dp, DarkBorder, RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        // 1. Now Playing Screen Layout
                        Column {
                            Text(
                                text = "Now Playing Player Screen",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhite
                                )
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                NowPlayingLayout.entries.forEach { layout ->
                                    val isSelected = currentNowPlayingLayout == layout
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFF1E243A))
                                            .border(
                                                1.dp,
                                                if (isSelected) MaterialTheme.colorScheme.primary else DarkBorder,
                                                RoundedCornerShape(12.dp)
                                            )
                                            .clickable { onSetNowPlayingLayout(layout) }
                                            .padding(vertical = 10.dp, horizontal = 4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = layout.title,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) Color.White else TextMuted,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                        }

                        // 2. Music Library Layout
                        Column {
                            Text(
                                text = "Music Library View",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhite
                                )
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                LibraryLayout.entries.forEach { layout ->
                                    val isSelected = currentLibraryLayout == layout
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFF1E243A))
                                            .border(
                                                1.dp,
                                                if (isSelected) MaterialTheme.colorScheme.primary else DarkBorder,
                                                RoundedCornerShape(12.dp)
                                            )
                                            .clickable { onSetLibraryLayout(layout) }
                                            .padding(vertical = 10.dp, horizontal = 4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = layout.title,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) Color.White else TextMuted,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                        }

                        // 3. Video Library Layout
                        Column {
                            Text(
                                text = "Video Library View",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhite
                                )
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                VideoLayout.entries.forEach { layout ->
                                    val isSelected = currentVideoLayout == layout
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFF1E243A))
                                            .border(
                                                1.dp,
                                                if (isSelected) MaterialTheme.colorScheme.primary else DarkBorder,
                                                RoundedCornerShape(12.dp)
                                            )
                                            .clickable { onSetVideoLayout(layout) }
                                            .padding(vertical = 10.dp, horizontal = 4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = layout.title,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) Color.White else TextMuted,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Theme Mode Selector Card
            item {
                Text(
                    text = "Appearance & Theming",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 13.sp
                    ),
                    modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
                )

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
                            text = "Theme Mode",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )
                        )
                        Spacer(modifier = Modifier.height(10.dp))
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

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Accent Color",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )
                        )
                        Spacer(modifier = Modifier.height(10.dp))
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
                                        .clickable { onSetAccentColor(accent) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Audio & Playback Options
            item {
                Text(
                    text = "Audio & Playback",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 13.sp
                    ),
                    modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
                )

                SettingsSectionItem(
                    title = "Equalizer & Sound Effects",
                    icon = Icons.Default.VolumeUp,
                    subtitle = "10-band EQ, Bass Boost, Virtualizer & 10 presets",
                    onClick = onOpenEqualizer
                )
            }

            item {
                SettingsToggleItem(
                    title = "Auto-play Next Track",
                    subtitle = "Automatically advance to the next song in queue",
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

            // Media Library Scanning
            item {
                Text(
                    text = "Media Library",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 13.sp
                    ),
                    modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
                )

                SettingsSectionItem(
                    title = "Rescan Device Media",
                    icon = Icons.Default.Refresh,
                    subtitle = "Scan device storage for real audio and video files",
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
                    title = "Privacy & Offline Mode",
                    icon = Icons.Default.Security,
                    subtitle = "100% offline player, zero data collection",
                    onClick = { showPrivacyDialog = true }
                )
            }

            // About
            item {
                SettingsSectionItem(
                    title = "About TuneFlow",
                    icon = Icons.Default.Info,
                    subtitle = "v1.0.0 • Modern Music & Video Player",
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_app_logo),
                        contentDescription = null,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("TuneFlow v1.0.0", color = TextWhite, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Text(
                    text = "TuneFlow is an offline music & video player for Android.\n\n" +
                            "• 100% offline — only your real local device media is scanned\n" +
                            "• Real-time video thumbnail extraction\n" +
                            "• Seamless background playback & notification controls\n" +
                            "• Resume playback for both audio and video\n" +
                            "• 3 customizable Now Playing, Library & Video layouts\n" +
                            "• 10-band parametric Equalizer with Bass Boost & Virtualizer",
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
                    text = "TuneFlow only accesses your device storage to index and play local audio and video files. " +
                            "None of your media, history, or settings are ever uploaded or shared. Everything remains strictly on your device.",
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
                    text = "You can export your playlists and equalizer presets as a local file, or restore them anytime.",
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
