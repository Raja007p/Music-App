package com.example.ui

import android.app.Application
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.PlaylistEntity
import com.example.data.db.TuneFlowDatabase
import com.example.data.model.EqualizerState
import com.example.data.model.Playlist
import com.example.data.model.Song
import com.example.data.model.Video
import com.example.data.scanner.MediaScanner
import com.example.playback.RepeatMode
import com.example.playback.TuneFlowPlayerManager
import com.example.ui.screens.music.LibraryTab
import com.example.ui.theme.AccentColor
import com.example.ui.theme.AppThemeMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class MainNavScreen(val title: String) {
    HOME("Home"),
    MUSIC("Music"),
    VIDEOS("Videos"),
    PLAYLISTS("Playlists"),
    SETTINGS("Settings")
}

sealed class ActiveOverlay {
    data object None : ActiveOverlay()
    data object NowPlaying : ActiveOverlay()
    data object Equalizer : ActiveOverlay()
    data object SleepTimer : ActiveOverlay()
    data class VideoPlayer(val video: Video) : ActiveOverlay()
    data class PlaylistDetail(val playlist: Playlist) : ActiveOverlay()
}

class TuneFlowViewModel(application: Application) : AndroidViewModel(application) {
    private val database = TuneFlowDatabase.getDatabase(application)
    private val songDao = database.songDao()
    private val videoDao = database.videoDao()
    private val playlistDao = database.playlistDao()

    val playerManager = TuneFlowPlayerManager.getInstance(application)
    private val mediaScanner = MediaScanner(application)

    // Navigation and Overlays
    private val _currentNavScreen = MutableStateFlow(MainNavScreen.HOME)
    val currentNavScreen: StateFlow<MainNavScreen> = _currentNavScreen.asStateFlow()

    private val _activeOverlay = MutableStateFlow<ActiveOverlay>(ActiveOverlay.None)
    val activeOverlay: StateFlow<ActiveOverlay> = _activeOverlay.asStateFlow()

    private val _musicInitialTab = MutableStateFlow(LibraryTab.SONGS)
    val musicInitialTab: StateFlow<LibraryTab> = _musicInitialTab.asStateFlow()

    // Appearance State
    private val _themeMode = MutableStateFlow(AppThemeMode.DARK)
    val themeMode: StateFlow<AppThemeMode> = _themeMode.asStateFlow()

    private val _accentColor = MutableStateFlow(AccentColor.PURPLE)
    val accentColor: StateFlow<AccentColor> = _accentColor.asStateFlow()

