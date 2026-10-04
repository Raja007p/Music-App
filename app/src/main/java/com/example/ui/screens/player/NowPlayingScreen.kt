package com.example.ui.screens.player

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Song
import com.example.playback.RepeatMode
import com.example.ui.components.ArtworkImage
import com.example.ui.components.formatDuration
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.PurpleBlueGradient
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode as AnimRepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.ui.theme.NowPlayingLayout

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NowPlayingScreen(
    song: Song?,
    isPlaying: Boolean,
    positionMs: Long,
    durationMs: Long,
    isShuffle: Boolean,
    repeatMode: RepeatMode,
    playbackSpeed: Float,
    queue: List<Song>,
    layout: NowPlayingLayout = NowPlayingLayout.COSMIC_EXPANSIVE,
    onChangeLayout: (NowPlayingLayout) -> Unit = {},
    onCollapse: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSeek: (Long) -> Unit,
    onToggleShuffle: () -> Unit,
    onCycleRepeat: () -> Unit,
    onToggleFavorite: () -> Unit,
    onSetSpeed: (Float) -> Unit,
    onOpenEqualizer: () -> Unit,
    onOpenSleepTimer: () -> Unit
) {
    if (song == null) return

    var totalDrag by remember { mutableFloatStateOf(0f) }
    var isSeeking by remember { mutableStateOf(false) }
    var seekProgress by remember { mutableFloatStateOf(0f) }
    var showSpeedDialog by remember { mutableStateOf(false) }
    var showQueueSheet by remember { mutableStateOf(false) }
    var showLyricsSheet by remember { mutableStateOf(false) }
    var showOptionsMenu by remember { mutableStateOf(false) }

    val currentFraction = if (durationMs > 0) {
        positionMs.toFloat() / durationMs.toFloat()
    } else 0f

    val displayProgress = if (isSeeking) seekProgress else currentFraction

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .draggable(
                state = rememberDraggableState { delta ->
                    totalDrag += delta
                },
                orientation = Orientation.Horizontal,
                onDragStopped = {
                    if (totalDrag < -120f) {
                        onNext()
                    } else if (totalDrag > 120f) {
                        onPrevious()
                    }
                    totalDrag = 0f
                }
            ),
        color = DarkBackground
    ) {
        // Ambient background glow from artwork
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.22f),
                                Color(0xFF090C15).copy(alpha = 0.95f),
                                Color(0xFF090C15)
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(36.dp))

                // Top action bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onCollapse,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(DarkSurfaceElevated)
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Collapse",
                            tint = TextWhite,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "PLAYING FROM LIBRARY",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = TextMuted,
                                letterSpacing = 1.sp,
                                fontSize = 10.sp
                            )
                        )
                        Text(
                            text = song.album.ifBlank { "TuneFlow" },
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = TextWhite
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Box {
                        IconButton(
                            onClick = { showOptionsMenu = true },
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(DarkSurfaceElevated)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "More",
                                tint = TextWhite
                            )
                        }

                        DropdownMenu(
                            expanded = showOptionsMenu,
                            onDismissRequest = { showOptionsMenu = false },
                            modifier = Modifier.background(DarkSurfaceElevated)
                        ) {
                            DropdownMenuItem(
                                text = { Text("Playback Speed (${playbackSpeed}x)", color = TextWhite) },
                                onClick = {
                                    showOptionsMenu = false
                                    showSpeedDialog = true
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("View Lyrics", color = TextWhite) },
                                onClick = {
                                    showOptionsMenu = false
                                    showLyricsSheet = true
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Equalizer", color = TextWhite) },
                                onClick = {
                                    showOptionsMenu = false
                                    onOpenEqualizer()
                                }
                            )
                            NowPlayingLayout.entries.forEach { opt ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            "Layout: ${opt.title}",
                                            color = if (layout == opt) MaterialTheme.colorScheme.primary else TextWhite
                                        )
                                    },
                                    onClick = {
                                        onChangeLayout(opt)
                                        showOptionsMenu = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Artwork or Vinyl Record Display based on Layout Option
                when (layout) {
                    NowPlayingLayout.COSMIC_EXPANSIVE -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(28.dp))
                                .border(1.dp, DarkBorder, RoundedCornerShape(28.dp))
                                .shadow(24.dp, RoundedCornerShape(28.dp), spotColor = MaterialTheme.colorScheme.primary)
                        ) {
                            ArtworkImage(
                                uri = song.albumArtUri,
                                modifier = Modifier.fillMaxSize(),
                                cornerRadius = 28.dp
                            )
                        }
                    }

                    NowPlayingLayout.VINYL_TURNTABLE -> {
                        VinylRecordView(
                            artworkUri = song.albumArtUri,
                            isPlaying = isPlaying,
                            modifier = Modifier
                                .fillMaxWidth(0.9f)
                                .aspectRatio(1f)
                        )
                    }

                    NowPlayingLayout.MINIMAL_CARD -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp))
                                .background(DarkSurfaceElevated)
                                .border(1.dp, DarkBorder, RoundedCornerShape(20.dp))
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(200.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .shadow(16.dp, RoundedCornerShape(16.dp))
                            ) {
                                ArtworkImage(
                                    uri = song.albumArtUri,
                                    modifier = Modifier.fillMaxSize(),
                                    cornerRadius = 16.dp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Title, Artist, Favorite Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = song.title,
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextWhite,
                                fontSize = 22.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = song.artist,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = TextMuted,
                                fontSize = 15.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    IconButton(
                        onClick = onToggleFavorite,
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(DarkSurfaceElevated)
                    ) {
                        Icon(
                            imageVector = if (song.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (song.isFavorite) Color(0xFFEC4899) else TextWhite,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Progress seek bar
                Slider(
                    value = displayProgress.coerceIn(0f, 1f),
                    onValueChange = {
                        isSeeking = true
                        seekProgress = it
                    },
                    onValueChangeFinished = {
                        isSeeking = false
                        val targetMs = (seekProgress * durationMs).toLong()
                        onSeek(targetMs)
                    },
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary,
                        inactiveTrackColor = Color(0xFF222944)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Time labels
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val currentMs = if (isSeeking) (seekProgress * durationMs).toLong() else positionMs
                    Text(
                        text = formatDuration(currentMs),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    )
                    Text(
                        text = formatDuration(durationMs),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Main Playback Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onToggleShuffle) {
                        Icon(
                            imageVector = Icons.Default.Shuffle,
                            contentDescription = "Shuffle",
                            tint = if (isShuffle) MaterialTheme.colorScheme.primary else TextMuted
                        )
                    }

                    IconButton(
                        onClick = onPrevious,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipPrevious,
                            contentDescription = "Previous",
                            tint = TextWhite,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    // Large Glowing Play/Pause Button
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .clip(CircleShape)
                            .background(PurpleBlueGradient)
                            .shadow(16.dp, CircleShape, spotColor = MaterialTheme.colorScheme.primary)
                            .clickable { onPlayPause() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    IconButton(
                        onClick = onNext,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipNext,
                            contentDescription = "Next",
                            tint = TextWhite,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    IconButton(onClick = onCycleRepeat) {
                        Icon(
                            imageVector = when (repeatMode) {
                                RepeatMode.OFF -> Icons.Default.Repeat
                                RepeatMode.ALL -> Icons.Default.Repeat
                                RepeatMode.ONE -> Icons.Default.RepeatOne
                            },
                            contentDescription = "Repeat",
                            tint = if (repeatMode != RepeatMode.OFF) MaterialTheme.colorScheme.primary else TextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))

                // Bottom feature buttons row (Lyrics, Equalizer, Sleep Timer, Playlist/Queue)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PlayerFeatureButton(
                        icon = Icons.Outlined.ChatBubbleOutline,
                        label = "Lyrics",
                        onClick = { showLyricsSheet = true }
                    )
                    PlayerFeatureButton(
                        icon = Icons.Default.Equalizer,
                        label = "Equalizer",
                        onClick = onOpenEqualizer
                    )
                    PlayerFeatureButton(
                        icon = Icons.Default.Timer,
                        label = "Sleep Timer",
                        onClick = onOpenSleepTimer
                    )
                    PlayerFeatureButton(
                        icon = Icons.Default.QueueMusic,
                        label = "Playlist",
                        onClick = { showQueueSheet = true }
                    )
                }

                Spacer(modifier = Modifier.height(36.dp))
            }
        }
    }

    // Queue Modal Sheet
    if (showQueueSheet) {
        ModalBottomSheet(
            onDismissRequest = { showQueueSheet = false },
            containerColor = DarkSurfaceElevated
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "Playing Queue (${queue.size} songs)",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                )
                Spacer(modifier = Modifier.height(14.dp))
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    queue.forEachIndexed { index, qSong ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (qSong.id == song.id) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.Transparent)
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${index + 1}",
                                color = TextMuted,
                                fontSize = 12.sp,
                                modifier = Modifier.width(24.dp)
                            )
                            ArtworkImage(
                                uri = qSong.albumArtUri,
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(6.dp))
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = qSong.title,
                                    color = if (qSong.id == song.id) MaterialTheme.colorScheme.primary else TextWhite,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 14.sp
                                )
                                Text(text = qSong.artist, color = TextMuted, fontSize = 12.sp)
                            }
                            Text(
                                text = formatDuration(qSong.durationMs),
                                color = TextMuted,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }

    // Lyrics Modal Sheet
    if (showLyricsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showLyricsSheet = false },
            containerColor = DarkSurfaceElevated
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                )
                Text(
                    text = song.artist,
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextMuted)
                )
                Spacer(modifier = Modifier.height(20.dp))

                val lyricsContent = song.lyrics ?: "Lyrics for this song are not available offline.\nYou can add custom lyrics in Song Details."
                Text(
                    text = lyricsContent,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = TextWhite,
                        fontSize = 16.sp,
                        lineHeight = 28.sp,
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }

    // Playback Speed Dialog
    if (showSpeedDialog) {
        ModalBottomSheet(
            onDismissRequest = { showSpeedDialog = false },
            containerColor = DarkSurfaceElevated
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "Playback Speed",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                )
                Spacer(modifier = Modifier.height(14.dp))
                listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f).forEach { speed ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                onSetSpeed(speed)
                                showSpeedDialog = false
                            }
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${speed}x",
                            color = if (playbackSpeed == speed) MaterialTheme.colorScheme.primary else TextWhite,
                            fontWeight = if (playbackSpeed == speed) FontWeight.Bold else FontWeight.Normal
                        )
                        if (playbackSpeed == speed) {
                            Text("Selected", color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PlayerFeatureButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = TextMuted,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(
                color = TextMuted,
                fontSize = 11.sp
            )
        )
    }
}

