package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.outlined.PlaylistPlay
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.ActiveOverlay
import com.example.ui.MainNavScreen
import com.example.ui.TuneFlowViewModel
import com.example.ui.components.MiniPlayer
import com.example.ui.screens.equalizer.EqualizerScreen
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.music.LibraryTab
import com.example.ui.screens.music.MusicLibraryScreen
import com.example.ui.screens.player.NowPlayingScreen
import com.example.ui.screens.playlists.PlaylistDetailScreen
import com.example.ui.screens.playlists.PlaylistsScreen
import com.example.ui.screens.settings.SettingsScreen
import com.example.ui.screens.sleeptimer.SleepTimerScreen
import com.example.ui.screens.video.FullscreenVideoPlayer
import com.example.ui.screens.video.VideoLibraryScreen
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.TuneFlowTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: TuneFlowViewModel = viewModel()
            val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
            val accentColor by viewModel.accentColor.collectAsStateWithLifecycle()

            TuneFlowTheme(themeMode = themeMode, accent = accentColor) {
                MainApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainApp(viewModel: TuneFlowViewModel) {
    val context = LocalContext.current

    // Request permissions for storage and media playback
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.values.any { it }) {
            viewModel.rescanMedia()
        }
    }

    LaunchedEffect(Unit) {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.READ_MEDIA_AUDIO,
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.POST_NOTIFICATIONS
            )
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        val needed = permissions.filter {
            ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
        }

        if (needed.isNotEmpty()) {
            permissionLauncher.launch(needed.toTypedArray())
        }
    }

    val currentNavScreen by viewModel.currentNavScreen.collectAsStateWithLifecycle()
    val activeOverlay by viewModel.activeOverlay.collectAsStateWithLifecycle()

    val songs by viewModel.songs.collectAsStateWithLifecycle()
    val recentlyPlayed by viewModel.recentlyPlayed.collectAsStateWithLifecycle()
    val favoriteSongs by viewModel.favoriteSongs.collectAsStateWithLifecycle()
    val mostPlayed by viewModel.mostPlayed.collectAsStateWithLifecycle()
    val recentlyAdded by viewModel.recentlyAdded.collectAsStateWithLifecycle()
    val videos by viewModel.videos.collectAsStateWithLifecycle()
    val playlists by viewModel.playlists.collectAsStateWithLifecycle()

    val currentSong by viewModel.currentSong.collectAsStateWithLifecycle()
    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()
    val positionMs by viewModel.playbackPosition.collectAsStateWithLifecycle()
    val durationMs by viewModel.duration.collectAsStateWithLifecycle()
    val queue by viewModel.queue.collectAsStateWithLifecycle()
    val isShuffle by viewModel.isShuffle.collectAsStateWithLifecycle()
    val repeatMode by viewModel.repeatMode.collectAsStateWithLifecycle()
    val playbackSpeed by viewModel.playbackSpeed.collectAsStateWithLifecycle()
    val sleepTimerRemaining by viewModel.sleepTimerRemaining.collectAsStateWithLifecycle()
    val endAfterCurrentSong by viewModel.endAfterCurrentSong.collectAsStateWithLifecycle()

    val equalizerState by viewModel.equalizerState.collectAsStateWithLifecycle()
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val accentColor by viewModel.accentColor.collectAsStateWithLifecycle()
    val musicInitialTab by viewModel.musicInitialTab.collectAsStateWithLifecycle()

    // Handle back button
    BackHandler(enabled = activeOverlay !is ActiveOverlay.None || currentNavScreen != MainNavScreen.HOME) {
        if (activeOverlay !is ActiveOverlay.None) {
            viewModel.closeOverlay()
        } else if (currentNavScreen != MainNavScreen.HOME) {
            viewModel.setNavScreen(MainNavScreen.HOME)
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Main Navigation Screens
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = Color.Transparent,
                bottomBar = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                    ) {
                        // Persistent Mini Player above bottom bar
                        if (currentSong != null && activeOverlay is ActiveOverlay.None) {
                            val progress = if (durationMs > 0) positionMs.toFloat() / durationMs.toFloat() else 0f
                            MiniPlayer(
                                song = currentSong,
                                isPlaying = isPlaying,
                                progress = progress,
                                onExpand = { viewModel.openOverlay(ActiveOverlay.NowPlaying) },
                                onPlayPause = { viewModel.togglePlayPause() },
                                onNext = { viewModel.next() },
                                onPrevious = { viewModel.previous() },
                                onToggleFavorite = {
                                    currentSong?.let { viewModel.toggleFavorite(it) }
                                },
                                modifier = Modifier.testTag("mini_player")
                            )
                        }

                        // Bottom Navigation Bar
                        TuneFlowBottomBar(
                            currentScreen = currentNavScreen,
                            onSelect = { viewModel.setNavScreen(it) },
                            modifier = Modifier.testTag("bottom_nav_bar")
                        )
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    when (currentNavScreen) {
                        MainNavScreen.HOME -> HomeScreen(
                            songs = songs,
                            recentlyPlayed = recentlyPlayed,
                            favoriteSongs = favoriteSongs,
                            mostPlayed = mostPlayed,
                            recentlyAdded = recentlyAdded,
                            onSongClick = { song, list -> viewModel.playSong(song, list) },
                            onNavigateToEqualizer = { viewModel.openOverlay(ActiveOverlay.Equalizer) },
                            onNavigateToSettings = { viewModel.setNavScreen(MainNavScreen.SETTINGS) },
                            onNavigateToSearch = { viewModel.openMusicWithTab(LibraryTab.SONGS) },
                            onQuickAccessClick = { category ->
                                when (category) {
                                    "RECENTLY_PLAYED" -> viewModel.openMusicWithTab(LibraryTab.SONGS)
                                    "MOST_PLAYED" -> viewModel.openMusicWithTab(LibraryTab.SONGS)
                                    "RECENTLY_ADDED" -> viewModel.openMusicWithTab(LibraryTab.SONGS)
                                    "FAVORITES" -> {
                                        val favPlaylist = playlists.firstOrNull { it.isSmart }
                                        if (favPlaylist != null) {
                                            viewModel.openOverlay(ActiveOverlay.PlaylistDetail(favPlaylist))
                                        } else {
                                            viewModel.openMusicWithTab(LibraryTab.SONGS)
                                        }
                                    }
                                    "SONGS" -> viewModel.openMusicWithTab(LibraryTab.SONGS)
                                    "ALBUMS" -> viewModel.openMusicWithTab(LibraryTab.ALBUMS)
                                    "ARTISTS" -> viewModel.openMusicWithTab(LibraryTab.ARTISTS)
                                    "PLAYLISTS" -> viewModel.setNavScreen(MainNavScreen.PLAYLISTS)
                                    "VIDEOS" -> viewModel.setNavScreen(MainNavScreen.VIDEOS)
                                }
                            }
                        )

                        MainNavScreen.MUSIC -> MusicLibraryScreen(
                            songs = songs,
                            onSongClick = { song, list -> viewModel.playSong(song, list) },
                            onPlayNext = { viewModel.playNext(it) },
                            onAddToQueue = { viewModel.addToQueue(it) },
                            onToggleFavorite = { viewModel.toggleFavorite(it) },
                            initialTab = musicInitialTab
                        )

                        MainNavScreen.VIDEOS -> VideoLibraryScreen(
                            videos = videos,
                            onVideoClick = { video ->
                                viewModel.openOverlay(ActiveOverlay.VideoPlayer(video))
                            }
                        )

                        MainNavScreen.PLAYLISTS -> PlaylistsScreen(
                            playlists = playlists,
                            onSelectPlaylist = { playlist ->
                                viewModel.openOverlay(ActiveOverlay.PlaylistDetail(playlist))
                            },
                            onCreatePlaylist = { name -> viewModel.createPlaylist(name) },
                            onDeletePlaylist = { id -> viewModel.deletePlaylist(id) }
                        )

                        MainNavScreen.SETTINGS -> SettingsScreen(
                            currentThemeMode = themeMode,
                            currentAccent = accentColor,
                            onSetThemeMode = { viewModel.setThemeMode(it) },
                            onSetAccentColor = { viewModel.setAccentColor(it) },
                            onRescanMedia = { viewModel.rescanMedia() },
                            onOpenEqualizer = { viewModel.openOverlay(ActiveOverlay.Equalizer) }
                        )
                    }
                }
            }

            // Full Screen Animated Overlays
            AnimatedVisibility(
                visible = activeOverlay is ActiveOverlay.NowPlaying,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                NowPlayingScreen(
                    song = currentSong,
                    isPlaying = isPlaying,
                    positionMs = positionMs,
                    durationMs = durationMs,
                    isShuffle = isShuffle,
                    repeatMode = repeatMode,
                    playbackSpeed = playbackSpeed,
                    queue = queue,
                    onCollapse = { viewModel.closeOverlay() },
                    onPlayPause = { viewModel.togglePlayPause() },
                    onNext = { viewModel.next() },
                    onPrevious = { viewModel.previous() },
                    onSeek = { viewModel.seekTo(it) },
                    onToggleShuffle = { viewModel.toggleShuffle() },
                    onCycleRepeat = { viewModel.cycleRepeat() },
                    onToggleFavorite = { currentSong?.let { viewModel.toggleFavorite(it) } },
                    onSetSpeed = { viewModel.setPlaybackSpeed(it) },
                    onOpenEqualizer = { viewModel.openOverlay(ActiveOverlay.Equalizer) },
                    onOpenSleepTimer = { viewModel.openOverlay(ActiveOverlay.SleepTimer) }
                )
            }

            AnimatedVisibility(
                visible = activeOverlay is ActiveOverlay.Equalizer,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                EqualizerScreen(
                    state = equalizerState,
                    presets = viewModel.playerManager.effectsManager.presets,
                    onBack = { viewModel.closeOverlay() },
                    onToggleEnabled = { viewModel.setEqualizerEnabled(it) },
                    onSelectPreset = { viewModel.setEqualizerPreset(it) },
                    onBandChange = { band, db -> viewModel.setEqualizerBandLevel(band, db) },
                    onBassBoostChange = { viewModel.setBassBoost(it) },
                    onVirtualizerChange = { viewModel.setVirtualizer(it) }
                )
            }

            AnimatedVisibility(
                visible = activeOverlay is ActiveOverlay.SleepTimer,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                SleepTimerScreen(
                    remainingMs = sleepTimerRemaining,
                    isEndAfterCurrentSong = endAfterCurrentSong,
                    onBack = { viewModel.closeOverlay() },
                    onStartTimer = { mins -> viewModel.startSleepTimer(mins) },
                    onSetEndAfterSong = { end -> viewModel.setEndAfterCurrentSong(end) },
                    onCancelTimer = { viewModel.cancelSleepTimer() }
                )
            }

            AnimatedVisibility(
                visible = activeOverlay is ActiveOverlay.VideoPlayer,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                val video = (activeOverlay as? ActiveOverlay.VideoPlayer)?.video
                if (video != null) {
                    FullscreenVideoPlayer(
                        video = video,
                        onClose = { viewModel.closeOverlay() }
                    )
                }
            }

            AnimatedVisibility(
                visible = activeOverlay is ActiveOverlay.PlaylistDetail,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                val playlist = (activeOverlay as? ActiveOverlay.PlaylistDetail)?.playlist
                if (playlist != null) {
                    val playlistSongs = if (playlist.smartType == "FAVORITES") {
                        favoriteSongs
                    } else {
                        songs.take(12)
                    }
                    PlaylistDetailScreen(
                        playlist = playlist,
                        songs = playlistSongs,
                        onBack = { viewModel.closeOverlay() },
                        onSongClick = { song, list -> viewModel.playSong(song, list) },
                        onPlayAll = {
                            if (playlistSongs.isNotEmpty()) {
                                viewModel.playSong(playlistSongs.first(), playlistSongs)
                            }
                        },
                        onShuffleAll = {
                            if (playlistSongs.isNotEmpty()) {
                                viewModel.playSong(playlistSongs.shuffled().first(), playlistSongs.shuffled())
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun TuneFlowBottomBar(
    currentScreen: MainNavScreen,
    onSelect: (MainNavScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(DarkSurfaceElevated.copy(alpha = 0.95f))
            .border(1.dp, DarkBorder, RoundedCornerShape(22.dp))
            .padding(vertical = 6.dp, horizontal = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavItem(
                title = "Home",
                selected = currentScreen == MainNavScreen.HOME,
                selectedIcon = Icons.Filled.Home,
                unselectedIcon = Icons.Outlined.Home,
                onClick = { onSelect(MainNavScreen.HOME) },
                testTag = "nav_home"
            )
            BottomNavItem(
                title = "Music",
                selected = currentScreen == MainNavScreen.MUSIC,
                selectedIcon = Icons.Filled.LibraryMusic,
                unselectedIcon = Icons.Outlined.LibraryMusic,
                onClick = { onSelect(MainNavScreen.MUSIC) },
                testTag = "nav_music"
            )
            BottomNavItem(
                title = "Videos",
                selected = currentScreen == MainNavScreen.VIDEOS,
                selectedIcon = Icons.Filled.Videocam,
                unselectedIcon = Icons.Outlined.Videocam,
                onClick = { onSelect(MainNavScreen.VIDEOS) },
                testTag = "nav_videos"
            )
            BottomNavItem(
                title = "Playlists",
                selected = currentScreen == MainNavScreen.PLAYLISTS,
                selectedIcon = Icons.Filled.PlaylistPlay,
                unselectedIcon = Icons.Outlined.PlaylistPlay,
                onClick = { onSelect(MainNavScreen.PLAYLISTS) },
                testTag = "nav_playlists"
            )
            BottomNavItem(
                title = "Settings",
                selected = currentScreen == MainNavScreen.SETTINGS,
                selectedIcon = Icons.Filled.Settings,
                unselectedIcon = Icons.Outlined.Settings,
                onClick = { onSelect(MainNavScreen.SETTINGS) },
                testTag = "nav_settings"
            )
        }
    }
}

@Composable
fun BottomNavItem(
    title: String,
    selected: Boolean,
    selectedIcon: ImageVector,
    unselectedIcon: ImageVector,
    onClick: () -> Unit,
    testTag: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .testTag(testTag)
    ) {
        Icon(
            imageVector = if (selected) selectedIcon else unselectedIcon,
            contentDescription = title,
            tint = if (selected) MaterialTheme.colorScheme.primary else Color(0xFF64748B),
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                color = if (selected) MaterialTheme.colorScheme.primary else Color(0xFF64748B),
                fontSize = 11.sp
            )
        )
    }
}