    // Data Flows from Room
    val songs: StateFlow<List<Song>> = songDao.getAllSongs()
        .map { list -> list.map { it.toSong() } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentlyPlayed: StateFlow<List<Song>> = songDao.getRecentlyPlayed(20)
        .map { list -> list.map { it.toSong() } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteSongs: StateFlow<List<Song>> = songDao.getFavoriteSongs()
        .map { list -> list.map { it.toSong() } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val mostPlayed: StateFlow<List<Song>> = songDao.getMostPlayed(20)
        .map { list -> list.map { it.toSong() } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentlyAdded: StateFlow<List<Song>> = songDao.getRecentlyAdded(20)
        .map { list -> list.map { it.toSong() } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val videos: StateFlow<List<Video>> = videoDao.getAllVideos()
        .map { list -> list.map { it.toVideo() } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentlyPlayedVideos: StateFlow<List<Video>> = videoDao.getRecentlyPlayedVideos(50)
        .map { list -> list.map { it.toVideo() } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val playlists: StateFlow<List<Playlist>> = playlistDao.getAllPlaylists()
        .map { list ->
            list.map { entity ->
                entity.toPlaylist(count = 12)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Player State forwards
    val currentSong: StateFlow<Song?> = playerManager.currentSong
    val isPlaying: StateFlow<Boolean> = playerManager.isPlaying
    val playbackPosition: StateFlow<Long> = playerManager.playbackPosition
    val duration: StateFlow<Long> = playerManager.duration
    val queue: StateFlow<List<Song>> = playerManager.queue
    val isShuffle: StateFlow<Boolean> = playerManager.isShuffleEnabled
    val repeatMode: StateFlow<RepeatMode> = playerManager.repeatMode
    val playbackSpeed: StateFlow<Float> = playerManager.playbackSpeed
    val sleepTimerRemaining: StateFlow<Long?> = playerManager.sleepTimerRemainingMs
    val endAfterCurrentSong: StateFlow<Boolean> = playerManager.endAfterCurrentSong

    // Equalizer State forward
    val equalizerState: StateFlow<EqualizerState> = playerManager.effectsManager.equalizerState

    init {
        // Automatically scan or seed media on start
        viewModelScope.launch(Dispatchers.IO) {
            mediaScanner.scanDeviceMedia(forceRescan = false)
        }
    }

    fun setNavScreen(screen: MainNavScreen) {
        _currentNavScreen.value = screen
        _activeOverlay.value = ActiveOverlay.None
    }

    fun openOverlay(overlay: ActiveOverlay) {
        _activeOverlay.value = overlay
    }

    fun closeOverlay() {
        _activeOverlay.value = ActiveOverlay.None
    }

    fun openMusicWithTab(tab: LibraryTab) {
        _musicInitialTab.value = tab
        _currentNavScreen.value = MainNavScreen.MUSIC
        _activeOverlay.value = ActiveOverlay.None
    }

    fun playSong(song: Song, activeQueue: List<Song>? = null) {
        playerManager.playSong(song, activeQueue)
    }

    fun togglePlayPause() = playerManager.togglePlayPause()
    fun next() = playerManager.next()
    fun previous() = playerManager.previous()
    fun seekTo(positionMs: Long) = playerManager.seekTo(positionMs)
    fun toggleShuffle() = playerManager.toggleShuffle()
    fun cycleRepeat() = playerManager.cycleRepeatMode()
    fun setPlaybackSpeed(speed: Float) = playerManager.setPlaybackSpeed(speed)
    fun toggleFavorite(song: Song) = playerManager.toggleFavorite(song)
    fun addToQueue(song: Song) = playerManager.addToQueue(song)
    fun playNext(song: Song) = playerManager.playNext(song)

    // Equalizer Controls
    fun setEqualizerEnabled(enabled: Boolean) = playerManager.effectsManager.setEnabled(enabled)
    fun setEqualizerPreset(preset: String) = playerManager.effectsManager.setPreset(preset)
    fun setEqualizerBandLevel(band: Int, db: Int) = playerManager.effectsManager.setBandLevel(band, db)
    fun setBassBoost(percent: Int) = playerManager.effectsManager.setBassBoost(percent)
    fun setVirtualizer(percent: Int) = playerManager.effectsManager.setVirtualizer(percent)

    // Sleep Timer Controls
    fun startSleepTimer(minutes: Int) = playerManager.startSleepTimer(minutes)
    fun setEndAfterCurrentSong(enabled: Boolean) = playerManager.setEndAfterCurrentSong(enabled)
    fun cancelSleepTimer() = playerManager.cancelSleepTimer()

    // Playlist Controls
    fun createPlaylist(name: String) {
        viewModelScope.launch(Dispatchers.IO) {
            playlistDao.insertPlaylist(PlaylistEntity(name = name))
        }
    }

    fun deletePlaylist(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            playlistDao.deletePlaylist(id)
        }
    }

    // Appearance
    fun setThemeMode(mode: AppThemeMode) {
        _themeMode.value = mode
    }

    fun setAccentColor(accent: AccentColor) {
        _accentColor.value = accent
    }

    fun rescanMedia() {
        viewModelScope.launch(Dispatchers.IO) {
            mediaScanner.scanDeviceMedia(forceRescan = true)
        }
    }

    fun clearAudioHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            songDao.clearHistory()
        }
    }

    fun clearVideoHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            videoDao.clearVideoHistory()
        }
    }

    fun updateVideoPosition(videoId: Long, positionMs: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            videoDao.updatePlaybackPosition(videoId, positionMs)
        }
    }

    fun playVideoBackground(video: Video, startPositionMs: Long) {
        playerManager.playVideoBackground(video, startPositionMs)
    }

    fun setResumePlaybackEnabled(enabled: Boolean) {
        playerManager.preferences.isResumePlaybackEnabled = enabled
    }

    fun isResumePlaybackEnabled(): Boolean = playerManager.preferences.isResumePlaybackEnabled

    fun setVideoBackgroundPlayEnabled(enabled: Boolean) {
        playerManager.preferences.isVideoBackgroundPlayEnabled = enabled
    }

    fun isVideoBackgroundPlayEnabled(): Boolean = playerManager.preferences.isVideoBackgroundPlayEnabled
}