@Composable
fun VinylRecordView(
    artworkUri: String?,
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "vinyl")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(14000, easing = LinearEasing),
            repeatMode = AnimRepeatMode.Restart
        ),
        label = "rotation"
    )

    Box(
        modifier = modifier
            .shadow(28.dp, CircleShape, spotColor = MaterialTheme.colorScheme.primary)
            .clip(CircleShape)
            .background(Color(0xFF0F1117))
            .border(2.dp, Color(0xFF1F2433), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        // Vinyl grooves
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val maxR = size.width / 2f
            for (i in 5..10) {
                drawCircle(
                    color = Color.White.copy(alpha = 0.04f),
                    radius = maxR * (i / 11f),
                    center = center,
                    style = Stroke(width = 1.2f)
                )
            }
        }

        // Center spinning label with album artwork
        Box(
            modifier = Modifier
                .fillMaxSize(0.44f)
                .rotate(if (isPlaying) rotation else 0f)
                .clip(CircleShape)
                .border(2.dp, Color(0xFF333B4F), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            ArtworkImage(
                uri = artworkUri,
                modifier = Modifier.fillMaxSize(),
                cornerRadius = 100.dp
            )
            // Center spindle
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF0A0C12))
                    .border(2.dp, Color(0xFF475569), CircleShape)
            )
        }
    }
}
